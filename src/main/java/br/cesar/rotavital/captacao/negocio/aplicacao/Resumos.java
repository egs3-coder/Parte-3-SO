package br.cesar.rotavital.captacao.negocio.aplicacao;

import java.time.LocalDate;
import java.util.List;

import br.cesar.rotavital.captacao.negocio.dominio.Doacao;
import br.cesar.rotavital.captacao.negocio.dominio.Doador;
import br.cesar.rotavital.captacao.negocio.dominio.ResultadoTriagem;
import br.cesar.rotavital.captacao.negocio.dominio.Sexo;

/** Dados que os casos de uso da Captação devolvem para fora (apresentação e outros contextos). */
public final class Resumos {

    private Resumos() {
    }

    public record NovoDoador(String nome, String cpf, LocalDate dataNascimento, Sexo sexo, double pesoKg,
                             String tipoSanguineo, boolean consentimentoResponsavel) {
    }

    public record DoadorResumo(Long id, String nome, String cpf, LocalDate dataNascimento, Sexo sexo,
                               double pesoKg, String tipoSanguineo, boolean consentimentoResponsavel) {

        static DoadorResumo de(Doador d) {
            return new DoadorResumo(d.id(), d.nome(), d.cpf().numero(), d.dataNascimento(), d.sexo(), d.pesoKg(),
                    d.tipoSanguineo().simbolo(), d.consentimentoResponsavel());
        }
    }

    public record TriagemResumo(boolean apto, List<String> motivos) {

        static TriagemResumo de(ResultadoTriagem r) {
            return new TriagemResumo(r.apto(), r.motivos());
        }
    }

    public record DoacaoResumo(Long doacaoId, Long doadorId, String codigoBolsa, String tipoSanguineo,
                               LocalDate dataColeta) {

        static DoacaoResumo de(Doacao d) {
            return new DoacaoResumo(d.id(), d.doadorId(), d.numero().valor(), d.tipoSanguineo().simbolo(),
                    d.dataColeta());
        }
    }
}
