package br.cesar.rotavital.aceitacao;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import br.cesar.rotavital.TestcontainersConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Sobe a aplicação inteira (HTTP real numa porta aleatória) com PostgreSQL em container.
 * Só as migrações de schema: os dados sintéticos da demonstração (db/seed) ficam de fora dos testes.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.locations=classpath:db/migration")
@Import(TestcontainersConfiguration.class)
public class CucumberSpringConfiguration {
}
