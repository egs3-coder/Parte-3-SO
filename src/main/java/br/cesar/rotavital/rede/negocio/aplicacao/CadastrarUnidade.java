package br.cesar.rotavital.rede.negocio.aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.cesar.rotavital.compartilhado.aplicacao.ConflitoException;
import br.cesar.rotavital.rede.negocio.dominio.CodigoUnidade;
import br.cesar.rotavital.rede.negocio.dominio.Endereco;
import br.cesar.rotavital.rede.negocio.dominio.UnidadeRepository;
import br.cesar.rotavital.rede.negocio.dominio.UnidadeSolicitante;

/** Caso de uso: cadastrar hospital ou unidade solicitante. */
@Service
public class CadastrarUnidade {

    public record NovaUnidade(String codigo, String nome, String cidade, String uf) {
    }

    private final UnidadeRepository unidades;

    public CadastrarUnidade(UnidadeRepository unidades) {
        this.unidades = unidades;
    }

    @Transactional
    public UnidadeResumo executar(NovaUnidade pedido) {
        CodigoUnidade codigo = new CodigoUnidade(pedido.codigo());
        if (unidades.buscar(codigo).isPresent()) {
            throw new ConflitoException("Já existe unidade com o código " + codigo);
        }
        UnidadeSolicitante unidade = UnidadeSolicitante.cadastrar(codigo, pedido.nome(),
                new Endereco(pedido.cidade(), pedido.uf()));
        unidades.salvar(unidade);
        return UnidadeResumo.de(unidade);
    }
}
