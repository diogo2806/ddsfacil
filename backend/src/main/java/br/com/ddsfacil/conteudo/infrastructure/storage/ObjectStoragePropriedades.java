package br.com.ddsfacil.conteudo.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "object.storage")
public record ObjectStoragePropriedades(
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        boolean pathStyle
) {
}
