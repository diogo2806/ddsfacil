// Arquivo: backend/src/main/java/br/com/ddsfacil/conteudo/ConteudoDdsEntity.java
package br.com.ddsfacil.conteudo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "conteudos_dds", indexes = @Index(name = "idx_conteudos_dds_empresa_id", columnList = "empresa_id"))
@Filter(name = "filtroEmpresa", condition = "empresa_id = :empresaId")
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

    @Column(name = "arquivo_dados", columnDefinition = "bytea")
    private byte[] arquivoDados;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // Construtor mantido para compatibilidade com o AllArgsConstructor gerado
    public ConteudoDdsEntity(String titulo, String descricao, TipoConteudo tipo, String url, String arquivoNome, String arquivoPath, byte[] arquivoDados, Long empresaId) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.url = url;
        this.arquivoNome = arquivoNome;
        this.arquivoPath = arquivoPath;
        this.arquivoDados = arquivoDados;
        this.empresaId = empresaId;
    }
}