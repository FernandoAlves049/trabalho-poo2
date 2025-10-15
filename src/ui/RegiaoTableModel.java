package ui;

import model.Populacao;
import model.Regiao;
import indicadores.DensidadeDemografica;
import indicadores.CrescimentoPopulacional;
import indicadores.Indicador;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RegiaoTableModel extends AbstractTableModel {
    private final String[] cols = {"Tipo", "Código", "Nome", "Área (km²)", "Última Pop.", "Densidade", "Crescimento (%)"};
    private List<Regiao> data = new ArrayList<>();
    private final Indicador dens = new DensidadeDemografica();
    private final Indicador cresc = new CrescimentoPopulacional();

    public void setData(List<Regiao> regs) {
        this.data = regs == null ? new ArrayList<>() : regs;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Regiao r = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return r.getTipo();
            case 1: return r.getCodigo();
            case 2: return r.getNome();
            case 3: return String.format("%.2f", r.getAreaKm2());
            case 4: {
                Populacao p = r.getUltimaPopulacao();
                return p == null ? "n/d" : String.valueOf(p.getHabitantes());
            }
            case 5: return String.format("%.2f", dens.calcular(r));
            case 6: return String.format("%.2f", cresc.calcular(r));
            default: return "";
        }
    }
}
