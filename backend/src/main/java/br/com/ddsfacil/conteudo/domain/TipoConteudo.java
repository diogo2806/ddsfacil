// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/domain/TipoConteudo.java
package br.com.ddsfacil.conteudo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TipoConteudo {
    TEXTO("TEXTO"),
    LINK("LINK"),
    ARQUIVO("ARQUIVO");

    private static final Map<String, TipoConteudo> NOME_PARA_ENUM_MAP =
            Stream.of(values())
                    .collect(Collectors.toMap(s -> s.name().toLowerCase(), s -> s));

    private final String descricao;

    TipoConteudo(String descricao) {
        this.descricao = descricao;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static TipoConteudo fromString(String valor) {
        if (valor == null) {
            return null;
        }

        // 1. Tenta pelo nome da constante (ex: "TEXTO")
        TipoConteudo enumPorNome = NOME_PARA_ENUM_MAP.get(valor.toLowerCase());
        if (enumPorNome != null) {
            return enumPorNome;
        }

        // 2. Tenta pela descrição (ex: "TEXTO")
        for (TipoConteudo e : values()) {
            if (e.getDescricao().equalsIgnoreCase(valor)) {
                return e;
            }
        }

        // 3. Fallback para valueOf (caso o valor seja "TEXTO" e não "Texto")
        try {
            return TipoConteudo.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Valor inválido para TipoConteudo: '" + valor + "'");
        }
    }
}