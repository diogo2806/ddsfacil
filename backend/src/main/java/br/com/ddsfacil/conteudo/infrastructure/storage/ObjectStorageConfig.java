package br.com.ddsfacil.conteudo.infrastructure.storage;

import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@EnableConfigurationProperties({ObjectStoragePropriedades.class, ConteudoStoragePropriedades.class})
public class ObjectStorageConfig {

    @Bean
    public S3Client s3Client(ObjectStoragePropriedades propriedades) {
        S3ClientBuilder builder = S3Client.builder()
                .endpointOverride(URI.create(propriedades.endpoint()))
                .region(Region.of(propriedades.region()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(propriedades.pathStyle()).build());

        if (temCredenciais(propriedades)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(propriedades.accessKey(), propriedades.secretKey())
            ));
        } else {
            builder.credentialsProvider(AnonymousCredentialsProvider.create());
        }

        return builder.build();
    }

    private boolean temCredenciais(ObjectStoragePropriedades propriedades) {
        return propriedades.accessKey() != null
                && !propriedades.accessKey().isBlank()
                && propriedades.secretKey() != null
                && !propriedades.secretKey().isBlank();
    }
}
