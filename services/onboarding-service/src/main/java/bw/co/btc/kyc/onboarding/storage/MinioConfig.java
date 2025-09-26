package bw.co.btc.kyc.onboarding.storage;

import io.minio.MinioClient;
import io.minio.MakeBucketArgs;
import io.minio.BucketExistsArgs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}") private String internalEndpoint;        // e.g. http://minio:9000
    @Value("${minio.publicEndpoint}") private String publicEndpoint;    // e.g. http://localhost:9000
    @Value("${minio.accessKey}") private String accessKey;
    @Value("${minio.secretKey}") private String secretKey;
    @Value("${minio.bucket}") private String bucket;

    @Bean("minioInternal")
    public MinioClient minioInternal() {
        return MinioClient.builder()
                .endpoint(internalEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean("minioPublic")
    public MinioClient minioPublic() {
        // This client is used only to generate presigned URLs (no network call needed)
        return MinioClient.builder()
                .endpoint(publicEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public String ensureBucket(@Qualifier("minioInternal") MinioClient client) throws Exception {
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        return bucket;
    }
}
