package br.com.ddsfacil.seguranca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    private final FiltroAutenticacaoJwt filtroAutenticacaoJwt;

    public SegurancaConfig(FiltroAutenticacaoJwt filtroAutenticacaoJwt) {
        this.filtroAutenticacaoJwt = filtroAutenticacaoJwt;
    }

    // Libera o Spring Security completamente para as rotas exclusivas do JobRunr
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                new AntPathRequestMatcher("/jobrunr/**"),
                new AntPathRequestMatcher("/jobrunr"),
                new AntPathRequestMatcher("/api/jobs/**"),
                new AntPathRequestMatcher("/api/jobs"),
                new AntPathRequestMatcher("/api/recurring-jobs/**"),
                new AntPathRequestMatcher("/api/recurring-jobs"),
                new AntPathRequestMatcher("/api/servers/**"),
                new AntPathRequestMatcher("/api/servers"),
                new AntPathRequestMatcher("/api/problems/**"),
                new AntPathRequestMatcher("/api/problems"),
                new AntPathRequestMatcher("/api/version/**"),
                new AntPathRequestMatcher("/api/version"),
                new AntPathRequestMatcher("/sse/**"),
                new AntPathRequestMatcher("/sse")
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filtroAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}