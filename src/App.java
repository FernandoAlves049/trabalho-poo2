import exceptions.ArquivoInvalidoException;
import indicadores.CrescimentoPopulacional;
import indicadores.DensidadeDemografica;
import indicadores.Indicador;
import model.Populacao;
import model.Regiao;
import service.Entrada;

import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        // se passado --gui, abre interface Swing mais fácil de usar
        if (args != null && args.length > 0 && "--gui".equalsIgnoreCase(args[0])) {
            ui.GuiApp.main(args);
            return;
        }

        String path = Paths.get("data", "populacao.csv").toString();

        try {
            List<Regiao> regioes = Entrada.lerArquivo(path);

            // converter para ArrayList (já foi lido como ArrayList internamente) e ordenar
            List<Regiao> lista = new ArrayList<>(regioes);

            // Ordenar por tipo (Estado/Municipio) e depois por nome
            lista.sort(Comparator.comparing(Regiao::getTipo).thenComparing(Regiao::getNome));

            // Agrupar por tipo usando HashMap
            Map<String, List<Regiao>> agrupado = new HashMap<>();
            for (Regiao r : lista) {
                agrupado.computeIfAbsent(r.getTipo(), k -> new ArrayList<>()).add(r);
            }

            // Indicadores
            Indicador dens = new DensidadeDemografica();
            Indicador crec = new CrescimentoPopulacional();

            // Exibir resumo estilizado
            NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("pt-BR"));
            nf.setMaximumFractionDigits(2);

            System.out.println("\n========================================");
            System.out.println(" Resumo das Regiões lidas (formato tabular)");
            System.out.println("========================================\n");

            // Cabeçalho da tabela
            String fmtHeader = "%-8s | %-10s | %-30s | %12s | %15s | %12s | %10s";
            String fmtRow = "%-8s | %-10s | %-30s | %12s | %15s | %12s | %10s";
            System.out.println(String.format(fmtHeader, "CÓD", "TIPO", "NOME", "ÁREA (km²)", "ÚLTIMA POP.", "DENS.(hab)", "CRESC.(%)"));
            int totalWidth = 8 + 3 + 10 + 3 + 30 + 3 + 12 + 3 + 15 + 3 + 12 + 3 + 10;
            System.out.println(new String(new char[totalWidth]).replace('\0', '-'));

            for (Regiao r : lista) {
                Populacao ultimo = r.getUltimaPopulacao();
                String lastPop = (ultimo == null) ? "n/d" : nf.format(ultimo.getHabitantes());
                String areaFmt = nf.format(r.getAreaKm2());
                String densFmt = nf.format(dens.calcular(r));
                String crecFmt = nf.format(crec.calcular(r));
                System.out.println(String.format(fmtRow, r.getCodigo(), r.getTipo(), abbreviate(r.getNome(), 30), areaFmt, lastPop, densFmt, crecFmt));
            }

            // Ordenação por maior população (último ano)
            List<Regiao> porPop = lista.stream()
                    .sorted((a, b) -> {
                        long pa = a.getUltimaPopulacao() == null ? 0 : a.getUltimaPopulacao().getHabitantes();
                        long pb = b.getUltimaPopulacao() == null ? 0 : b.getUltimaPopulacao().getHabitantes();
                        return Long.compare(pb, pa);
                    })
                    .collect(Collectors.toList());

            System.out.println();
            System.out.println("===== TOP 5 (Último ano) =====");
            for (int i = 0; i < Math.min(5, porPop.size()); i++) {
                Regiao r = porPop.get(i);
                Populacao p = r.getUltimaPopulacao();
                String popFmt = p == null ? "n/d" : nf.format(p.getHabitantes());
                System.out.printf("%d) %s (%s) - %s habitantes%n", i + 1, abbreviate(r.getNome(), 30), r.getTipo(), popFmt);
            }

            // Persistência em banco foi removida conforme solicitado; o programa apenas lê
            // o CSV, calcula e exibe os indicadores.

        } catch (ArquivoInvalidoException e) {
            System.err.println("Erro ao processar arquivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCli() {
        String path = Paths.get("data", "populacao.csv").toString();

        try {
            List<Regiao> regioes = Entrada.lerArquivo(path);

            // converter para ArrayList (já foi lido como ArrayList internamente) e ordenar
            List<Regiao> lista = new ArrayList<>(regioes);

            // Ordenar por tipo (Estado/Municipio) e depois por nome
            lista.sort(Comparator.comparing(Regiao::getTipo).thenComparing(Regiao::getNome));

            // Agrupar por tipo usando HashMap
            Map<String, List<Regiao>> agrupado = new HashMap<>();
            for (Regiao r : lista) {
                agrupado.computeIfAbsent(r.getTipo(), k -> new ArrayList<>()).add(r);
            }

            // Indicadores
            Indicador dens = new DensidadeDemografica();
            Indicador crec = new CrescimentoPopulacional();

            // Exibir resumo
            System.out.println("Resumo das Regiões lidas: \n");
            for (String tipo : agrupado.keySet()) {
                System.out.println("== " + tipo + " ==");
                for (Regiao r : agrupado.get(tipo)) {
                    Populacao ultimo = r.getUltimaPopulacao();
                    String hab = (ultimo == null) ? "n/d" : String.valueOf(ultimo.getHabitantes());
                    System.out.printf("%s - %s | area=%.2f km2 | ultima pop=%s\n", r.getCodigo(), r.getNome(), r.getAreaKm2(), hab);
                    System.out.printf("  - %s: %.2f\n", dens.getNome(), dens.calcular(r));
                    System.out.printf("  - %s: %.2f\n", crec.getNome(), crec.calcular(r));
                }
                System.out.println();
            }

            // Exemplo de ordenação por maior população (último ano)
            lista.sort((a, b) -> {
                long pa = a.getUltimaPopulacao() == null ? 0 : a.getUltimaPopulacao().getHabitantes();
                long pb = b.getUltimaPopulacao() == null ? 0 : b.getUltimaPopulacao().getHabitantes();
                return Long.compare(pb, pa); // decrescente
            });

            System.out.println("Top 5 regiões por população (último ano):");
            for (int i = 0; i < Math.min(5, lista.size()); i++) {
                Regiao r = lista.get(i);
                Populacao p = r.getUltimaPopulacao();
                System.out.printf("%d. %s (%s) - %s habitantes\n", i + 1, r.getNome(), r.getTipo(), p == null ? "n/d" : String.valueOf(p.getHabitantes()));
            }

        } catch (ArquivoInvalidoException e) {
            System.err.println("Erro ao processar arquivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String abbreviate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }
}
