package bw.co.btc.kyc.onboarding.enumeration;

public enum KycDecisionStatus {
    APPROVED,   // final positive verdict
    REJECTED,   // final negative verdict
    REVIEW      // requires manual or further review
}
