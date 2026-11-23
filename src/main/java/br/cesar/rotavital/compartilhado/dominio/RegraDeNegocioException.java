package br.cesar.rotavital.compartilhado.dominio;

import java.util.List;

/**
 * Uma operação foi recusada porque quebraria uma regra (invariante) do domínio.
 * A mensagem é a da linguagem ubíqua e é mostrada como está para o usuário;
 * {@link #motivos()} traz os detalhes quando há mais de um (ex.: todos os motivos de inaptidão da triagem).
 */
public class RegraDeNegocioException extends RuntimeException {

    private final List<String> motivos;

    public RegraDeNegocioException(String mensagem) {
        this(mensagem, List.of());
    }

    public RegraDeNegocioException(String mensagem, List<String> motivos) {
        super(mensagem);
        this.motivos = List.copyOf(motivos);
    }

    public List<String> motivos() {
        return motivos;
    }
}
