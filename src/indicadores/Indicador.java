package indicadores;

import model.Regiao;

public interface Indicador {
    double calcular(Regiao r);
    String getNome();
}
