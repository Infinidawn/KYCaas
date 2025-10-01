package bw.co.btc.kyc.biometrics.simulator;

import bw.co.btc.kyc.biometrics.dto.BiometricIntakeRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates biometric verification:
 * - Generates a face similarity score and a liveness score
 * - Applies simple thresholds to derive MATCHED / MISMATCHED
 *
 * Replace with a real SDK/ML integration later.
 */
@Component
public class BiometricSimulator {

    // tweakable thresholds for local testing
    private static final double FACE_MATCH_THRESHOLD = 0.75;
    private static final double LIVENESS_THRESHOLD   = 0.75;

    public BiometricResultSim run(UUID sessionId, UUID tenantId, BiometricIntakeRequest req) {
        // Generate pseudo-random but reasonable scores
        double face = scoreFromPresence(req.selfieUrl(), 0.65, 0.95);
        double live = scoreFromPresence(req.videoUrl(), 0.60, 0.93);

        boolean livenessPassed = live >= LIVENESS_THRESHOLD;
        boolean faceOk = face >= FACE_MATCH_THRESHOLD;

        String status = (livenessPassed && faceOk) ? "MATCHED" : "MISMATCHED";
        String message = status.equals("MATCHED")
                ? "ok"
                : (!livenessPassed ? "LOW_LIVENESS" : "LOW_FACE_MATCH");

        return BiometricResultSim.builder()
                .sessionId(sessionId)
                .tenantId(tenantId)
                .selfieUrl(req.selfieUrl())
                .videoUrl(req.videoUrl())
                .status(status)
                .faceMatchScore(face)
                .livenessScore(live)
                .livenessPassed(livenessPassed)
                .message(message)
                .build();
    }

    private static double scoreFromPresence(String url, double min, double max) {
        // If missing artifact, push score lower; if present, random in range
        if (url == null || url.isBlank()) {
            return clamp(min - 0.15);
        }
        return clamp(ThreadLocalRandom.current().nextDouble(min, max));
    }

    private static double clamp(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
