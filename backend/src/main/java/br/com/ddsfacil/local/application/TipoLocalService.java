// Arquivo: backend/src/main/java/br/com/ddsfacil/local/application/TipoLocalService.java
package br.com.ddsfacil.local.application;

import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.excecao.RegraNegocioException;
import br.com.ddsfacil.local.domain.TipoLocal;
import br.com.ddsfacil.local.infrastructure.LocalTrabalhoRepository;
import br.com.ddsfacil.local.infrastructure.TipoLocalRepository;
import br.com.ddsfacil.local.infrastructure.dto.TipoLocalRequest;
import br.com.ddsfacil.local.infrastructure.dto.TipoLocalResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TipoLocalService {

    private final TipoLocalRepository tipoLocalRepository;
    private final LocalTrabalhoRepository localTrabalhoRepository;

    public TipoLocalService(TipoLocalRepository tipoLocalRepository, LocalTrabalhoRepository localTrabalhoRepository) {
        this.tipoLocalRepository = tipoLocalRepository;
        this.localTrabalhoRepository = localTrabalhoRepository;
    }

    @Transactional
    public TipoLocalResponse criar(TipoLocalRequest request) {
        Objects.requireNonNull(request, "Requisição não pode ser nula.");
        String nomeLimpo = sanitizarNome(request.getNome());
        verificarDuplicidadeNome(nomeLimpo, null);

        TipoLocal novoTipo = new TipoLocal(nomeLimpo);
        TipoLocal salvo = tipoLocalRepository.save(novoTipo);
        return new TipoLocalResponse(salvo);
    }

    @Transactional
    public TipoLocalResponse atualizar(Long id, TipoLocalRequest request) {
        Objects.requireNonNull(request, "Requisição não pode ser nula.");
        String nomeLimpo = sanitizarNome(request.getNome());

        TipoLocal tipoLocal = localizarPorId(id);
        verificarDuplicidadeNome(nomeLimpo, id);

        tipoLocal.setNome(nomeLimpo);
        TipoLocal salvo = tipoLocalRepository.save(tipoLocal);
        return new TipoLocalResponse(salvo);
    }

    @Transactional
    public void remover(Long id) {
        TipoLocal tipoLocal = localizarPorId(id);

        // Regra de negócio: Não permitir exclusão se o tipo estiver em uso
        boolean emUso = localTrabalhoRepository.existsByTipoLocalId(id);
        if (emUso) {
            throw new RegraNegocioException("Não é possível excluir este tipo de local, pois ele está sendo utilizado por um ou mais locais de trabalho.");
        }

        tipoLocalRepository.delete(tipoLocal);
    }

    @Transactional(readOnly = true)
    public List<TipoLocalResponse> listarTodosAdmin() {
        return tipoLocalRepository.findAllByOrderByNomeAsc().stream()
                .map(TipoLocalResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarParaCombobox() {
        return tipoLocalRepository.findAllByOrderByNomeAsc().stream()
                .map(tipo -> Map.of(
                        "value", (Object) tipo.getId(),
                        "label", (Object) tipo.getNome()
                ))
                .collect(Collectors.toList());
    }

    private TipoLocal localizarPorId(Long id) {
        return tipoLocalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de local não encontrado."));
    }

    private void verificarDuplicidadeNome(String nome, Long idExcluido) {
        tipoLocalRepository.findByNomeIgnoreCase(nome)
                .ifPresent(tipoExistente -> {
                    if (idExcluido == null || !tipoExistente.getId().equals(idExcluido)) {
                        throw new RegraNegocioException("O nome '" + nome + "' já está em uso por outro tipo de local.");
                    }
                });
    }

    private String sanitizarNome(String nome) {
        if (nome == null) {
            return "";
        }
        return Jsoup.clean(nome.trim(), Safelist.none());
    }
}