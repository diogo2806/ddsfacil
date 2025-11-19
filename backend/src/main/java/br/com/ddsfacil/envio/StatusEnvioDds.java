// Arquivo: backend/src/main/java/br/com/ddsfacil/envio/StatusEnvioDds.java
package br.com.ddsfacil.envio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum StatusEnvioDds {
    PENDENTE("Pendente"),
    ENVIADO("Enviado"),
    CONFIRMADO("Confirmado");

    private static final Map<String, StatusEnvioDds> NOME_PARA_ENUM_MAP =
            Stream.of(values())
                    .collect(Collectors.toMap(s -> s.name().toLowerCase(), s -> s));

    private final String descricao;

    StatusEnvioDds(String descricao) {
        this.descricao = descricao;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static StatusEnvioDds fromString(String valor) {
        if (valor == null) {
            return null;
        }

        // 1. Tenta pelo nome da constante (ex: "CONFIRMADO")
        StatusEnvioDds enumPorNome = NOME_PARA_ENUM_MAP.get(valor.toLowerCase());
        if (enumPorNome != null) {
            return enumPorNome;
        }

        // 2. Tenta pela descrição (ex: "Confirmado")
        for (StatusEnvioDds e : values()) {
            if (e.getDescricao().equalsIgnoreCase(valor)) {
                return e;
            }
        }

        // 3. Fallback para valueOf (caso o valor seja "CONFIRMADO" e não "Confirmado")
        try {
            return StatusEnvioDds.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Valor inválido para StatusEnvioDds: '" + valor + "'");
        }
    }
}