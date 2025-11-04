// Arquivo: backend/src/main/java/br/com/ddsfacil/local/LocalTrabalho.java
package br.com.ddsfacil.local;

import br.com.ddsfacil.local.tipoLocal.TipoLocal;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Nova entidade para o "Local" de trabalho, conforme sua sugestão.
 * (Regra 7 e Regra 13)
 */
@Entity
@Table(name = "locais_trabalho")
// [REATORADO] Adicionando EntityGraph para otimizar query (Regra 2.5)
@NamedEntityGraph(
        name = "LocalTrabalho.withTipoLocal",
        attributeNodes = @NamedAttributeNode("tipoLocal")
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LocalTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_local_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TipoLocal tipoLocal;

    public LocalTrabalho(String nome, TipoLocal tipoLocal) {
        this.nome = nome;
        this.tipoLocal = tipoLocal;
    }
}