package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Os pacotes existentes são irmãos, então informamos onde o Spring deve procurar.
@SpringBootApplication(scanBasePackages = {"app", "controller"})
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}