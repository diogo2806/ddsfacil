// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/Funcionario.java
package br.com.ddsfacil.funcionario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "funcionarios")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LGPD: Dado Pessoal
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    // LGPD: Dado Pessoal
    @Column(name = "celular", nullable = false, length = 20)
    private String celular;

    @Column(name = "obra", nullable = false, length = 120)
    private String obra;

    // Construtor mantido para compatibilidade com o AllArgsConstructor gerado
    public Funcionario(String nome, String celular, String obra) {
        this.nome = nome;
        this.celular = celular;
        this.obra = obra;
    }
}