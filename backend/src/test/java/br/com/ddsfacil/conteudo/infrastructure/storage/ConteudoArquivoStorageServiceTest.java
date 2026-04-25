package br.com.ddsfacil.conteudo.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.excecao.RegraNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class ConteudoArquivoStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        ContextoEmpresa.definirEmpresaId(1L);
    }

    @AfterEach
    void tearDown() {
        ContextoEmpresa.limpar();
    }

    @Test
    void deveCriarBucketQuandoNaoExistir() {
        ConteudoStoragePropriedades propriedades = new ConteudoStoragePropriedades("dds-conteudos", "arquivos-dds");
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenThrow(
                NoSuchBucketException.builder().statusCode(404).build()
        );

        new ConteudoArquivoStorageService(s3Client, propriedades);

        verify(s3Client).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void deveFalharComMensagemDeBucketQuandoUploadRetornaNoSuchBucket() {
        ConteudoStoragePropriedades propriedades = new ConteudoStoragePropriedades("dds-conteudos", "arquivos-dds");
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(
                NoSuchBucketException.builder().statusCode(404).build()
        );

        ConteudoArquivoStorageService service = new ConteudoArquivoStorageService(s3Client, propriedades);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> service.salvar(new byte[] {1}, "a.txt"));

        assertEquals("Bucket de conteúdo não foi encontrado no object storage.", ex.getMessage());
    }

    @Test
    void naoDeveCriarBucketQuandoJaExiste() {
        ConteudoStoragePropriedades propriedades = new ConteudoStoragePropriedades("dds-conteudos", "arquivos-dds");
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());

        new ConteudoArquivoStorageService(s3Client, propriedades);

        verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
    }
}
