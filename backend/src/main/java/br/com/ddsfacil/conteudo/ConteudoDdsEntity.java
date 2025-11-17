// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/ConteudoDdsEntity.java
package br.com.ddsfacil.conteudo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conteudos_dds")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ConteudoDdsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "descricao", nullable = false, length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoConteudo tipo;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "arquivo_nome", length = 255)
    private String arquivoNome;

    @Column(name = "arquivo_path", length = 1000)
    private String arquivoPath;

    @Lob
    @Column(name = "arquivo_dados")
    private byte[] arquivoDados;

    // Construtor mantido para compatibilidade com o AllArgsConstructor gerado
    public ConteudoDdsEntity(String titulo, String descricao, TipoConteudo tipo, String url, String arquivoNome, String arquivoPath, byte[] arquivoDados) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.url = url;
        this.arquivoNome = arquivoNome;
        this.arquivoPath = arquivoPath;
        this.arquivoDados = arquivoDados;
    }
}