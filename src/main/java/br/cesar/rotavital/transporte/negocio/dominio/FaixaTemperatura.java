package br.cesar.rotavital.transporte.negocio.dominio;

/** Objeto de valor: faixa de temperatura permitida na caixa térmica, em °C. */
public record FaixaTemperatura(double minimaC, double maximaC) {

    /** Concentrado de hemácias: 2 a 6 °C durante o transporte. */
    public static final FaixaTemperatura HEMACIAS = new FaixaTemperatura(2.0, 6.0);

    public FaixaTemperatura {
        if (Double.isNaN(minimaC) || Double.isNaN(maximaC) || minimaC >= maximaC) {
            throw new IllegalArgumentException("Faixa de temperatura inválida: mínima deve ser menor que a máxima");
        }
    }

    public boolean contem(double temperaturaC) {
        return temperaturaC >= minimaC && temperaturaC <= maximaC;
    }
}
