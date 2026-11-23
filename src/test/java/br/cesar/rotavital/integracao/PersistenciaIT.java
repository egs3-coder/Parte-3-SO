package br.cesar.rotavital.integracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import br.cesar.rotavital.TestcontainersConfiguration;
import br.cesar.rotavital.captacao.negocio.dominio.Cpf;
import br.cesar.rotavital.captacao.negocio.dominio.Doacao;
import br.cesar.rotavital.captacao.negocio.dominio.DoacaoRepository;
import br.cesar.rotavital.captacao.negocio.dominio.Doador;
import br.cesar.rotavital.captacao.negocio.dominio.DoadorRepository;
import br.cesar.rotavital.captacao.negocio.dominio.ResultadoTriagem;
import br.cesar.rotavital.captacao.negocio.dominio.Sexo;
import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.BolsaRepository;
import br.cesar.rotavital.estoque.negocio.dominio.StatusBolsa;
import br.cesar.rotavital.expedicao.negocio.dominio.GuiaRemessa;
import br.cesar.rotavital.expedicao.negocio.dominio.Remessa;
import br.cesar.rotavital.expedicao.negocio.dominio.RemessaRepository;
import br.cesar.rotavital.transporte.negocio.dominio.FaixaTemperatura;
import br.cesar.rotavital.transporte.negocio.dominio.LeituraTelemetria;
import br.cesar.rotavital.transporte.negocio.dominio.Viagem;
import br.cesar.rotavital.transporte.negocio.dominio.ViagemRepository;

/**
 * Integração com PostgreSQL real (Testcontainers + Flyway): cada adaptador da camada de dados grava e reconstrói
 * o seu agregado sem perder nada, no schema do seu contexto.
 */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration")
@Import(TestcontainersConfiguration.class)
class PersistenciaIT {

    static final LocalDate HOJE = LocalDate.of(2026, 11, 24);
    static final LocalDateTime AGORA = HOJE.atTime(19, 0);

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    DoadorRepository doadores;

    @Autowired
    DoacaoRepository doacoes;

    @Autowired
    BolsaRepository bolsas;

    @Autowired
    RemessaRepository remessas;

    @Autowired
    ViagemRepository viagens;

    @BeforeEach
    void limpar() {
        jdbc.execute("TRUNCATE captacao.doacao, captacao.doador, estoque.bolsa, expedicao.remessa_bolsa, "
                + "expedicao.remessa, transporte.leitura, transporte.viagem RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("Captação: doador e doação persistem; o histórico volta como datas e o número é sequencial")
    void captacao() {
        Doador doador = doadores.salvar(Doador.novo("Maria Sintética", new Cpf("52998224725"),
                LocalDate.of(1995, 3, 10), Sexo.FEMININO, 62.5, TipoSanguineo.O_NEGATIVO, false, HOJE));
        CodigoBolsa primeiro = doacoes.proximoNumero();
        doacoes.salvar(Doacao.registrar(doador, ResultadoTriagem.de(List.of()), primeiro, HOJE));
        doacoes.salvar(Doacao.registrar(doador, ResultadoTriagem.de(List.of()), doacoes.proximoNumero(),
                HOJE.minusDays(120)));

        assertThat(doadores.existeCpf(new Cpf("52998224725"))).isTrue();
        assertThat(doadores.buscar(doador.id())).get().extracting(Doador::nome).isEqualTo("Maria Sintética");
        assertThat(doacoes.datasDeColetaDoDoador(doador.id())).containsExactlyInAnyOrder(HOJE, HOJE.minusDays(120));
        assertThat(primeiro.valor()).matches("RV-\\d{6}");
    }

    @Test
    @DisplayName("Estoque: reserva grava status e requisição; a lista em estoque vem ordenada pela validade")
    void estoque() {
        Bolsa b1 = Bolsa.registrar(new CodigoBolsa("B-1"), Hemocomponente.CONCENTRADO_HEMACIAS,
                TipoSanguineo.A_POSITIVO, HOJE.minusDays(2));
        Bolsa b2 = Bolsa.registrar(new CodigoBolsa("B-2"), Hemocomponente.CONCENTRADO_HEMACIAS,
                TipoSanguineo.O_NEGATIVO, HOJE.minusDays(20));
        bolsas.salvar(b1);
        bolsas.salvar(b2);

        Bolsa travada = bolsas.buscarParaAtualizar(new CodigoBolsa("B-1")).orElseThrow();
        travada.reservar("REQ-001", AGORA);
        bolsas.salvar(travada);

        Bolsa lida = bolsas.buscar(new CodigoBolsa("B-1")).orElseThrow();
        assertThat(lida.status()).isEqualTo(StatusBolsa.RESERVADA);
        assertThat(lida.reservadaPara()).isEqualTo("REQ-001");
        assertThat(lida.validade()).isEqualTo(b1.validade());
        assertThat(bolsas.emEstoque()).extracting(b -> b.codigo().valor()).containsExactly("B-2", "B-1");
    }

    @Test
    @DisplayName("Expedição e Transporte: remessa com bolsas e viagem com leituras voltam inteiras")
    void expedicaoETransporte() {
        remessas.salvar(Remessa.montar("REQ-001", "HSA", List.of(new CodigoBolsa("B-1"), new CodigoBolsa("B-2")),
                AGORA));
        assertThat(remessas.buscar(new GuiaRemessa("GR-REQ-001"))).get()
                .extracting(Remessa::bolsas).asList().hasSize(2);

        Viagem viagem = Viagem.iniciar("REQ-001", "GR-REQ-001", FaixaTemperatura.HEMACIAS, AGORA);
        viagens.salvar(viagem);
        Viagem lida = viagens.buscar("VG-REQ-001").orElseThrow();
        lida.registrarLeitura(new LeituraTelemetria(AGORA.plusMinutes(1), 4.0));
        lida.registrarLeitura(new LeituraTelemetria(AGORA.plusMinutes(2), 9.0));
        viagens.salvar(lida);

        Viagem relida = viagens.buscar("VG-REQ-001").orElseThrow();
        assertThat(relida.leituras()).hasSize(2);
        assertThat(relida.excursoes()).isEqualTo(1);
    }
}
