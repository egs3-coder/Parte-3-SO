package br.cesar.rotavital.integracao;

import static br.cesar.rotavital.compartilhado.dominio.Hemocomponente.CONCENTRADO_HEMACIAS;
import static br.cesar.rotavital.compartilhado.dominio.Hemocomponente.PLASMA_FRESCO_CONGELADO;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.TestcontainersConfiguration;
import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;
import br.cesar.rotavital.requisicao.negocio.dominio.BolsaReservada;
import br.cesar.rotavital.requisicao.negocio.dominio.ItemId;
import br.cesar.rotavital.requisicao.negocio.dominio.ItemRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.JustificativaClinica;
import br.cesar.rotavital.requisicao.negocio.dominio.NovoItem;
import br.cesar.rotavital.requisicao.negocio.dominio.Prioridade;
import br.cesar.rotavital.requisicao.negocio.dominio.Quantidade;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.UnidadeSolicitanteId;

/** O adaptador JDBC grava e reconstrói o agregado Requisição inteiro (raiz, itens e alocações) no PostgreSQL. */
@SpringBootTest(properties = "spring.flyway.locations=classpath:db/migration")
@Import(TestcontainersConfiguration.class)
class RequisicaoRepositoryIT {

    static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 24, 19, 0);

    @Autowired
    RequisicaoRepository requisicoes;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limpar() {
        jdbc.execute("TRUNCATE requisicao.alocacao, requisicao.item, requisicao.requisicao CASCADE");
    }

    @Test
    @Transactional
    @DisplayName("grava e relê a requisição com itens e alocações; regravar não duplica nada")
    void idaEVolta() {
        RequisicaoId id = requisicoes.proximoId();
        Requisicao requisicao = Requisicao.registrar(id, new UnidadeSolicitanteId("HSA"), Prioridade.URGENCIA,
                new JustificativaClinica("Cirurgia cardíaca"), List.of(
                        new NovoItem(CONCENTRADO_HEMACIAS, TipoSanguineo.A_POSITIVO, new Quantidade(2)),
                        new NovoItem(PLASMA_FRESCO_CONGELADO, TipoSanguineo.A_POSITIVO, new Quantidade(1))),
                AGORA);
        requisicao.iniciarAtendimento();
        requisicao.alocar(new ItemId("ITEM-1"), new BolsaReservada(new CodigoBolsa("B-100"), CONCENTRADO_HEMACIAS,
                TipoSanguineo.O_NEGATIVO, new Validade(AGORA.plusDays(10))), AGORA);
        requisicoes.salvar(requisicao);
        requisicoes.salvar(requisicao);

        Requisicao lida = requisicoes.buscarParaAtualizar(id).orElseThrow();
        assertThat(lida.status()).isEqualTo(StatusRequisicao.EM_ATENDIMENTO);
        assertThat(lida.prioridade()).isEqualTo(Prioridade.URGENCIA);
        assertThat(lida.justificativa().texto()).isEqualTo("Cirurgia cardíaca");
        assertThat(lida.registradaEm()).isEqualTo(AGORA);
        assertThat(lida.itens()).extracting(ItemRequisicao::quantidadeAlocada).containsExactly(1, 0);
        assertThat(lida.bolsasAlocadas()).containsExactly(new CodigoBolsa("B-100"));
        assertThat(lida.item(new ItemId("ITEM-1")).orElseThrow().alocacoes().get(0).validade())
                .isEqualTo(new Validade(AGORA.plusDays(10)));

        lida.desalocar(new ItemId("ITEM-1"), new CodigoBolsa("B-100"));
        requisicoes.salvar(lida);
        assertThat(requisicoes.buscar(id).orElseThrow().bolsasAlocadas()).isEmpty();
        assertThat(requisicoes.todas()).hasSize(1);
    }
}
