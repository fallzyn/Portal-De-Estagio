package config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import service.InicializacaoAdminService;

@Component
@ConditionalOnProperty(name = "portal.admin.inicializar", havingValue = "true")
public class InicializadorAdministrador implements CommandLineRunner {
    private final InicializacaoAdminService administradores;
    private final String email;
    private final String senha;
    public InicializadorAdministrador(InicializacaoAdminService administradores,
            @Value("${PORTAL_ADMIN_EMAIL:}") String email,
            @Value("${PORTAL_ADMIN_PASSWORD:}") String senha) {
        this.administradores = administradores;
        this.email = email;
        this.senha = senha;
    }
    @Override public void run(String... args) { administradores.inicializar(email, senha); }
}
