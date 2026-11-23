package br.cesar.rotavital.requisicao.dados;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.compartilhado.dominio.Validade;
import br.cesar.rotavital.requisicao.negocio.dominio.AlocacaoBolsa;
import br.cesar.rotavital.requisicao.negocio.dominio.ItemId;
import br.cesar.rotavital.requisicao.negocio.dominio.ItemRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.JustificativaClinica;
import br.cesar.rotavital.requisicao.negocio.dominio.Prioridade;
import br.cesar.rotavital.requisicao.negocio.dominio.Quantidade;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;
import br.cesar.rotavital.requisicao.negocio.dominio.StatusRequisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.UnidadeSolicitanteId;

/**
 * Adaptador da porta {@link RequisicaoRepository} com SQL explícito (Spring JdbcClient), nas tabelas do schema
 * {@code requisicao}. O agregado é gravado inteiro: a linha da raiz, os itens e as alocações.
 *
 * <p>JDBC em vez de JPA porque o agregado tem duas coleções aninhadas (itens → alocações); com SQL fica claro
 * o que é gravado, e o domínio continua sem nenhuma anotação.
 */
@Repository
class RequisicaoRepositoryJdbc implements RequisicaoRepository {

    private final JdbcClient jdbc;

    RequisicaoRepositoryJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public RequisicaoId proximoId() {
        Long numero = jdbc.sql("SELECT nextval('requisicao.requisicao_seq')").query(Long.class).single();
        return RequisicaoId.doNumero(numero);
    }

    @Override
    public void salvar(Requisicao r) {
        jdbc.sql("""
                INSERT INTO requisicao.requisicao (id, unidade_solicitante, prioridade, justificativa, registrada_em,
                                                   status, guia_remessa, viagem_id, motivo_cancelamento)
                VALUES (:id, :unidade, :prioridade, :justificativa, :registradaEm, :status, :guia, :viagem, :motivo)
                ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status,
                                               guia_remessa = EXCLUDED.guia_remessa,
                                               viagem_id = EXCLUDED.viagem_id,
                                               motivo_cancelamento = EXCLUDED.motivo_cancelamento
                """)
                .param("id", r.id().valor())
                .param("unidade", r.solicitante().codigo())
                .param("prioridade", r.prioridade().name())
                .param("justificativa", r.justificativa().texto())
                .param("registradaEm", r.registradaEm())
                .param("status", r.status().name())
                .param("guia", r.guiaRemessa())
                .param("viagem", r.viagemId())
                .param("motivo", r.motivoCancelamento())
                .update();

        // Itens não mudam depois do registro; alocações são regravadas (a raiz é a dona delas).
        jdbc.sql("DELETE FROM requisicao.alocacao WHERE requisicao_id = :id").param("id", r.id().valor()).update();
        for (ItemRequisicao item : r.itens()) {
            jdbc.sql("""
                    INSERT INTO requisicao.item (requisicao_id, item_id, hemocomponente, tipo_receptor, quantidade)
                    VALUES (:req, :item, :hemocomponente, :receptor, :quantidade)
                    ON CONFLICT (requisicao_id, item_id) DO NOTHING
                    """)
                    .param("req", r.id().valor())
                    .param("item", item.id().valor())
                    .param("hemocomponente", item.hemocomponente().name())
                    .param("receptor", item.tipoReceptor().name())
                    .param("quantidade", item.quantidade().valor())
                    .update();
            for (AlocacaoBolsa a : item.alocacoes()) {
                jdbc.sql("""
                        INSERT INTO requisicao.alocacao (requisicao_id, item_id, codigo_bolsa, hemocomponente,
                                                         tipo_sanguineo, validade, alocada_em)
                        VALUES (:req, :item, :codigo, :hemocomponente, :tipo, :validade, :alocadaEm)
                        """)
                        .param("req", r.id().valor())
                        .param("item", item.id().valor())
                        .param("codigo", a.codigo().valor())
                        .param("hemocomponente", a.hemocomponente().name())
                        .param("tipo", a.tipoSanguineo().name())
                        .param("validade", a.validade().limite())
                        .param("alocadaEm", a.alocadaEm())
                        .update();
            }
        }
    }

    @Override
    public Optional<Requisicao> buscar(RequisicaoId id) {
        return carregar(id, "");
    }

    @Override
    public Optional<Requisicao> buscarParaAtualizar(RequisicaoId id) {
        return carregar(id, " FOR UPDATE");
    }

    @Override
    public List<Requisicao> todas() {
        return jdbc.sql("SELECT id FROM requisicao.requisicao ORDER BY registrada_em DESC, id DESC")
                .query(String.class).list().stream()
                .map(id -> carregar(new RequisicaoId(id), "").orElseThrow())
                .toList();
    }

    // ---------- leitura do agregado ----------

    private record Linha(String unidade, String prioridade, String justificativa, LocalDateTime registradaEm,
                         String status, String guia, String viagem, String motivo) {
    }

    private Optional<Requisicao> carregar(RequisicaoId id, String trava) {
        Optional<Linha> linha = jdbc.sql("""
                SELECT unidade_solicitante, prioridade, justificativa, registrada_em, status, guia_remessa,
                       viagem_id, motivo_cancelamento
                FROM requisicao.requisicao WHERE id = :id""" + trava)
                .param("id", id.valor())
                .query((rs, n) -> new Linha(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getObject(4, LocalDateTime.class), rs.getString(5), rs.getString(6), rs.getString(7),
                        rs.getString(8)))
                .optional();
        return linha.map(l -> Requisicao.reconstituir(id, new UnidadeSolicitanteId(l.unidade()),
                Prioridade.valueOf(l.prioridade()), new JustificativaClinica(l.justificativa()), l.registradaEm(),
                itens(id), StatusRequisicao.valueOf(l.status()), l.guia(), l.viagem(), l.motivo()));
    }

    private List<ItemRequisicao> itens(RequisicaoId id) {
        Map<String, List<AlocacaoBolsa>> alocacoesPorItem = jdbc.sql("""
                SELECT item_id, codigo_bolsa, hemocomponente, tipo_sanguineo, validade, alocada_em
                FROM requisicao.alocacao WHERE requisicao_id = :id ORDER BY alocada_em, codigo_bolsa""")
                .param("id", id.valor())
                .query((rs, n) -> Map.entry(rs.getString("item_id"), alocacao(rs)))
                .list().stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        return jdbc.sql("""
                SELECT item_id, hemocomponente, tipo_receptor, quantidade
                FROM requisicao.item WHERE requisicao_id = :id ORDER BY length(item_id), item_id""")
                .param("id", id.valor())
                .query((rs, n) -> ItemRequisicao.reconstituir(new ItemId(rs.getString("item_id")),
                        Hemocomponente.valueOf(rs.getString("hemocomponente")),
                        TipoSanguineo.valueOf(rs.getString("tipo_receptor")),
                        new Quantidade(rs.getInt("quantidade")),
                        alocacoesPorItem.getOrDefault(rs.getString("item_id"), List.of())))
                .list();
    }

    private static AlocacaoBolsa alocacao(ResultSet rs) throws SQLException {
        return AlocacaoBolsa.reconstituir(new CodigoBolsa(rs.getString("codigo_bolsa")),
                Hemocomponente.valueOf(rs.getString("hemocomponente")),
                TipoSanguineo.valueOf(rs.getString("tipo_sanguineo")),
                new Validade(rs.getObject("validade", LocalDateTime.class)),
                rs.getObject("alocada_em", LocalDateTime.class));
    }
}
