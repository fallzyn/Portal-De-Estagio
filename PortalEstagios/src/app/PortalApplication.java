package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Os pacotes existentes são irmãos, então informamos onde o Spring deve procurar.
@SpringBootApplication(scanBasePackages = {"app", "controller", "service"})
@EntityScan("model")
@EnableJpaRepositories("repository")
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}