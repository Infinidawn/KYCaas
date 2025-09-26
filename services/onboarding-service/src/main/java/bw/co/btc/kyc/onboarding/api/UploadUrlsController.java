package bw.co.btc.kyc.onboarding.api;

import bw.co.btc.kyc.onboarding.storage.PresignService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/public/v1/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
public class UploadUrlsController {

    private final PresignService presign;

    public UploadUrlsController(PresignService presign) {
        this.presign = presign;
    }

    /**
     * Returns presigned PUT URLs for all expected artifacts of a session.
     * Clients PUT the media directly to MinIO/S3, then call the existing
     * Document/Biometrics endpoints with the object URLs they just uploaded.
     */
    @PostMapping("/{sessionId}/upload-urls")
    public Map<String, Object> createUploadUrls(@PathVariable("sessionId") UUID sessionId,
                                                @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdHeader) throws Exception {
        // In a gateway setup, tenantId is injected; here we allow header or fallback to "demo"
        String tenantId = (tenantIdHeader == null || tenantIdHeader.isBlank()) ? "demo-tenant" : tenantIdHeader;

        String prefix = String.format("%s/sessions/%s/", tenantId, sessionId);

        Map<String, Object> out = new HashMap<>();
        out.put("sessionId", sessionId.toString());
        out.put("issuedAt", Instant.now().toString());

        Map<String, Object> items = new HashMap<>();
        items.put("documentFront", presign.mkUploadDescriptor(prefix + "documents/front.jpg"));
        items.put("documentBack",  presign.mkUploadDescriptor(prefix + "documents/back.jpg"));
        items.put("selfie",        presign.mkUploadDescriptor(prefix + "selfie/selfie.jpg"));
        items.put("livenessVideo", presign.mkUploadDescriptor(prefix + "liveness/video.mp4"));

        out.put("items", items);
        return out;
    }
}
