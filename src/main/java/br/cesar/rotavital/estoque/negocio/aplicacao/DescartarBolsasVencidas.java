package br.cesar.rotavital.estoque.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.BolsaRepository;
import br.cesar.rotavital.estoque.negocio.dominio.DescarteBolsas;

/** T4.2: descarta as bolsas disponíveis que venceram e devolve os códigos. */
@Service
public class DescartarBolsasVencidas {

    private final BolsaRepository bolsas;
    private final Clock clock;
    private final DescarteBolsas descarte = new DescarteBolsas();

    public DescartarBolsasVencidas(BolsaRepository bolsas, Clock clock) {
        this.bolsas = bolsas;
        this.clock = clock;
    }

    @Transactional
    public List<String> executar() {
        List<Bolsa> vencidas = descarte.paraDescartar(bolsas.emEstoque(), LocalDateTime.now(clock));
        vencidas.forEach(bolsa -> {
            bolsa.descartar();
            bolsas.salvar(bolsa);
        });
        return vencidas.stream().map(b -> b.codigo().valor()).toList();
    }
}
