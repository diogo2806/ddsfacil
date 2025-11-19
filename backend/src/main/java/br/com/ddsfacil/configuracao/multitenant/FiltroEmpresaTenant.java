package br.com.ddsfacil.configuracao.multitenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.hibernate.Session;
import org.hibernate.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
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
        String empresaHeader = request.getHeader(CABECALHO_EMPRESA);
        if (!StringUtils.hasText(empresaHeader)) {
            LOGGER.warn("Requisição sem o cabeçalho obrigatório {}.", CABECALHO_EMPRESA);
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Cabeçalho X-Empresa-Id é obrigatório para acessar a API.");
            return;
        }

        Long empresaId;
        try {
            empresaId = Long.parseLong(empresaHeader);
        } catch (NumberFormatException ex) {
            LOGGER.warn("Valor inválido recebido para o cabeçalho {}: {}", CABECALHO_EMPRESA, empresaHeader);
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Cabeçalho X-Empresa-Id deve ser numérico.");
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
}
