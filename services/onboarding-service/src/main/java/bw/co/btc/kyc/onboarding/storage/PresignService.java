package bw.co.btc.kyc.onboarding.storage;

import bw.co.btc.kyc.onboarding.dto.UploadUrlsResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

/**
 * Service for generating presigned URLs for object uploads and previews.
 * Clients upload directly to MinIO/S3 using the presigned PUT URL,
 * then use the same objectKey in subsequent Document/Biometric API calls.
 */
@Service
public class PresignService {

    private final MinioClient minio;
    private final String bucket;
    private final int ttlSeconds;

    public PresignService(MinioClient minio,
                          @Value("${minio.bucket}") String bucket,
                          @Value("${minio.presignTtlSeconds}") int ttlSeconds) {
        this.minio = minio;
        this.bucket = bucket;
        this.ttlSeconds = ttlSeconds;
    }

    private String presignedUrl(String objectKey, Method method) throws Exception {
        return minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(method)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(ttlSeconds)
                        .build());
    }

    /**
     * Generate a typed UploadItem descriptor for a given object key.
     * @param objectKey e.g. demo-tenant/sessions/{sessionId}/documents/front.jpg
     */
    public UploadUrlsResponse.UploadItem mkUploadDescriptor(String objectKey) throws Exception {
        String putUrl = presignedUrl(objectKey, Method.PUT);
        String getUrl = presignedUrl(objectKey, Method.GET);

        // In most cases, PUT requires no extra headers for MinIO; return an empty map.
        return new UploadUrlsResponse.UploadItem(
                "PUT",
                putUrl,
                Collections.emptyMap(),
                Instant.now().plusSeconds(ttlSeconds)
        );
    }
}
