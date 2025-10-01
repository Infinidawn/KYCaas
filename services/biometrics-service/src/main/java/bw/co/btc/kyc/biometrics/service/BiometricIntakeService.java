package bw.co.btc.kyc.biometrics.service;

import bw.co.btc.kyc.biometrics.dto.BiometricIntakeRequest;
import bw.co.btc.kyc.biometrics.entity.BiometricResult;
import bw.co.btc.kyc.biometrics.messaging.BiometricEventProducer;
import bw.co.btc.kyc.biometrics.messaging.dto.BiometricCompletedEvent;
import bw.co.btc.kyc.biometrics.messaging.dto.BiometricMetadataSaved;
import bw.co.btc.kyc.biometrics.repo.BiometricResultRepository;
import bw.co.btc.kyc.biometrics.simulator.BiometricResultSim;
import bw.co.btc.kyc.biometrics.simulator.BiometricSimulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricIntakeService {

    private final BiometricResultRepository repo;
    private final BiometricEventProducer events;
    private final BiometricSimulator simulator;

    /**
     * Orchestrates:
     *  - persist metadata
     *  - emit metadata.saved
     *  - simulate biometric matching
     *  - persist result
     *  - emit biometric.completed
     */
    public UUID process(UUID sessionId, String tenantIdStr, BiometricIntakeRequest req) {
        UUID tenantId = safeTenantUuidOrDemo(tenantIdStr);

        // --- Step 2: simulate biometric verification
        BiometricResultSim sim = simulator.run(sessionId, tenantId, req);

        // --- Step 3: persist result
        var row = BiometricResult.builder()
                .sessionId(sessionId)
                .tenantId(tenantId)
                .selfieUrl(sim.getSelfieUrl())
                .videoUrl(sim.getVideoUrl())
                .matchStatus(sim.getStatus())        // MATCHED | MISMATCHED
                .matchScore(sim.getFaceMatchScore()) // renamed for clarity
                .livenessPassed(sim.isLivenessPassed())
                .createdAt(Instant.now())
                .build();
        repo.save(row);

        // --- Step 1: emit biometric.metadata.saved
        Map<String, String> objects = new HashMap<>();
        if (req.selfieUrl() != null && !req.selfieUrl().isBlank()) {
            objects.put("selfie", req.selfieUrl());
        }
        if (req.videoUrl() != null && !req.videoUrl().isBlank()) {
            objects.put("livenessVideo", req.videoUrl());
        }


        events.emitMetadataSaved(new BiometricMetadataSaved(sessionId, tenantId, objects));
        log.info("[Biometric] metadata.saved → sessionId={} objects={}", sessionId, objects.keySet());

        // --- Step 4: emit biometric.completed
        String completedStatus = "MATCHED".equalsIgnoreCase(sim.getStatus()) ? "SUCCESS" : "FAILED";
        var evt = new BiometricCompletedEvent(
                sim.getSessionId(),
                sim.getTenantId(),
                completedStatus,
                sim.getLivenessScore(),   // ✅ send both scores
                sim.getFaceMatchScore(),
                sim.getMessage()
        );
        events.emitBiometricCompleted(evt);

        log.info("[Biometric] completed → sessionId={} status={} faceMatch={} liveness={}",
                sessionId, completedStatus, sim.getFaceMatchScore(), sim.getLivenessScore());

        return row.getId();
    }

    private UUID safeTenantUuidOrDemo(String v) {
        try { return UUID.fromString(v); }
        catch (Exception e) { return UUID.fromString("11111111-1111-1111-1111-111111111111"); }
    }
}