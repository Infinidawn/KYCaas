package bw.co.btc.kyc.document.service;

import bw.co.btc.kyc.document.dto.DocumentIntakeRequest;
import bw.co.btc.kyc.document.mq.OcrProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentIntakeService {
    private final OcrProducer producer;
    private final String defaultBucket;

    public DocumentIntakeService(OcrProducer producer,
                                 @Value("${app.storage.defaultBucket}") String defaultBucket) {
        this.producer = producer;
        this.defaultBucket = defaultBucket;
    }

    public void handle(UUID sessionId, String tenantId, DocumentIntakeRequest req) {
        // Normalize to bucket + objectKey
        String bucket = req.bucket();
        String objectKey = req.objectKey();

        if ((bucket == null || objectKey == null) && req.frontImageUrl() != null) {
            // Parse v1 URL → assume /{bucket}/{objectKey}
            URI u = URI.create(req.frontImageUrl());
            String path = u.getPath(); // /kyc-objects/demo-tenant/sessions/.../front.jpg
            String[] parts = path.startsWith("/") ? path.substring(1).split("/", 2) : path.split("/", 2);
            bucket = (parts.length > 0) ? parts[0] : defaultBucket;
            objectKey = (parts.length > 1) ? parts[1] : null;
        }

        if (bucket == null || objectKey == null) {
            throw new IllegalArgumentException("Provide either frontImageUrl or bucket+objectKey");
        }

        Map<String, Object> msg = new HashMap<>();
        msg.put("tenantId", tenantId);
        msg.put("sessionId", sessionId.toString());
        msg.put("idType", req.idType());
        msg.put("idNumber", req.idNumber());
        msg.put("bucket", bucket);
        msg.put("objectKey", objectKey);
        msg.put("requestedAt", Instant.now().toString());

        producer.send(msg);
    }
}
