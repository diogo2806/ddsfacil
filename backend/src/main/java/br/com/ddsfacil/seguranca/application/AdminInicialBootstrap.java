package br.com.ddsfacil.seguranca.application;

import br.com.ddsfacil.empresa.domain.EmpresaEntity;
import br.com.ddsfacil.empresa.infrastructure.EmpresaRepository;
import br.com.ddsfacil.seguranca.infrastructure.propriedades.AdminInicialPropriedades;
import br.com.ddsfacil.usuario.domain.PerfilUsuario;
import br.com.ddsfacil.usuario.domain.UsuarioEntity;
import br.com.ddsfacil.usuario.infrastructure.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(AdminInicialPropriedades.class)
public class AdminInicialBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminInicialBootstrap.class);
    private static final Long EMPRESA_PADRAO_ID = 1L;
    private static final String NOME_ADMIN_PADRAO = "Administrador DDS Facil";

    private final AdminInicialPropriedades propriedades;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInicialBootstrap(
            AdminInicialPropriedades propriedades,
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.propriedades = propriedades;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void sincronizarUsuarioAdmin() {
        String emailConfigurado = propriedades.email() == null ? "" : propriedades.email().trim().toLowerCase();
        String senhaConfigurada = propriedades.senha() == null ? "" : propriedades.senha().trim();

        if (emailConfigurado.isBlank() || senhaConfigurada.isBlank()) {
            LOGGER.warn("Variáveis ddsfacil.admin.email/ddsfacil.admin.senha não configuradas. Usuário admin não será sincronizado.");
            return;
        }

        String senhaHash = passwordEncoder.encode(senhaConfigurada);

        Optional<UsuarioEntity> usuarioPorEmail = usuarioRepository.findByEmailIgnoreCase(emailConfigurado);
        if (usuarioPorEmail.isPresent()) {
            UsuarioEntity usuarioExistente = usuarioPorEmail.get();
            usuarioExistente.atualizarDadosAcesso(NOME_ADMIN_PADRAO, emailConfigurado, senhaHash);
            usuarioRepository.save(usuarioExistente);
            LOGGER.info("Credenciais do usuário admin sincronizadas para o e-mail {}.", emailConfigurado);
            return;
        }

        Optional<UsuarioEntity> adminEmpresaPadrao = usuarioRepository.findFirstByPerfilAndEmpresaId(PerfilUsuario.ADMIN, EMPRESA_PADRAO_ID);
        if (adminEmpresaPadrao.isPresent()) {
            UsuarioEntity usuarioExistente = adminEmpresaPadrao.get();
            usuarioExistente.atualizarDadosAcesso(NOME_ADMIN_PADRAO, emailConfigurado, senhaHash);
            usuarioRepository.save(usuarioExistente);
            LOGGER.info("Usuário ADMIN da empresa padrão atualizado para o e-mail {}.", emailConfigurado);
            return;
        }

        EmpresaEntity empresa = empresaRepository.findById(EMPRESA_PADRAO_ID)
                .orElseThrow(() -> new IllegalStateException("Empresa padrão não encontrada para criação do usuário admin."));

        UsuarioEntity novoAdmin = new UsuarioEntity(
                empresa,
                NOME_ADMIN_PADRAO,
                emailConfigurado,
                senhaHash,
                PerfilUsuario.ADMIN
        );
        usuarioRepository.save(novoAdmin);
        LOGGER.info("Usuário admin criado para a empresa padrão com o e-mail {}.", emailConfigurado);
    }
}
