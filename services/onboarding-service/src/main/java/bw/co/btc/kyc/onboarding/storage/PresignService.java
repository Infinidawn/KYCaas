package bw.co.btc.kyc.onboarding.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PresignService {

    private final MinioClient presignMinio;   // public host client
    private final String bucket;
    private final int ttlSeconds;

    public PresignService(@Qualifier("minioPublic") MinioClient presignMinio,
                          @Value("${minio.bucket}") String bucket,
                          @Value("${minio.presignTtlSeconds}") int ttlSeconds) {
        this.presignMinio = presignMinio;
        this.bucket = bucket;
        this.ttlSeconds = ttlSeconds;
    }

    public String presignedPut(String objectKey) throws Exception {
        return presignMinio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(ttlSeconds)
                        .build());
    }

    public String presignedGet(String objectKey) throws Exception {
        return presignMinio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(ttlSeconds)
                        .build());
    }

    public Map<String, Object> mkUploadDescriptor(String objectKey) throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put("objectKey", objectKey);
        m.put("putUrl", presignedPut(objectKey));
        m.put("getUrlPreview", presignedGet(objectKey));
        m.put("expiresInSeconds", ttlSeconds);
        return m;
    }
}
