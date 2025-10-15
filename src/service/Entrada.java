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

            // detectar delimitador: ';' ou ',' (prefere ';' se presente)
            String firstLine = lines.get(0).trim();
            String delimiter = firstLine.contains(";") ? ";" : ",";

            // detectar se existe header: header costuma conter a palavra 'tipo' ou 'codigo'
            String[] firstTokens = firstLine.split(delimiter);
            boolean hasHeader = false;
            if (firstTokens.length > 0) {
                String t0 = firstTokens[0].toLowerCase();
                if (t0.contains("tipo") || t0.contains("codigo") || t0.contains("ano") || t0.contains("habitantes")) {
                    hasHeader = true;
                }
            }

            // preparar linhas de dados (array conforme requisito)
            List<String> dataLines = new ArrayList<>();
            for (int i = hasHeader ? 1 : 0; i < lines.size(); i++) {
                String ln = lines.get(i);
                if (ln == null || ln.trim().isEmpty()) continue;
                dataLines.add(ln.trim());
            }
            String[] arr = dataLines.toArray(new String[0]);

            List<Regiao> regioes = new ArrayList<>();
            Map<String, Estado> estadosByCodigo = new HashMap<>();

            for (String ln : arr) {
                String[] f = ln.split(delimiter);
                if (f.length == 0) continue;
                String tipo = f[0].trim();

                if (tipo.equalsIgnoreCase("ESTADO")) {
                    // formatos possíveis:
                    // ESTADO;codigo;nome;uf
                    // ou ESTADO;codigo;nome;area;ano;habitantes;uf  (menos comum)
                    String codigo = f.length > 1 ? f[1].trim() : "";
                    String nome = f.length > 2 ? f[2].trim() : "";
                    String uf = f.length > 3 ? f[3].trim() : "";
                    double area = 0.0;
                    // tentar encontrar um campo numérico que represente area
                    for (int i = 3; i < f.length; i++) {
                        String tok = f[i].trim();
                        try {
                            double v = Double.parseDouble(tok);
                            area = v;
                            break;
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    Estado e = estadosByCodigo.get(codigo);
                    if (e == null) {
                        e = new Estado(codigo, nome, area, uf);
                        estadosByCodigo.put(codigo, e);
                        regioes.add(e);
                    }
                    // se houver dados de ano/habitantes na mesma linha, tentamos adicioná-los
                    // procurar pares (ano, habitantes)
                    for (int i = 3; i + 1 < f.length; i++) {
                        String a = f[i].trim();
                        String b = f[i + 1].trim();
                        if (a.matches("\\d{4}") && b.matches("\\d+")) {
                            try {
                                int ano = Integer.parseInt(a);
                                long hab = Long.parseLong(b);
                                e.addPopulacao(new Populacao(ano, hab));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }

                } else if (tipo.equalsIgnoreCase("MUNICIPIO")) {
                    // formatos possíveis (com base no CSV atual):
                    // MUNICIPIO;codigo;nome;uf;ano;habitantes;area
                    // ou variantes sem area ou sem codigoEstado explícito
                    String codigo = f.length > 1 ? f[1].trim() : "";
                    String nome = f.length > 2 ? f[2].trim() : "";
                    double area = 0.0;
                    Integer ano = null;
                    Long hab = null;
                    String codigoEstado = "";

                    // tentar detectar ano (campo com 4 dígitos) e habitantes (seguinte)
                    for (int i = 3; i < f.length; i++) {
                        String tok = f[i].trim();
                        if (tok.matches("\\d{4}")) {
                            try {
                                ano = Integer.parseInt(tok);
                                if (i + 1 < f.length) {
                                    String next = f[i + 1].trim();
                                    try {
                                        hab = Long.parseLong(next);
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                                // área pode estar após habitantes
                                if (i + 2 < f.length) {
                                    String next2 = f[i + 2].trim();
                                    try {
                                        area = Double.parseDouble(next2);
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                                break;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }

                    // se não encontrou ano/hab, tentar posições padrão
                    if (ano == null && f.length > 4) {
                        try {
                            ano = Integer.parseInt(f[4].trim());
                        } catch (Exception ignored) {
                        }
                    }
                    if (hab == null && f.length > 5) {
                        try {
                            hab = Long.parseLong(f[5].trim());
                        } catch (Exception ignored) {
                        }
                    }
                    if (area == 0.0 && f.length > 6) {
                        try {
                            area = Double.parseDouble(f[6].trim());
                        } catch (Exception ignored) {
                        }
                    }

                    // tentar obter codigoEstado: se presente como campo extra, use-o;
                    // caso contrário deduzir dos dois primeiros dígitos do código do município
                    if (f.length > 7 && !f[7].trim().isEmpty()) {
                        codigoEstado = f[7].trim();
                    } else if (!codigo.isEmpty() && codigo.length() >= 2) {
                        codigoEstado = codigo.substring(0, 2);
                    }

                    Municipio m = new Municipio(codigo, nome, area, codigoEstado);
                    if (ano != null && hab != null) {
                        m.addPopulacao(new Populacao(ano, hab));
                    }
                    regioes.add(m);

                } else {
                    throw new ArquivoInvalidoException("Tipo desconhecido na linha: " + ln);
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
        }
    }
}
