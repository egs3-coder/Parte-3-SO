package br.cesar.rotavital.estoque.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.estoque.negocio.dominio.AlertaEstoque;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.BolsaRepository;

/** Consultas do Estoque: interface pública de leitura, usada pela apresentação e pela Requisição. */
@Service
@Transactional(readOnly = true)
public class ConsultarEstoque {

    private final BolsaRepository bolsas;
    private final Clock clock;
    private final AlertaEstoque alerta = new AlertaEstoque();

    public ConsultarEstoque(BolsaRepository bolsas, Clock clock) {
        this.bolsas = bolsas;
        this.clock = clock;
    }

    /** Bolsas utilizáveis (disponíveis e na validade) por tipo sanguíneo, com todos os tipos listados. */
    public Map<String, Integer> utilizaveisPorTipo(Hemocomponente hemocomponente) {
        Map<String, Integer> porTipo = new LinkedHashMap<>();
        contarUtilizaveis(hemocomponente).forEach((tipo, qtd) -> porTipo.put(tipo.simbolo(), qtd));
        return porTipo;
    }

    /** T4.1: tipos com concentrado de hemácias abaixo do mínimo de segurança. */
    public List<String> tiposCriticos() {
        return alerta.tiposCriticos(contarUtilizaveis(Hemocomponente.CONCENTRADO_HEMACIAS)).stream()
                .map(TipoSanguineo::simbolo)
                .toList();
    }

    /** Bolsas que podem ser oferecidas a uma requisição agora (da que vence antes para a que vence depois). */
    public List<BolsaResumo> utilizaveis() {
        LocalDateTime agora = LocalDateTime.now(clock);
        return bolsas.emEstoque().stream().filter(b -> b.utilizavelEm(agora)).map(BolsaResumo::de).toList();
    }

    /** Todas as bolsas não descartadas, inclusive reservadas e vencidas. */
    public List<BolsaResumo> emEstoque() {
        return bolsas.emEstoque().stream().map(BolsaResumo::de).toList();
    }

    public Optional<BolsaResumo> buscar(String codigo) {
        return bolsas.buscar(new CodigoBolsa(codigo)).map(BolsaResumo::de);
    }

    private Map<TipoSanguineo, Integer> contarUtilizaveis(Hemocomponente hemocomponente) {
        LocalDateTime agora = LocalDateTime.now(clock);
        Map<TipoSanguineo, Integer> porTipo = new EnumMap<>(TipoSanguineo.class);
        for (TipoSanguineo tipo : TipoSanguineo.values()) {
            porTipo.put(tipo, 0);
        }
        for (Bolsa b : bolsas.emEstoque()) {
            if (b.hemocomponente() == hemocomponente && b.utilizavelEm(agora)) {
                porTipo.merge(b.tipoSanguineo(), 1, Integer::sum);
            }
        }
        return porTipo;
    }
}
