package br.cesar.rotavital.captacao.negocio.aplicacao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.captacao.negocio.aplicacao.Resumos.DoadorResumo;
import br.cesar.rotavital.captacao.negocio.dominio.DoacaoRepository;
import br.cesar.rotavital.captacao.negocio.dominio.DoadorRepository;
import br.cesar.rotavital.compartilhado.aplicacao.NaoEncontradoException;

/** Consultas da Captação (interface pública de leitura, usada também pelo painel). */
@Service
@Transactional(readOnly = true)
public class ConsultarDoadores {

    private final DoadorRepository doadores;
    private final DoacaoRepository doacoes;

    public ConsultarDoadores(DoadorRepository doadores, DoacaoRepository doacoes) {
        this.doadores = doadores;
        this.doacoes = doacoes;
    }

    public DoadorResumo buscar(Long id) {
        return doadores.buscar(id).map(DoadorResumo::de)
                .orElseThrow(() -> new NaoEncontradoException("Doador " + id + " não encontrado"));
    }

    public List<DoadorResumo> recentes(int limite) {
        return doadores.recentes(limite).stream().map(DoadorResumo::de).toList();
    }

    public long totalDeDoadores() {
        return doadores.contar();
    }

    public long totalDeDoacoes() {
        return doacoes.contar();
    }
}
