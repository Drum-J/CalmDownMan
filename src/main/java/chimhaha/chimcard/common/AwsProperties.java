package chimhaha.chimcard.common;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties("spring.cloud.aws")
public class AwsProperties {
    private final String region;
    private final S3Properties s3;

    public AwsProperties(String region, S3Properties s3) {
        this.region = region;
        this.s3 = s3;
    }


    public record S3Properties(String bucket, String accessKey, String secretKey, String prefix) {
    }
}
