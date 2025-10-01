package bw.co.btc.kyc.document.dto;

import java.util.UUID;

/** Ack to let client correlate their submission. */
public record DocumentIntakeResponse(
        boolean accepted,
        UUID correlationId
) {}
