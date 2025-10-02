package bw.co.btc.kyc.onboarding.enumeration;

/** High-level session milestones for onboarding. */
public enum KycSessionState {
    PENDING,
    MEDIA_ISSUED,
    MEDIA_UPLOADED,
    DOCS_SUBMITTED,
    OCR_COMPLETED,
    BIOMETRIC_SUBMITTED,
    BIOMETRIC_COMPLETED,
    DECIDED
}