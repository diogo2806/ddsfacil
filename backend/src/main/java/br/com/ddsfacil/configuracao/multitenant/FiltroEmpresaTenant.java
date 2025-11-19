package br.com.ddsfacil.configuracao.multitenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import br.com.ddsfacil.seguranca.UsuarioAutenticado;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 5)
public class FiltroEmpresaTenant extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiltroEmpresaTenant.class);
    private static final String CABECALHO_EMPRESA = "X-Empresa-Id";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean publicEndpoint = uri != null && uri.startsWith("/api/public/");
        boolean preflight = "OPTIONS".equalsIgnoreCase(request.getMethod());
        return publicEndpoint || preflight;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        UsuarioAutenticado usuarioAutenticado = obterUsuarioAutenticado();
        if (usuarioAutenticado == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token JWT inválido ou ausente.");
            return;
        }
        Long empresaId = usuarioAutenticado.getEmpresaId();
        if (!validarCabecalhoEmpresa(request, response, empresaId)) {
            return;
        }
        ContextoEmpresa.definirEmpresaId(empresaId);
        Session session = entityManager.unwrap(Session.class);
        Filter filtroEmpresa = session.enableFilter("filtroEmpresa");
        filtroEmpresa.setParameter("empresaId", empresaId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            session.disableFilter("filtroEmpresa");
            ContextoEmpresa.limpar();
        }
    }

    private boolean validarCabecalhoEmpresa(HttpServletRequest request, HttpServletResponse response, Long empresaId)
            throws IOException {
        String empresaHeader = request.getHeader(CABECALHO_EMPRESA);
        if (!StringUtils.hasText(empresaHeader)) {
            return true;
        }
        try {
            Long empresaHeaderId = Long.parseLong(empresaHeader);
            if (!empresaHeaderId.equals(empresaId)) {
                LOGGER.warn("Tentativa de acessar empresa {} informando cabeçalho {}.", empresaId, empresaHeaderId);
                response.sendError(HttpStatus.FORBIDDEN.value(), "Cabeçalho X-Empresa-Id não coincide com o token JWT.");
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Cabeçalho X-Empresa-Id deve ser numérico.");
            return false;
        }
    }

    private UsuarioAutenticado obterUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioAutenticado usuarioAutenticado) {
            return usuarioAutenticado;
        }
        return null;
    }
}
