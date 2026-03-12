package br.com.ddsfacil.seguranca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    private final FiltroAutenticacaoJwt filtroAutenticacaoJwt;

    public SegurancaConfig(FiltroAutenticacaoJwt filtroAutenticacaoJwt) {
        this.filtroAutenticacaoJwt = filtroAutenticacaoJwt;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/jobrunr/**"),
                                new AntPathRequestMatcher("/jobrunr"),
                                new AntPathRequestMatcher("/api/jobs/**"),
                                new AntPathRequestMatcher("/api/jobs"),
                                new AntPathRequestMatcher("/api/servers/**"),
                                new AntPathRequestMatcher("/api/servers"),
                                new AntPathRequestMatcher("/api/problems/**"),
                                new AntPathRequestMatcher("/api/problems"),
                                new AntPathRequestMatcher("/api/version/**"),
                                new AntPathRequestMatcher("/api/version"),
                                new AntPathRequestMatcher("/sse/**"),
                                new AntPathRequestMatcher("/sse")
                        ).permitAll()
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