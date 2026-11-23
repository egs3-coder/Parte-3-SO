package br.cesar.rotavital.captacao.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.DoadorResumo;
import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.NovoDoador;
import br.cesar.rotavital.captacao.negocio.dominio.Cpf;
import br.cesar.rotavital.captacao.negocio.dominio.Doador;
import br.cesar.rotavital.captacao.negocio.dominio.DoadorRepository;
import br.cesar.rotavital.compartilhado.aplicacao.ConflitoException;
import br.cesar.rotavital.compartilhado.dominio.TipoSanguineo;

/** Caso de uso: cadastrar doador. CPF inválido não passa do objeto de valor; CPF repetido vira conflito. */
@Service
public class CadastrarDoador {

    private final DoadorRepository doadores;
    private final Clock clock;

    public CadastrarDoador(DoadorRepository doadores, Clock clock) {
        this.doadores = doadores;
        this.clock = clock;
    }

    @Transactional
    public DoadorResumo executar(NovoDoador pedido) {
        Cpf cpf = new Cpf(pedido.cpf());
        if (doadores.existeCpf(cpf)) {
            throw new ConflitoException("Já existe doador cadastrado com o CPF " + cpf);
        }
        Doador novo = Doador.novo(pedido.nome(), cpf, pedido.dataNascimento(), pedido.sexo(), pedido.pesoKg(),
                TipoSanguineo.deSimbolo(pedido.tipoSanguineo()), pedido.consentimentoResponsavel(),
                LocalDate.now(clock));
        return DoadorResumo.de(doadores.salvar(novo));
    }
}
