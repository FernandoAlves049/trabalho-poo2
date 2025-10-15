package indicadores;

import model.Populacao;
import model.Regiao;

public class DensidadeDemografica implements Indicador {
    @Override
    public double calcular(Regiao r) {
        if (r == null) return 0;
        Populacao p = r.getUltimaPopulacao();
        if (p == null || r.getAreaKm2() <= 0) return 0;
        return (double) p.getHabitantes() / r.getAreaKm2();
    }

    @Override
    public String getNome() {
        return "Densidade Demográfica (hab/km²)";
    }
}
