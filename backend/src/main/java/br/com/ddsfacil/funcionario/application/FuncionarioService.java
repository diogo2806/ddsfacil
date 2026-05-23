// Arquivo: backend/src/main/java/br/com/ddsfacil/funcionario/application/FuncionarioService.java
package br.com.ddsfacil.funcionario.application;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.excecao.RegraNegocioException;
import br.com.ddsfacil.funcionario.domain.FuncionarioEntity;
import br.com.ddsfacil.funcionario.infrastructure.FuncionarioRepository;
import br.com.ddsfacil.funcionario.infrastructure.dto.FuncionarioRequest;
import br.com.ddsfacil.funcionario.infrastructure.dto.FuncionarioResponse;
import br.com.ddsfacil.funcionario.infrastructure.dto.ImportacaoFuncionariosResponse;
import br.com.ddsfacil.envio.infrastructure.EnvioDdsRepository;
import br.com.ddsfacil.local.domain.LocalTrabalho;
import br.com.ddsfacil.local.infrastructure.LocalTrabalhoRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FuncionarioService {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioService.class);
    private final FuncionarioRepository funcionarioRepository;
    private final LocalTrabalhoRepository localTrabalhoRepository;
    private final EnvioDdsRepository envioRepository;

    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            LocalTrabalhoRepository localTrabalhoRepository,
            EnvioDdsRepository envioRepository
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.localTrabalhoRepository = localTrabalhoRepository;
        this.envioRepository = envioRepository;
    }

    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest requisicao) {
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");
        LocalTrabalho local = localTrabalhoRepository.findById(requisicao.getLocalTrabalhoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Local de trabalho não encontrado."));
        String nomeLimpo = sanitizarTexto(requisicao.getNome());
        String celularLimpo = sanitizarCelular(requisicao.getCelular());
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();

        log.info("Criando novo funcionário. Nome: {}, Local: {}", nomeLimpo, local.getNome());
        FuncionarioEntity funcionarioEntity = new FuncionarioEntity(nomeLimpo, celularLimpo, local, empresaId);
        FuncionarioEntity salvo = funcionarioRepository.save(funcionarioEntity);

        log.info("Funcionário criado com ID: {}. (LGPD: Dados Pessoais envolvidos)", salvo.getId());
        return mapearParaResposta(salvo);
    }

    @Transactional
    public FuncionarioResponse atualizar(Long id, FuncionarioRequest requisicao) {
        Objects.requireNonNull(requisicao, "Requisição não pode ser nula.");
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();

        FuncionarioEntity funcionarioEntity = funcionarioRepository.findById(id)
                .filter(funcionario -> empresaId.equals(funcionario.getEmpresaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado."));

        LocalTrabalho local = localTrabalhoRepository.findById(requisicao.getLocalTrabalhoId())
                .filter(localTrabalho -> empresaId.equals(localTrabalho.getEmpresaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Local de trabalho não encontrado."));

        funcionarioEntity.setNome(sanitizarTexto(requisicao.getNome()));
        funcionarioEntity.setCelular(sanitizarCelular(requisicao.getCelular()));
        funcionarioEntity.setLocalTrabalho(local);

        log.info("Funcionário ID: {} atualizado. (LGPD: Dados Pessoais envolvidos)", id);
        return mapearParaResposta(funcionarioEntity);
    }

    @Transactional
    public ImportacaoFuncionariosResponse importar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("Selecione um arquivo CSV com os funcionários para importar.");
        }
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();

        Map<String, LocalTrabalho> locaisPorNome = new HashMap<>();
        for (LocalTrabalho local : localTrabalhoRepository.findAllByOrderByNomeAsc()) {
            locaisPorNome.put(local.getNome().toLowerCase(Locale.ROOT).trim(), local);
        }

        List<FuncionarioEntity> aSalvar = new ArrayList<>();
        List<ImportacaoFuncionariosResponse.ErroLinha> erros = new ArrayList<>();
        int totalLinhas = 0;

        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linha;
            int numeroLinha = 0;
            boolean primeiraLinha = true;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                String conteudo = numeroLinha == 1 ? removerBom(linha) : linha;
                if (conteudo.isBlank()) {
                    continue;
                }
                String[] colunas = dividirColunas(conteudo);
                if (primeiraLinha && pareceCabecalho(colunas)) {
                    primeiraLinha = false;
                    continue;
                }
                primeiraLinha = false;
                totalLinhas++;

                if (colunas.length < 3) {
                    erros.add(new ImportacaoFuncionariosResponse.ErroLinha(numeroLinha,
                            "Esperado: nome;celular;local."));
                    continue;
                }
                String nome = sanitizarTexto(colunas[0]);
                String celular = sanitizarCelular(colunas[1]);
                String nomeLocal = sanitizarTexto(colunas[2]).toLowerCase(Locale.ROOT);

                if (nome.isBlank() || celular.replaceAll("\\D", "").length() < 10) {
                    erros.add(new ImportacaoFuncionariosResponse.ErroLinha(numeroLinha,
                            "Nome ou celular inválido."));
                    continue;
                }
                LocalTrabalho local = locaisPorNome.get(nomeLocal);
                if (local == null) {
                    erros.add(new ImportacaoFuncionariosResponse.ErroLinha(numeroLinha,
                            "Local de trabalho não encontrado: " + sanitizarTexto(colunas[2]) + "."));
                    continue;
                }
                aSalvar.add(new FuncionarioEntity(nome, celular, local, empresaId));
            }
        } catch (IOException ex) {
            throw new RegraNegocioException("Não foi possível ler o arquivo enviado.");
        }

        if (!aSalvar.isEmpty()) {
            funcionarioRepository.saveAll(aSalvar);
        }
        log.info("Importação de funcionários: {} importados, {} erros.", aSalvar.size(), erros.size());
        return new ImportacaoFuncionariosResponse(totalLinhas, aSalvar.size(), erros);
    }

    @Transactional(readOnly = true)
    public List<FuncionarioResponse> listar(Long localTrabalhoId) {
        List<FuncionarioEntity> funcionarioEntities;
        if (localTrabalhoId != null) {
            // [REATORADO] Chamando o método derivado correto
            funcionarioEntities = funcionarioRepository.findAllByLocalTrabalhoIdOrderByNomeAsc(localTrabalhoId);
        } else {
            funcionarioEntities = funcionarioRepository.findAllByOrderByNomeAsc();
        }
        return funcionarioEntities.stream().map(this::mapearParaResposta).collect(Collectors.toList());
    }

    @Transactional
    public void remover(Long id) {
        log.info("Tentando remover funcionário ID: {}", id);
        if (!funcionarioRepository.existsById(id)) {
            log.warn("Funcionário ID: {} não encontrado para remoção.", id);
            throw new RecursoNaoEncontradoException("Funcionário não encontrado.");
        }
        if (envioRepository.existsByFuncionarioEntityId(id)) {
            throw new RegraNegocioException(
                    "Não é possível remover: o funcionário possui histórico de envios de DDS (evidência de compliance).");
        }
        funcionarioRepository.deleteById(id);
        log.info("Funcionário ID: {} removido com sucesso. (LGPD: Remoção de Dados Pessoais)", id);
    }

    private FuncionarioResponse mapearParaResposta(FuncionarioEntity funcionarioEntity) {
        return new FuncionarioResponse(funcionarioEntity);
    }

    private String sanitizarTexto(String texto) {
        return Jsoup.clean(texto, Safelist.none()).trim();
    }

    private String sanitizarCelular(String celular) {
        String semTags = Jsoup.clean(celular, Safelist.none());
        return semTags.replaceAll("[^0-9()+\\-\\s]", "").trim();
    }

    private String[] dividirColunas(String linha) {
        String separador = linha.contains(";") ? ";" : ",";
        return linha.split(separador, -1);
    }

    private boolean pareceCabecalho(String[] colunas) {
        if (colunas.length == 0) {
            return false;
        }
        String primeira = colunas[0].toLowerCase(Locale.ROOT).trim();
        return primeira.equals("nome");
    }

    private String removerBom(String linha) {
        if (!linha.isEmpty() && linha.charAt(0) == '\uFEFF') {
            return linha.substring(1);
        }
        return linha;
    }
}