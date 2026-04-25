package br.com.ddsfacil.seguranca.infrastructure.propriedades;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ddsfacil.admin")
public record AdminInicialPropriedades(String email, String senha) {
}
