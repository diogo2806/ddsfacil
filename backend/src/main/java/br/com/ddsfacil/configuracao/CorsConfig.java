package br.com.ddsfacil.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Permitir que o frontend acesse as rotas /api em desenvolvimento e producao
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(
                                "http://localhost:5173",
                                "https://ddsfacil.valenstech.com.br",
                                "https://www.ddsfacil.valenstech.com.br",
                                "https://api-ddsfacil.valenstech.com.br")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
