package bw.co.btc.kyc.biometrics.dto;

/**
 * Client payload for submitting biometric artifacts for a session.
 * Keep it simple for now: direct URLs to the selfie image and optional liveness video.
 * (If you later switch to object storage keys, add fields for bucket/objectKey.)
 */
public record BiometricIntakeRequest(
        String selfieUrl,
        String videoUrl
) {}