// Arquivo: backend/src/main/java/br/com/ddsfacil/local/LocalTrabalho.java
package br.com.ddsfacil.local;

import br.com.ddsfacil.local.tipoLocal.TipoLocal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Filter;

/**
 * Nova entidade para o "Local" de trabalho, conforme sua sugestão.
 * (Regra 7 e Regra 13)
 */
@Entity
@Table(name = "locais_trabalho", indexes = @Index(name = "idx_locais_trabalho_empresa_id", columnList = "empresa_id"))
// [REATORADO] Adicionando EntityGraph para otimizar query (Regra 2.5)
@NamedEntityGraph(
        name = "LocalTrabalho.withTipoLocal",
        attributeNodes = @NamedAttributeNode("tipoLocal")
)
@Filter(name = "filtroEmpresa", condition = "empresa_id = :empresaId")
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

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    public LocalTrabalho(String nome, TipoLocal tipoLocal, Long empresaId) {
        this.nome = nome;
        this.tipoLocal = tipoLocal;
        this.empresaId = empresaId;
    }
}