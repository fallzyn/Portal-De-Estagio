package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import security.Senhas;

@Configuration
public class SenhaConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Senhas.encoder();
    }
}