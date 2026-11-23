package br.cesar.rotavital.estoque.negocio.aplicacao;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.aplicacao.ConflitoException;
import br.cesar.rotavital.compartilhado.dominio.CodigoBolsa;
import br.cesar.rotavital.compartilhado.dominio.Hemocomponente;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;
import br.cesar.rotavital.estoque.negocio.dominio.Bolsa;
import br.cesar.rotavital.estoque.negocio.dominio.BolsaRepository;

/** Caso de uso: dar entrada de uma bolsa no estoque (pela doação ou pelo cadastro direto do laboratório). */
@Service
public class RegistrarBolsa {

    public record NovaBolsa(String codigo, Hemocomponente hemocomponente, String tipoSanguineo, LocalDate dataColeta) {
    }

    private final BolsaRepository bolsas;

    public RegistrarBolsa(BolsaRepository bolsas) {
        this.bolsas = bolsas;
    }

    @Transactional
    public BolsaResumo executar(NovaBolsa pedido) {
        CodigoBolsa codigo = new CodigoBolsa(pedido.codigo());
        if (bolsas.existe(codigo)) {
            throw new ConflitoException("Já existe bolsa com o código " + codigo);
        }
        Bolsa bolsa = Bolsa.registrar(codigo, pedido.hemocomponente(),
                TipoSanguineo.deSimbolo(pedido.tipoSanguineo()), pedido.dataColeta());
        bolsas.salvar(bolsa);
        return BolsaResumo.de(bolsa);
    }
}
