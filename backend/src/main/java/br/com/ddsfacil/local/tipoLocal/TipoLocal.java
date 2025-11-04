// Arquivo: backend/src/main/java/br/com/ddsfacil/local/TipoLocal.java
package br.com.ddsfacil.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Nova entidade para o "Tipo" de local, conforme sua sugestão.
 * (Regra 7 - Mapeamento JPA)
 */
@Entity
@Table(name = "tipo_local")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TipoLocal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    public TipoLocal(String nome) {
        this.nome = nome;
    }

    /**
     * Serializa o Enum para o seu nome amigável.
     * (Regra 15)
     */
    @JsonValue
    public String getNome() {
        return nome;
    }

    /**
     * Desserialização robusta (Regra 15) - Embora esta entidade não
     * deva ser desserializada diretamente em cenários de requisição complexos,
     * o método é útil.
     */
    @JsonCreator
    public static TipoLocal fromString(String valor) {
        if (valor == null) {
            return null;
        }
        // Em um cenário real, deveríamos buscar pelo nome no repositório,
        // mas para DTOs de entrada, receberemos o ID.
        // Isso é mais para conformidade com a Regra 15.
        return new TipoLocal(valor);
    }
}