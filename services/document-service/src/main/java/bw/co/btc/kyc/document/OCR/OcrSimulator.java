package bw.co.btc.kyc.document.OCR;
import bw.co.btc.kyc.document.OCR.dto.OcrResult;
import bw.co.btc.kyc.document.dto.DocumentIntakeRequest;
import bw.co.btc.kyc.document.enumeration.IdType;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulates OCR for now. Replace with a real implementation (Tesseract, vendor SDK, etc.).
 * All OCR-derived fields live here to keep orchestration clean.
 */
@Component
public class OcrSimulator {

    /**
     * Run OCR simulation and return a rich result with everything needed
     * for persistence (DocumentResult) and for the outbound event.
     */
    public OcrResult run(UUID sessionId,
                         UUID tenantId,
                         DocumentIntakeRequest req,
                         String bucket,
                         String frontKey,
                         String backKey) {

        IdType idType = req.idType();
        String extractedId = pickIdNumber(req.idNumber(), frontKey);
        double quality = simulateQuality(frontKey, backKey);
        boolean mrzPresent = backKey != null && !backKey.isBlank();
        boolean mrzValid = mrzPresent; // naive assumption for now

        // Simple verdict heuristic
        String verdict = "PASS";
        String message = "ok";
        if (extractedId == null || extractedId.isBlank()) {
            verdict = "FAIL";
            message = "ID_NUMBER_UNREADABLE";
        } else if (quality < 0.6) {
            verdict = "REVIEW";
            message = "LOW_QUALITY";
        }

        // Synthetic PII fields (replace with real OCR/MRZ parsing later)
        String surname = "MOLEFHE";
        String givenNames = "KEFILWE THATO";
        String dob = "1992-05-14";
        String sex = "M";
        String nationality = "BWA";
        String docNumber = idType == IdType.PASSPORT ? "BW1234567" : "8001011234567";
        String doi = "2019-06-01";
        String doe = idType == IdType.PASSPORT ? "2029-05-31" : null;
        String mrz1 = mrzPresent ? "P<BWAEXAMPLE<<<<<<<<<<<<<<<<<<<<<<<" : null;
        String mrz2 = mrzPresent ? "A1234567<4BWA9001012M3001012<<<<<<<<<<<<<<04" : null;

        return new OcrResult(
                sessionId, tenantId,
                idType, extractedId,
                surname, givenNames, dob, sex, nationality,
                docNumber, doi, doe,
                mrzPresent, mrzValid, mrz1, mrz2,
                quality, verdict, message,
                bucket, frontKey, backKey
        );
    }

    // ---------------- helpers ----------------

    private static String pickIdNumber(String provided, String frontKey) {
        if (provided != null && !provided.isBlank()) return provided.trim();
        if (frontKey == null || frontKey.isBlank()) return null;
        String[] parts = frontKey.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            String digits = parts[i].replaceAll("\\D", "");
            if (digits.length() >= 6) return digits;
        }
        return null;
    }

    private static double simulateQuality(String frontKey, String backKey) {
        double base = 0.7;
        if (backKey == null || backKey.isBlank()) base -= 0.15;
        return Math.max(0, Math.min(1, base));
    }}