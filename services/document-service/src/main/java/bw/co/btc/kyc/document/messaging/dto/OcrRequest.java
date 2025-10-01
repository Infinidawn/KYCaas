package bw.co.btc.kyc.document.messaging.dto;

/**
 * OCR Request = message sent to the OCR queue ("ocr.request").
 *
 * Purpose:
 *   - Trigger OCR processing for a given KYC session.
 *   - Instructs the OCR worker where to find the uploaded files
 *     (front/back images in object storage).
 *
 * Who produces it:
 *   - DocumentIntakeService (document-service)
 *
 * Who consumes it:
 *   - OcrWorker (document-service, or a dedicated OCR microservice)
 *
 * What happens:
 *   1. Client uploads front/back images via presigned URLs.
 *   2. Client calls DocumentController POST /sessions/{id}/documents.
 *   3. DocumentIntakeService normalizes the bucket/objectKeys and
 *      publishes an OcrRequest message to RabbitMQ.
 *   4. OcrWorker picks it up and runs OCR.
 */
public record OcrRequest(

        /** Correlation id (UUID) for tracing the request across services. */
        String correlationId,

        /** Tenant identifier (UUID string or logical like "demo-tenant"). */
        String tenantId,

        /** Session id (UUID string) this document belongs to. */
        String sessionId,

        /** Document type: "OMANG" or "PASSPORT". */
        String idType,

        /** ID number provided by user (optional, may be empty/null). */
        String idNumber,

        /** S3/MinIO bucket where files live. */
        String bucket,

        /** Object key for front image (required). */
        String frontObjectKey,

        /** Object key for back image (optional, may be null). */
        String backObjectKey,

        /** ISO-8601 timestamp when request was created. */
        String requestedAt
) {}