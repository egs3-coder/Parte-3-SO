package br.cesar.rotavital.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Regra de dependência da Parte 2 (C.2), verificada a cada push pela esteira.
 * Se alguém fizer o domínio importar Spring, ou um contexto ler a tabela de outro, este teste fica vermelho
 * e o deploy não acontece.
 */
class RegraDeDependenciaTest {

    private static final String RAIZ = "br.cesar.rotavital";

    static final List<String> CONTEXTOS = List.of("captacao", "estoque", "requisicao", "expedicao", "transporte", "rede");

    private static final JavaClasses CODIGO = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(RAIZ);

    @Test
    @DisplayName("domínio é Java puro: não importa Spring, JPA, HTTP nem as outras camadas")
    void dominioSemFramework() {
        classes().that().resideInAnyPackage("..negocio.dominio..", "..compartilhado.dominio..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java..", RAIZ + ".*.negocio.dominio..", RAIZ + ".compartilhado.dominio..")
                .because("o modelo de domínio não importa controller, repositório concreto nem classes do framework")
                .check(CODIGO);
    }

    @Test
    @DisplayName("aplicação usa as interfaces do domínio e nunca importa dados nem apresentação")
    void aplicacaoNaoConheceDados() {
        noClasses().that().resideInAPackage("..negocio.aplicacao..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..dados..", "..apresentacao..", "jakarta.persistence..", "org.springframework.web..")
                .because("a camada de dados implementa as portas do domínio (inversão de dependência)")
                .check(CODIGO);
    }

    @Test
    @DisplayName("apresentação não fala direto com a camada de dados")
    void apresentacaoNaoConheceDados() {
        noClasses().that().resideInAnyPackage("..apresentacao..", RAIZ + ".portal..")
                .should().dependOnClassesThat().resideInAPackage("..dados..")
                .check(CODIGO);
    }

    @Test
    @DisplayName("JPA só aparece na camada de dados")
    void jpaSoEmDados() {
        noClasses().that().resideOutsideOfPackage("..dados..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .check(CODIGO);
    }

    static List<String> contextos() {
        return CONTEXTOS;
    }

    @ParameterizedTest(name = "{0} só fala com os outros contextos pela camada de aplicação deles")
    @MethodSource("contextos")
    void contextoNaoAcessaInternosDeOutro(String contexto) {
        for (String outro : CONTEXTOS) {
            if (outro.equals(contexto)) {
                continue;
            }
            noClasses().that().resideInAPackage(RAIZ + "." + contexto + "..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            RAIZ + "." + outro + ".negocio.dominio..",
                            RAIZ + "." + outro + ".dados..",
                            RAIZ + "." + outro + ".apresentacao..")
                    .because("nenhum contexto acessa as tabelas ou o modelo interno de outro; "
                            + "a comunicação passa por interface pública (aplicação) ou por evento")
                    .allowEmptyShould(true)
                    .check(CODIGO);
        }
    }

    @Test
    @DisplayName("o portal (página e painel) só lê a camada de aplicação dos contextos")
    void portalSoUsaAplicacao() {
        for (String contexto : CONTEXTOS) {
            noClasses().that().resideInAPackage(RAIZ + ".portal..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            RAIZ + "." + contexto + ".negocio.dominio..", RAIZ + "." + contexto + ".dados..")
                    .check(CODIGO);
        }
    }

    @Test
    @DisplayName("o núcleo compartilhado não conhece nenhum contexto")
    void nucleoCompartilhadoIndependente() {
        noClasses().that().resideInAPackage(RAIZ + ".compartilhado..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        CONTEXTOS.stream().map(c -> RAIZ + "." + c + "..").toArray(String[]::new))
                .check(CODIGO);
    }

    @Test
    @DisplayName("não há ciclo de dependência entre os contextos")
    void semCiclos() {
        slices().matching(RAIZ + ".(*)..").should().beFreeOfCycles().check(CODIGO);
    }
}
