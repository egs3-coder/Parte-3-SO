package br.cesar.rotavital.captacao.negocio.dominio;

/**
 * Objeto de valor: CPF do doador. Imutável e validado no construtor (T2.5 da Parte 1):
 * 11 dígitos e os dois dígitos verificadores corretos. Um Cpf inválido não chega a existir.
 */
public record Cpf(String numero) {

    public Cpf {
        if (numero == null || !numero.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos");
        }
        // 111.111.111-11, 222.222.222-22... passam na conta, mas não são CPFs reais
        if (numero.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF inválido: " + numero);
        }
        if (digitoVerificador(numero, 9) != digito(numero, 9) || digitoVerificador(numero, 10) != digito(numero, 10)) {
            throw new IllegalArgumentException("CPF inválido (dígitos verificadores não conferem): " + numero);
        }
    }

    /** Conveniência para validar sem lançar exceção (ex.: formulário). */
    public static boolean valido(String numero) {
        try {
            new Cpf(numero);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Soma cada dígito multiplicado por um peso decrescente; o resto da divisão por 11 define o dígito. */
    private static int digitoVerificador(String cpf, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;
        for (int i = 0; i < quantidade; i++) {
            soma += digito(cpf, i) * peso;
            peso--;
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }

    private static int digito(String cpf, int posicao) {
        return cpf.charAt(posicao) - '0';
    }

    @Override
    public String toString() {
        return numero;
    }
}
