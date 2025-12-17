package br.com.ddsfacil.seguranca.config;

import br.com.ddsfacil.seguranca.application.TokenJwtServico;
import br.com.ddsfacil.seguranca.infrastructure.DadosTokenJwt;
import br.com.ddsfacil.seguranca.infrastructure.UsuarioAutenticado;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiltroAutenticacaoJwt.class);
    private final TokenJwtServico tokenJwtServico;

    public FiltroAutenticacaoJwt(TokenJwtServico tokenJwtServico) {
        this.tokenJwtServico = tokenJwtServico;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String autorizacao = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(autorizacao) && autorizacao.startsWith("Bearer ")) {
            String token = autorizacao.substring(7);
            try {
                DadosTokenJwt dadosTokenJwt = tokenJwtServico.validar(token);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsuarioAutenticado usuario = new UsuarioAutenticado(dadosTokenJwt);
                    UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            usuario.getAuthorities()
                    );
                    autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                }
            } catch (Exception ex) {
                LOGGER.warn("Token JWT inválido: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
