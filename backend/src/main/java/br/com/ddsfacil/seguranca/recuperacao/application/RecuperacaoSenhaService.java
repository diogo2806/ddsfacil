package br.com.ddsfacil.seguranca.recuperacao.application;

import br.com.ddsfacil.excecao.RegraNegocioException;
import br.com.ddsfacil.seguranca.recuperacao.domain.TokenRedefinicaoSenha;
import br.com.ddsfacil.seguranca.recuperacao.infrastructure.TokenRedefinicaoSenhaRepository;
import br.com.ddsfacil.usuario.domain.UsuarioEntity;
import br.com.ddsfacil.usuario.infrastructure.UsuarioRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecuperacaoSenhaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private static final int VALIDADE_MINUTOS = 30;

    private final UsuarioRepository usuarioRepository;
    private final TokenRedefinicaoSenhaRepository tokenRepository;
    private final EmailService emailService;
    private final EmailPropriedades emailPropriedades;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecuperacaoSenhaService(
            UsuarioRepository usuarioRepository,
            TokenRedefinicaoSenhaRepository tokenRepository,
            EmailService emailService,
            EmailPropriedades emailPropriedades,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.emailPropriedades = emailPropriedades;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void solicitar(String email) {
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase();
        usuarioRepository.findByEmailIgnoreCase(emailNormalizado).ifPresentOrElse(usuario -> {
            if (!usuario.isAtivo() || !usuario.getEmpresa().isAtivo()) {
                LOGGER.info("Solicitação de redefinição ignorada para usuário inativo: {}.", emailNormalizado);
                return;
            }
            String token = gerarToken();
            tokenRepository.save(new TokenRedefinicaoSenha(
                    usuario,
                    token,
                    LocalDateTime.now().plusMinutes(VALIDADE_MINUTOS)
            ));
            emailService.enviarRedefinicaoSenha(usuario.getEmail(), montarLink(token));
        }, () -> LOGGER.info("Solicitação de redefinição para e-mail inexistente: {}.", emailNormalizado));
    }

    @Transactional
    public void redefinir(String token, String novaSenha) {
        TokenRedefinicaoSenha registro = tokenRepository.findByToken(token == null ? "" : token.trim())
                .filter(reg -> reg.valido(LocalDateTime.now()))
                .orElseThrow(() -> new RegraNegocioException("Token de redefinição inválido ou expirado."));

        UsuarioEntity usuario = registro.getUsuario();
        usuario.redefinirSenha(passwordEncoder.encode(novaSenha));
        registro.marcarComoUsado();
        LOGGER.info("Senha redefinida via token para o usuário {}.", usuario.getEmail());
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String montarLink(String token) {
        String base = emailPropriedades.getUrlBaseRedefinicao();
        if (base == null || base.isBlank()) {
            return token;
        }
        String separador = base.contains("?") ? "&" : "?";
        return base + separador + "token=" + token;
    }
}
