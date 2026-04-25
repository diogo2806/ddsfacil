package br.com.ddsfacil.conteudo.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "conteudo.storage")
public record ConteudoStoragePropriedades(
        String bucket,
        String rootPath
) {
}
