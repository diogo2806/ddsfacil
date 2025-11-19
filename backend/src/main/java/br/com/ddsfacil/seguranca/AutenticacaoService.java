package br.com.ddsfacil.seguranca;

import br.com.ddsfacil.excecao.RegraNegocioException;
import br.com.ddsfacil.seguranca.dto.AutenticacaoRequest;
import br.com.ddsfacil.seguranca.dto.AutenticacaoResponse;
import br.com.ddsfacil.usuario.UsuarioEntity;
import br.com.ddsfacil.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutenticacaoService.class);
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenJwtServico tokenJwtServico;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            TokenJwtServico tokenJwtServico
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenJwtServico = tokenJwtServico;
    }

    public AutenticacaoResponse autenticar(AutenticacaoRequest requisicao) {
        String emailSanitizado = requisicao.getEmail().trim().toLowerCase();
        UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(emailSanitizado)
                .orElseThrow(() -> new RegraNegocioException("Usuário ou senha inválidos."));
        if (!usuario.isAtivo() || !usuario.getEmpresa().isAtivo()) {
            throw new RegraNegocioException("Acesso desautorizado. Entre em contato com o suporte.");
        }
        if (!passwordEncoder.matches(requisicao.getSenha(), usuario.getSenhaHash())) {
            LOGGER.warn("Tentativa de login com senha inválida para o e-mail {}.", emailSanitizado);
            throw new RegraNegocioException("Usuário ou senha inválidos.");
        }
        String token = tokenJwtServico.gerarToken(usuario);
        return new AutenticacaoResponse(token, usuario.getEmpresa().getId(), usuario.getNome(), usuario.getPerfil());
    }
}
