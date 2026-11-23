package br.cesar.rotavital.aceitacao;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cenários de aceitação HTTP (src/test/resources/aceitacao), na fase de integração (failsafe):
 * a aplicação inteira sobe com PostgreSQL em container (cucumber-spring + Testcontainers).
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("aceitacao")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.cesar.rotavital.aceitacao")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-aceitacao.html")
public class AceitacaoIT {
}
