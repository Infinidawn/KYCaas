package bw.co.btc.kyc.document.service;

import bw.co.btc.kyc.document.OCR.OcrSimulator;
import bw.co.btc.kyc.document.OCR.dto.OcrResult;
import bw.co.btc.kyc.document.dto.DocumentIntakeRequest;
import bw.co.btc.kyc.document.entity.DocumentResult;
import bw.co.btc.kyc.document.entity.DocumentSubmission;
import bw.co.btc.kyc.document.messaging.DocumentEventProducer;
import bw.co.btc.kyc.document.messaging.dto.DocumentMetadataSaved;
import bw.co.btc.kyc.document.messaging.dto.OcrCompletedEvent;
import bw.co.btc.kyc.document.repo.DocumentResultRepository;
import bw.co.btc.kyc.document.repo.DocumentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIntakeService {

    private final DocumentSubmissionRepository submissionRepo;
    private final DocumentResultRepository resultRepo;
    private final DocumentEventProducer events;
    private final OcrSimulator ocrSimulator;

    @Value("${app.storage.defaultBucket:kyc-objects}")
    private String defaultBucket;

    /**
     * Orchestrates: normalize addressing → persist submission → emit metadata.saved →
     * delegate OCR → persist DocumentResult → emit ocr.completed.
     *
     * @param sessionId     KYC session
     * @param tenantId      REQUIRED; injected by gateway as X-Tenant-Id
     * @param req           payload
     * @param correlationId OPTIONAL; pass X-Correlation-Id from gateway if present
     * @return correlation id (we use sessionId here, but could be a new UUID if preferred)
     */
    public UUID processDocuments(UUID sessionId, UUID tenantId, DocumentIntakeRequest req, String correlationId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Missing tenantId");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("Missing sessionId");
        }

        final String corr = correlationId != null && !correlationId.isBlank() ? correlationId : sessionId.toString();
        final Keys keys = resolveKeys(req);

        // 1) Persist submission
        persistSubmission(sessionId, tenantId, req, keys);

        // 2) Emit metadata.saved (to kick any downstream consumers)
        emitMetadataSaved(sessionId, tenantId, keys, corr);

        // 3) Run (simulated) OCR
        OcrResult ocr = ocrSimulator.run(
                sessionId, tenantId, req,
                keys.bucket(), keys.frontKey(), keys.backKey()
        );

        // 4) Persist OCR result
        persistOcrResult(ocr);

        // 5) Emit ocr.completed for risk-engine
        emitOcrCompleted(ocr, corr);

        log.info("[Document][corr={}] ocr.completed → sessionId={} status={} quality={}",
                corr, sessionId, mapToCompletedStatus(ocr.getVerdict()), ocr.getQualityScore());

        // we return a UUID correlation; using sessionId keeps it simple/traceable
        return sessionId;
    }

    // ---------------- step helpers ----------------

    private Keys resolveKeys(DocumentIntakeRequest req) {
        String bucket = req.bucket();
        String frontKey = req.frontObjectKey();
        String backKey = req.backObjectKey();

        if ((bucket == null || frontKey == null) && req.frontImageUrl() != null) {
            Parsed p = parseUrl(req.frontImageUrl());
            if (bucket == null) bucket = p.bucket();
            if (frontKey == null) frontKey = p.key();
        }
        if ((bucket == null || backKey == null) && req.backImageUrl() != null) {
            Parsed p = parseUrl(req.backImageUrl());
            if (bucket == null) bucket = p.bucket();
            if (backKey == null) backKey = p.key();
        }

        if (bucket == null) bucket = defaultBucket;
        if (frontKey == null) {
            throw new IllegalArgumentException("Front image is required (frontImageUrl or bucket+frontObjectKey).");
        }
        return new Keys(bucket, frontKey, backKey);
    }

    private void persistSubmission(UUID sessionId, UUID tenantId, DocumentIntakeRequest req, Keys keys) {
        var submission = DocumentSubmission.builder()
                .tenantId(tenantId)
                .sessionId(sessionId)
                .idType(req.idType())
                .providedIdNumber(req.idNumber())
                .bucket(keys.bucket())
                .frontObjectKey(keys.frontKey())
                .backObjectKey(keys.backKey())
                .frontImageUrl(req.frontImageUrl())
                .backImageUrl(req.backImageUrl())
                .receivedAt(Instant.now())
                .build();
        submissionRepo.save(submission);
    }

    private void emitMetadataSaved(UUID sessionId, UUID tenantId, Keys keys, String corr) {
        Map<String, String> objects = new HashMap<>();
        objects.put("documentFront", keys.frontKey());
        if (keys.backKey() != null) objects.put("documentBack", keys.backKey());

        // If your producer supports correlation id headers, pass it there too.
        events.emitMetadataSaved(new DocumentMetadataSaved(sessionId, tenantId, objects));
        log.info("[Document][corr={}] metadata.saved → sessionId={} objects={}", corr, sessionId, objects.keySet());
    }

    private void persistOcrResult(OcrResult ocr) {
        var row = DocumentResult.builder()
                .tenantId(ocr.getTenantId())
                .sessionId(ocr.getSessionId())
                .idType(ocr.getIdType())
                .ocrIdNumber(ocr.getOcrIdNumber())
                .surname(ocr.getSurname())
                .givenNames(ocr.getGivenNames())
                .fullName(null) // derive later if needed
                .dateOfBirth(ocr.getDateOfBirth())
                .sex(ocr.getSex())
                .nationality(ocr.getNationality())
                .documentNumber(ocr.getDocumentNumber())
                .dateOfIssue(ocr.getDateOfIssue())
                .dateOfExpiry(ocr.getDateOfExpiry())
                .mrzPresent(ocr.isMrzPresent())
                .mrzValid(ocr.isMrzValid())
                .mrzLine1(ocr.getMrzLine1())
                .mrzLine2(ocr.getMrzLine2())
                .qualityScore(ocr.getQualityScore())
                .status(ocr.getVerdict()) // PASS|FAIL|REVIEW
                .reasons("PASS".equalsIgnoreCase(ocr.getVerdict()) ? null : ocr.getMessage())
                .frontImageUrl(path(ocr.getBucket(), ocr.getFrontKey()))
                .backImageUrl(path(ocr.getBucket(), ocr.getBackKey()))
                .createdAt(Instant.now())
                .build();

        resultRepo.save(row);
    }

    private void emitOcrCompleted(OcrResult ocr, String corr) {
        // Contract for risk-engine: SUCCESS | FAILED (collapse REVIEW→FAILED for now)
        String completedStatus = mapToCompletedStatus(ocr.getVerdict());

        var evt = new OcrCompletedEvent(
                ocr.getSessionId(),
                ocr.getTenantId(),
                completedStatus,
                ocr.getQualityScore(),
                ocr.getMessage()
        );
        // If your producer can attach headers, include corr
        events.emitOcrCompleted(evt);
        log.info("[Document][corr={}] emitted ocr.completed → sessionId={} status={}", corr, ocr.getSessionId(), completedStatus);
    }

    private String mapToCompletedStatus(String verdict) {
        if ("PASS".equalsIgnoreCase(verdict)) return "SUCCESS";
        return "FAILED"; // includes FAIL and REVIEW
    }

    // ---------------- small records / utils ----------------

    private record Keys(String bucket, String frontKey, String backKey) {}
    private record Parsed(String bucket, String key) {}

    private Parsed parseUrl(String url) {
        var u = URI.create(url);
        var path = u.getPath().startsWith("/") ? u.getPath().substring(1) : u.getPath();
        var parts = path.split("/", 2);
        return new Parsed(parts.length > 0 ? parts[0] : null, parts.length > 1 ? parts[1] : null);
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static String path(String bucket, String key) { return blank(bucket) || blank(key) ? null : "/" + bucket + "/" + key; }
}
