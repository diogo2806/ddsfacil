// Arquivo: backend/src/main/java/br/com/ddsfacil/local/domain/TipoLocal.java
package br.com.ddsfacil.local.domain;

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
import lombok.Setter; // [NOVO] Importado

@Entity
@Table(name = "tipo_local")
@Getter
@Setter // [NOVO] Adicionado para permitir atualizações
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

    @JsonValue
    public String getNome() {
        return nome;
    }

    @JsonCreator
    public static TipoLocal fromString(String valor) {
        if (valor == null) {
            return null;
        }
        return new TipoLocal(valor);
    }
}