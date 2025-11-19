package br.com.ddsfacil.seguranca;

import br.com.ddsfacil.usuario.UsuarioEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class TokenJwtServico {

    private final TokenJwtPropriedades propriedades;
    private Key chaveAssinatura;

    public TokenJwtServico(TokenJwtPropriedades propriedades) {
        this.propriedades = propriedades;
    }

    public String gerarToken(UsuarioEntity usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(propriedades.getValidadeMinutos(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(Date.from(agora))
                .setExpiration(Date.from(expiracao))
                .setIssuer(propriedades.getEmissor())
                .claim("usuarioId", usuario.getId())
                .claim("empresaId", usuario.getEmpresa().getId())
                .claim("perfil", usuario.getPerfil().name())
                .claim("nome", usuario.getNome())
                .signWith(obterChave(), SignatureAlgorithm.HS256)
                .compact();
    }

    public DadosTokenJwt validar(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(obterChave())
                .build()
                .parseClaimsJws(token)
                .getBody();
        Long usuarioId = claims.get("usuarioId", Number.class).longValue();
        Long empresaId = claims.get("empresaId", Number.class).longValue();
        String perfil = claims.get("perfil", String.class);
        String nome = claims.get("nome", String.class);
        return new DadosTokenJwt(usuarioId, empresaId, claims.getSubject(), br.com.ddsfacil.usuario.PerfilUsuario.valueOf(perfil), nome);
    }

    private Key obterChave() {
        if (chaveAssinatura == null) {
            String segredo = propriedades.getSegredo();
            if (segredo == null || segredo.length() < 32) {
                throw new IllegalStateException("Segredo JWT inválido. Configure a propriedade seguranca.jwt.segredo com ao menos 32 caracteres.");
            }
            chaveAssinatura = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        }
        return chaveAssinatura;
    }
}
