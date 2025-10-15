package indicadores;

import model.Populacao;
import model.Regiao;

public class CrescimentoPopulacional implements Indicador {
    @Override
    public double calcular(Regiao r) {
        if (r == null) return 0;
        Populacao ultimo = r.getUltimaPopulacao();
        Populacao penultimo = r.getPenultimaPopulacao();
        if (ultimo == null || penultimo == null || penultimo.getHabitantes() == 0) return 0;
        double diff = (double) (ultimo.getHabitantes() - penultimo.getHabitantes());
        return (diff / penultimo.getHabitantes()) * 100.0; // percentual
    }

    @Override
    public String getNome() {
        return "Crescimento Populacional (%)";
    }
}
