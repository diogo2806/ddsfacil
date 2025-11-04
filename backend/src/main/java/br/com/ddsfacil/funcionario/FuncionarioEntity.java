// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/FuncionarioEntity.java
package br.com.ddsfacil.funcionario;

import br.com.ddsfacil.local.LocalTrabalho;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "funcionarios")
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

    public FuncionarioEntity(String nome, String celular, LocalTrabalho localTrabalho) {
        this.nome = nome;
        this.celular = celular;
        this.localTrabalho = localTrabalho;
    }
}