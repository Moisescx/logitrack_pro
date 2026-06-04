package org.proyecto.logistica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LogisticaCamionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticaCamionesApplication.class, args);
    }

}
