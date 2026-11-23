package br.cesar.rotavital.bdd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.RegraDeNegocioException;
import br.cesar.rotavital.requisicao.negocio.dominio.BolsaReservada;
import br.cesar.rotavital.requisicao.negocio.dominio.EstoqueGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.RedeGateway;
import br.cesar.rotavital.requisicao.negocio.dominio.Requisicao;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoId;
import br.cesar.rotavital.requisicao.negocio.dominio.RequisicaoRepository;
import br.cesar.rotavital.requisicao.negocio.dominio.UnidadeSolicitanteId;

/**
 * Implementações em memória das portas do domínio da Requisição, só para os testes BDD sem banco.
 * Mostram o ganho da inversão de dependência: o mesmo caso de uso roda com JDBC na aplicação e com mapas aqui.
 */
final class AdaptadoresEmMemoria {

    private AdaptadoresEmMemoria() {
    }

    static final class Requisicoes implements RequisicaoRepository {

        private final Map<RequisicaoId, Requisicao> porId = new LinkedHashMap<>();
        private long sequencia;

        @Override
        public RequisicaoId proximoId() {
            return RequisicaoId.doNumero(++sequencia);
        }

        @Override
        public void salvar(Requisicao requisicao) {
            porId.put(requisicao.id(), requisicao);
        }

        @Override
        public Optional<Requisicao> buscar(RequisicaoId id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public Optional<Requisicao> buscarParaAtualizar(RequisicaoId id) {
            return buscar(id);
        }

        @Override
        public List<Requisicao> todas() {
            return new ArrayList<>(porId.values());
        }
    }

    /** Estoque falso: guarda as bolsas e as reservas, com as mesmas recusas do Estoque real. */
    static final class Estoque implements EstoqueGateway {

        private final Map<CodigoBolsa, BolsaReservada> bolsas = new HashMap<>();
        private final Map<CodigoBolsa, RequisicaoId> reservas = new HashMap<>();

        void adicionar(BolsaReservada bolsa) {
            bolsas.put(bolsa.codigo(), bolsa);
        }

        /** Bolsas com reserva ativa, para conferir que uma recusa não deixa reserva para trás. */
        java.util.Set<String> reservadas() {
            return reservas.keySet().stream().map(CodigoBolsa::valor).collect(java.util.stream.Collectors.toSet());
        }

        @Override
        public BolsaReservada reservar(CodigoBolsa codigo, RequisicaoId requisicao) {
            BolsaReservada bolsa = bolsas.get(codigo);
            if (bolsa == null) {
                throw new RegraDeNegocioException("Bolsa " + codigo + " não encontrada");
            }
            if (reservas.containsKey(codigo)) {
                throw new RegraDeNegocioException("Bolsa já reservada");
            }
            reservas.put(codigo, requisicao);
            return bolsa;
        }

        @Override
        public void liberar(CodigoBolsa codigo, RequisicaoId requisicao) {
            reservas.remove(codigo, requisicao);
        }
    }

    /** Rede falsa: unidades cadastradas pelo nome, com um código gerado. */
    static final class Rede implements RedeGateway {

        private final Map<String, UnidadeSolicitanteId> porNome = new HashMap<>();

        UnidadeSolicitanteId cadastrar(String nome) {
            return porNome.computeIfAbsent(nome, n -> new UnidadeSolicitanteId("UN-" + (porNome.size() + 1)));
        }

        @Override
        public boolean unidadeAtiva(UnidadeSolicitanteId unidade) {
            return porNome.containsValue(unidade);
        }
    }
}
