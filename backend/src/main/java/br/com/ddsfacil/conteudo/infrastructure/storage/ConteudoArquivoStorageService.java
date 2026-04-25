package br.com.ddsfacil.conteudo.infrastructure.storage;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.excecao.RegraNegocioException;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class ConteudoArquivoStorageService {

    private static final Logger log = LoggerFactory.getLogger(ConteudoArquivoStorageService.class);

    private final S3Client s3Client;
    private final ConteudoStoragePropriedades propriedades;

    public ConteudoArquivoStorageService(S3Client s3Client, ConteudoStoragePropriedades propriedades) {
        this.s3Client = s3Client;
        this.propriedades = propriedades;
        garantirBucketDisponivel();
    }

    public String salvar(byte[] dados, String nomeArquivoOriginal) {
        String chave = gerarChave(nomeArquivoOriginal);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(propriedades.bucket())
                    .key(chave)
                    .contentLength((long) dados.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(dados));
            return chave;
        } catch (S3Exception e) {
            log.error("Erro ao enviar arquivo para object storage. key={}", chave, e);
            throw construirErroArmazenamento(e);
        }
    }

    public byte[] buscar(String chave) {
        try {
            ResponseBytes<?> objeto = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(propriedades.bucket()).key(chave).build()
            );
            return objeto.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new RecursoNaoEncontradoException("Arquivo não encontrado no object storage.");
        } catch (NoSuchBucketException e) {
            log.error("Bucket de conteúdo não encontrado ao buscar arquivo. bucket={}", propriedades.bucket(), e);
            throw new RegraNegocioException("Bucket de conteúdo não foi encontrado no object storage.");
        } catch (S3Exception e) {
            log.error("Erro ao buscar arquivo no object storage. key={}", chave, e);
            throw new RegraNegocioException("Não foi possível recuperar o arquivo no object storage.");
        }
    }

    private void garantirBucketDisponivel() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(propriedades.bucket()).build());
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw e;
            }
            log.warn("Bucket de conteúdo não encontrado. Criando bucket automaticamente: {}", propriedades.bucket());
            s3Client.createBucket(CreateBucketRequest.builder().bucket(propriedades.bucket()).build());
        }
    }

    private RegraNegocioException construirErroArmazenamento(S3Exception e) {
        if (e instanceof NoSuchBucketException || e.statusCode() == 404) {
            log.error("Bucket de conteúdo não encontrado no object storage. bucket={}", propriedades.bucket(), e);
            return new RegraNegocioException("Bucket de conteúdo não foi encontrado no object storage.");
        }
        return new RegraNegocioException("Não foi possível armazenar o arquivo no object storage.");
    }

    private String gerarChave(String nomeArquivoOriginal) {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        String nome = StringUtils.hasText(nomeArquivoOriginal) ? nomeArquivoOriginal : "arquivo";
        String nomeSeguro = nome.replaceAll("[^a-zA-Z0-9._-]", "_");
        String prefixo = StringUtils.hasText(propriedades.rootPath()) ? propriedades.rootPath().trim() : "conteudos";
        return prefixo + "/empresa-" + empresaId + "/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + nomeSeguro;
    }
}
