// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/domain/FuncionarioEntity.java
package br.com.ddsfacil.funcionario.domain;

import br.com.ddsfacil.local.domain.LocalTrabalho;
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
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "funcionarios", indexes = @Index(name = "idx_funcionarios_empresa_id", columnList = "empresa_id"))
// [REATORADO] Adicionando EntityGraph para otimizar queries (Regra 2.5)
@NamedEntityGraph(
        name = "Funcionario.withLocalTrabalhoAndTipo",
        attributeNodes = {
                @NamedAttributeNode(value = "localTrabalho", subgraph = "localTrabalho.tipoLocal")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "localTrabalho.tipoLocal",
                        attributeNodes = @NamedAttributeNode("tipoLocal")
                )
        }
)
@Filter(name = "filtroEmpresa", condition = "empresa_id = :empresaId")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FuncionarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "celular", nullable = false, length = 20)
    private String celular;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "local_trabalho_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LocalTrabalho localTrabalho;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    public FuncionarioEntity(String nome, String celular, LocalTrabalho localTrabalho, Long empresaId) {
        this.nome = nome;
        this.celular = celular;
        this.localTrabalho = localTrabalho;
        this.empresaId = empresaId;
    }
}