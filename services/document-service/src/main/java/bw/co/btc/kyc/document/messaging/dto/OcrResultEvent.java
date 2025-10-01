package bw.co.btc.kyc.document.messaging.dto;

/**
 * OCR Result Event = message sent to "ocr.result" queue.
 *
 * Purpose:
 *   - Communicate the outcome of OCR processing for a document.
 *   - Allows risk-engine to use document verification as a signal
 *     in the overall KYC decision.
 *
 * Who produces it:
 *   - OcrWorker (after saving DocumentResult in DB)
 *
 * Who consumes it:
 *   - risk-engine (OcrResultListener)
 *
 * Notes:
 *   - idNumber may be user-provided (from request) or OCR-extracted.
 *   - qualityScore is a heuristic (0..1) of how reliable the scan was.
 *   - status is a simplified verdict: PASS | FAIL | REVIEW.
 */
public record OcrResultEvent(

        /** Correlation id matching the request. */
        String correlationId,

        /** Tenant identifier (UUID string or logical tenant key). */
        String tenantId,

        /** Session id (UUID string) this result belongs to. */
        String sessionId,

        /** Document type: "OMANG" or "PASSPORT". */
        String idType,

        /** ID number extracted/verified by OCR (may differ from request). */
        String idNumber,

        /** Was MRZ (machine-readable zone) detected on back image? */
        boolean mrzPresent,

        /** Quality score from OCR engine (0.0 poor .. 1.0 excellent). */
        double qualityScore,

        /** Simplified verdict: PASS | FAIL | REVIEW. */
        String status,

        /** Machine-readable reason codes (array). */
        String[] reasons,

        /** ISO-8601 timestamp when OCR finished. */
        String createdAt
) {}
