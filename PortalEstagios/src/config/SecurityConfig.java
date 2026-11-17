package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import java.util.Map;
import java.util.List;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {
    private static boolean querJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            UserDetailsService usuarios, PasswordEncoder encoder, ObjectMapper json) throws Exception {
        DaoAuthenticationProvider autenticacao = new DaoAuthenticationProvider(usuarios);
        autenticacao.setPasswordEncoder(encoder);
        http.authenticationProvider(autenticacao);

        http.authorizeHttpRequests(regras -> regras
            .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
            .requestMatchers(HttpMethod.GET, "/login", "/login.html", "/cadastro.html", "/css/auth.css", "/js/auth.js",
                "/portal.html", "/js/portal.js", "/api/status", "/api/areas", "/api/vagas", "/api/auth/csrf").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/cadastro").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
            .requestMatchers("/api/empresa/**").hasRole("EMPRESA")
            .requestMatchers("/api/estudante/**").hasRole("ESTUDANTE")
            .requestMatchers(HttpMethod.GET, "/", "/conta.html", "/api/auth/eu").authenticated()
            .anyRequest().denyAll());

        // CSRF permanece habilitado, inclusive para cadastro, login e logout.
        http.formLogin(login -> login
            .loginPage("/login.html")
            .loginProcessingUrl("/api/auth/login")
            .failureUrl("/login.html?error")
            .successHandler((request, response, authentication) -> {
                if (querJson(request)) {
                    response.setContentType("application/json;charset=UTF-8");
                    json.writeValue(response.getWriter(), Map.of("autenticado", true));
                } else {
                    response.sendRedirect(request.getContextPath() + "/conta.html");
                }
            })
            .failureHandler(new SimpleUrlAuthenticationFailureHandler("/login.html?error") {
                @Override
                public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException exception) throws IOException, ServletException {
                    if (querJson(request)) {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        json.writeValue(response.getWriter(), Map.of("mensagem", "E-mail ou senha inválidos.", "detalhes", List.of()));
                    } else {
                        super.onAuthenticationFailure(request, response,
                            new BadCredentialsException("E-mail ou senha inválidos."));
                    }
                }
            }).permitAll());

        http.logout(logout -> logout.logoutUrl("/logout")
            .logoutSuccessUrl("/login.html?logout")
            .invalidateHttpSession(true).deleteCookies("JSESSIONID"));
        http.sessionManagement(sessoes -> sessoes.sessionFixation(fixacao -> fixacao.changeSessionId()));
        http.exceptionHandling(erros -> erros
            .defaultAuthenticationEntryPointFor((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    json.writeValue(response.getWriter(), Map.of("mensagem", "Autenticação necessária.", "detalhes", List.of()));
                },
                AntPathRequestMatcher.antMatcher("/api/**"))
            .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"),
                AnyRequestMatcher.INSTANCE)
            .accessDeniedHandler((request, response, exception) -> {
                if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    json.writeValue(response.getWriter(), Map.of("mensagem", "Operação não permitida.", "detalhes", List.of()));
                } else {
                    response.sendError(403, "Operação não permitida.");
                }
            }));
        return http.build();
    }
}
