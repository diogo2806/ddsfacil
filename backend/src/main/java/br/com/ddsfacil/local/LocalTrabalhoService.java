// Arquivo: backend/src/main/java/br/com/ddsfacil/local/LocalTrabalhoService.java
package br.com.ddsfacil.local;

import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.local.dto.LocalTrabalhoRequest;
import br.com.ddsfacil.local.dto.LocalTrabalhoResponse;
import br.com.ddsfacil.local.tipoLocal.TipoLocal;
import br.com.ddsfacil.local.tipoLocal.TipoLocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LocalTrabalhoService {

    private final LocalTrabalhoRepository localTrabalhoRepository;
    private final TipoLocalRepository tipoLocalRepository;

    public LocalTrabalhoService(LocalTrabalhoRepository localTrabalhoRepository, TipoLocalRepository tipoLocalRepository) {
        this.localTrabalhoRepository = localTrabalhoRepository;
        this.tipoLocalRepository = tipoLocalRepository;
    }

    @Transactional
    public LocalTrabalhoResponse criar(LocalTrabalhoRequest request) {
        Objects.requireNonNull(request, "Requisição não pode ser nula.");
        TipoLocal tipoLocal = tipoLocalRepository.findById(request.getTipoLocalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de local não encontrado."));
        LocalTrabalho local = new LocalTrabalho(request.getNome(), tipoLocal);
        LocalTrabalho salvo = localTrabalhoRepository.save(local);
        return new LocalTrabalhoResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<LocalTrabalhoResponse> listarTodos() {
        // [REATORADO] Usando a query otimizada (agora método derivado)
        return localTrabalhoRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(LocalTrabalhoResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void remover(Long id) {
        if (!localTrabalhoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Local de trabalho não encontrado.");
        }
        // TODO: Adicionar verificação se o local está em uso por algum funcionário antes de excluir
        localTrabalhoRepository.deleteById(id);
    }

    @Transactional
    public LocalTrabalhoResponse atualizar(Long id, LocalTrabalhoRequest request) {
        Objects.requireNonNull(request, "Requisição não pode ser nula.");
        LocalTrabalho existente = localTrabalhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Local de trabalho não encontrado."));
        TipoLocal tipoLocal = tipoLocalRepository.findById(request.getTipoLocalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de local não encontrado."));

        existente.setNome(request.getNome());
        existente.setTipoLocal(tipoLocal);
        LocalTrabalho salvo = localTrabalhoRepository.save(existente);
        return new LocalTrabalhoResponse(salvo);
    }
}