package br.com.ddsfacil.usuario.application;

import br.com.ddsfacil.configuracao.multitenant.ContextoEmpresa;
import br.com.ddsfacil.empresa.domain.EmpresaEntity;
import br.com.ddsfacil.empresa.infrastructure.EmpresaRepository;
import br.com.ddsfacil.excecao.RecursoNaoEncontradoException;
import br.com.ddsfacil.excecao.RegraNegocioException;
import br.com.ddsfacil.usuario.domain.PerfilUsuario;
import br.com.ddsfacil.usuario.domain.UsuarioEntity;
import br.com.ddsfacil.usuario.infrastructure.UsuarioRepository;
import br.com.ddsfacil.usuario.infrastructure.dto.RedefinirSenhaRequest;
import br.com.ddsfacil.usuario.infrastructure.dto.UsuarioAtualizacaoRequest;
import br.com.ddsfacil.usuario.infrastructure.dto.UsuarioRequest;
import br.com.ddsfacil.usuario.infrastructure.dto.UsuarioResponse;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        return usuarioRepository.findAllByEmpresa_IdOrderByNomeAsc(empresaId)
                .stream()
                .map(UsuarioResponse::new)
                .toList();
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest requisicao) {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        String nome = sanitizar(requisicao.getNome());
        String email = normalizarEmail(requisicao.getEmail());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail.");
        }

        EmpresaEntity empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada."));

        UsuarioEntity usuario = new UsuarioEntity(
                empresa,
                nome,
                email,
                passwordEncoder.encode(requisicao.getSenha()),
                requisicao.getPerfil()
        );
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioAtualizacaoRequest requisicao) {
        UsuarioEntity usuario = buscarNaEmpresa(id);
        boolean ativo = Boolean.TRUE.equals(requisicao.getAtivo());

        boolean deixaDeSerAdminAtivo = usuario.getPerfil() == PerfilUsuario.ADMIN
                && (requisicao.getPerfil() != PerfilUsuario.ADMIN || !ativo);
        if (deixaDeSerAdminAtivo) {
            garantirOutroAdminAtivo(usuario.getId());
        }

        usuario.atualizarPerfilEStatus(sanitizar(requisicao.getNome()), requisicao.getPerfil(), ativo);
        return new UsuarioResponse(usuario);
    }

    @Transactional
    public void redefinirSenha(Long id, RedefinirSenhaRequest requisicao) {
        UsuarioEntity usuario = buscarNaEmpresa(id);
        usuario.redefinirSenha(passwordEncoder.encode(requisicao.getNovaSenha()));
    }

    private UsuarioEntity buscarNaEmpresa(Long id) {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        return usuarioRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private void garantirOutroAdminAtivo(Long usuarioId) {
        Long empresaId = ContextoEmpresa.obterEmpresaIdObrigatorio();
        boolean existeOutroAdmin = usuarioRepository.findAllByEmpresa_IdOrderByNomeAsc(empresaId)
                .stream()
                .anyMatch(outro -> !outro.getId().equals(usuarioId)
                        && outro.getPerfil() == PerfilUsuario.ADMIN
                        && outro.isAtivo());
        if (!existeOutroAdmin) {
            throw new RegraNegocioException("É necessário manter ao menos um administrador ativo na empresa.");
        }
    }

    private String sanitizar(String texto) {
        return Jsoup.clean(texto == null ? "" : texto, Safelist.none()).trim();
    }

    private String normalizarEmail(String email) {
        return Jsoup.clean(email == null ? "" : email, Safelist.none()).trim().toLowerCase();
    }
}
