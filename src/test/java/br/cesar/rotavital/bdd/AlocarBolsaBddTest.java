package br.cesar.rotavital.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Executa o cenário Gherkin da Parte 2 ({@code features/alocar_bolsa.feature}, na raiz do repositório) com Cucumber,
 * na fase de testes unitários (surefire): sem Spring e sem banco. Os passos chamam os casos de uso da Requisição
 * com adaptadores em memória, então o que está sendo exercitado é o agregado e a camada de aplicação.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/alocar_bolsa.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.cesar.rotavital.bdd")
@ConfigurationParameter(key = OBJECT_FACTORY_PROPERTY_NAME, value = "io.cucumber.core.backend.DefaultObjectFactory")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-alocar-bolsa.html")
public class AlocarBolsaBddTest {
}
