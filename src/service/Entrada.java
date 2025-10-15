package service;

import exceptions.ArquivoInvalidoException;
import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entrada {
    /**
     * Espera um CSV com header. Formato exemplo:
     * tipo,codigo,nome,area_km2,ano,habitantes,uf,codigo_estado
     * tipo = ESTADO ou MUNICIPIO
     */
    public static List<Regiao> lerArquivo(String path) throws ArquivoInvalidoException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            if (lines.isEmpty()) throw new ArquivoInvalidoException("Arquivo vazio: " + path);
            String header = lines.get(0);
            String[] cols = header.split(",");
            if (cols.length < 6) throw new ArquivoInvalidoException("Header inválido: " + header);

            // ler linhas em array
            List<String> dataLines = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) dataLines.add(lines.get(i));
            String[] arr = dataLines.toArray(new String[0]); // array conforme requisito

            List<Regiao> regioes = new ArrayList<>();
            Map<String, Estado> estadosByCodigo = new HashMap<>();

            for (String ln : arr) {
                if (ln == null || ln.trim().isEmpty()) continue;
                String[] f = ln.split(",");
                if (f.length < 6) throw new ArquivoInvalidoException("Linha inválida: " + ln);
                String tipo = f[0].trim();
                String codigo = f[1].trim();
                String nome = f[2].trim();
                double area = Double.parseDouble(f[3].trim());
                int ano = Integer.parseInt(f[4].trim());
                long hab = Long.parseLong(f[5].trim());

                if (tipo.equalsIgnoreCase("ESTADO")) {
                    String uf = f.length > 6 ? f[6].trim() : "";
                    Estado e = estadosByCodigo.get(codigo);
                    if (e == null) {
                        e = new Estado(codigo, nome, area, uf);
                        estadosByCodigo.put(codigo, e);
                        regioes.add(e);
                    }
                    e.addPopulacao(new Populacao(ano, hab));
                } else if (tipo.equalsIgnoreCase("MUNICIPIO")) {
                    String codigoEstado = f.length > 7 ? f[7].trim() : (f.length > 6 ? f[6].trim() : "");
                    Municipio m = new Municipio(codigo, nome, area, codigoEstado);
                    m.addPopulacao(new Populacao(ano, hab));
                    regioes.add(m);
                    // ligação será feita depois caso estado ainda não exista
                } else {
                    throw new ArquivoInvalidoException("Tipo desconhecido: " + tipo);
                }
            }

            // associar municipios a estados por codigo
            for (Regiao r : regioes) {
                if (r instanceof Municipio) {
                    Municipio m = (Municipio) r;
                    String codigoEstado = m.getCodigoEstado();
                    Estado found = estadosByCodigo.get(codigoEstado);
                    if (found != null) {
                        found.addMunicipio(m);
                    }
                }
            }

            return regioes;
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Erro ao ler arquivo: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new ArquivoInvalidoException("Número em formato inválido: " + e.getMessage(), e);
        }
    }
}
