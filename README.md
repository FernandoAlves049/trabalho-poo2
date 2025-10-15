# Trabalho POO — Dashboards Populacionais (Entrega: Parte 1)

Este repositório contém a implementação da Parte 1 do trabalho prático "Dashboards Populacionais" — modelagem e organização dos dados. A Parte 2 (JavaFX, JDBC e multithread) foi retirada desta entrega conforme solicitado.

## Sumário
- Visão geral
- Como compilar e executar
- Estrutura do projeto e descrição completa dos arquivos Java
- Como os requisitos da Parte 1 foram atendidos
- Testes e verificações feitas
- Próximos passos (opcionais)

---

## Visão geral
O sistema lê dados populacionais de um CSV (arquivo `data/populacao.csv`), valida as linhas, constrói objetos de domínio (`Estado`, `Municipio`, `Populacao`), organiza em coleções, calcula indicadores através de polimorfismo e exibe um relatório simples no console com indicadores e um ranking (Top 5).

Este README descreve detalhadamente cada classe Java presente no projeto e como executar a entrega da Parte 1.

---

## Como compilar e executar (Windows PowerShell)
1) Compilar todos os arquivos Java:
```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out -sourcepath src $files
```
# Trabalho POO — Dashboards Populacionais (Entrega: Parte 1)

Este repositório contém a implementação da Parte 1 do trabalho prático "Dashboards Populacionais" — modelagem e organização dos dados. A Parte 2 (JavaFX, JDBC e multithread) foi retirada desta entrega conforme solicitado.

## Objetivo deste documento
Adicionar explicações técnicas detalhadas do código Java: contrato (entradas/saídas), assinaturas de métodos relevantes, algoritmos usados, complexidade esperada, tratamento de erros, casos de borda e exemplos de uso para cada classe Java principal.

---

## Como compilar e executar (Windows PowerShell)
1) Compilar todos os arquivos Java:
```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out -sourcepath src $files
```
2) Executar:
```powershell
java -cp out App
```

Observação: o diretório `out` é criado pelo `javac -d out`. Se preferir, substitua `out` por `bin`.

---

## Estrutura do projeto (resumo rápido)
- `src/` - código-fonte Java
	- `App.java`
	- `model/` — `Regiao`, `Estado`, `Municipio`, `Populacao`
	- `indicadores/` — `Indicador`, `DensidadeDemografica`, `CrescimentoPopulacional`
	- `service/` — `Entrada`
	- `exceptions/` — `ArquivoInvalidoException`
- `data/populacao.csv` — arquivo de exemplo usado pelo `App`

---

## Explicações detalhadas por arquivo Java
Para cada classe abaixo descrevo: contrato (entradas/saídas), assinaturas públicas relevantes, comportamento/algoritmo, casos de borda e um pequeno exemplo de uso.

### `App.java`
- Local: `src/App.java`
- Propósito: aplicação de demonstração. Lê o CSV, organiza dados, aplica indicadores e imprime relatórios.
- Contrato (alto nível):
	- Entrada: caminho do arquivo CSV (hardcoded em `App` como `data/populacao.csv`)
	- Saída: impressão formatada no console (System.out)
	- Erros: captura `ArquivoInvalidoException` e imprime mensagem de erro.
- Assinaturas importantes (públicas):
	- `public static void main(String[] args)`
- Comportamento / algoritmo:
	- Chama `Entrada.lerArquivo(path)` para obter `List<Regiao>`.
	- Converte em `ArrayList<Regiao>` para permitir ordenação in-place.
	- Ordena com `Comparator` por tipo (Estado antes de Município) e por nome.
	- Cria instâncias de indicadores (ex.: `new DensidadeDemografica()`), percorre as regiões e calcula valores.
	- Ordena regiões por população do último ano para gerar Top-5.
- Casos de borda tratados:
	- Arquivo inválido ou linhas malformadas: `Entrada` lança `ArquivoInvalidoException` (tratado no `main`).
	- Regiões sem histórico: indicadores tratam caso de `null`/ausência retornando `0` ou `Double.NaN` conforme a implementação.
- Exemplo de pseudouso (trecho):
	- `List<Regiao> regs = Entrada.lerArquivo("data/populacao.csv");` (tratamento de exceção obrigatório)


### `model/Regiao.java` (abstrata)
- Local: `src/model/Regiao.java`
- Propósito: fornecer campos e operações comuns a `Estado` e `Municipio`.
- Contrato:
	- Estado encapsula: `String codigo`, `String nome`, `double areaKm2`, `LinkedList<Populacao> historico`.
	- Promete: permitir adicionar registros populacionais e acessar último/penúltimo registro.
- Assinaturas públicas relevantes:
	- `public String getCodigo()`
	- `public void setCodigo(String codigo)`
	- `public String getNome()` / `public void setNome(String nome)`
	- `public double getAreaKm2()` / `public void setAreaKm2(double area)`
	- `public void addPopulacao(Populacao p)`
	- `public Populacao getUltimaPopulacao()`
	- `public Populacao getPenultimaPopulacao()`
	- `public List<Populacao> getHistorico()`
	- `public abstract String getTipo()`
- Comportamento / algoritmo:
	- `addPopulacao` insere ao final de `historico` (LinkedList) preservando ordem temporal.
	- `getUltimaPopulacao` retorna `historico.getLast()` se não vazio, caso contrário `null`.
	- `getPenultimaPopulacao` retorna penúltimo elemento ou `null`.
- Casos de borda:
	- `areaKm2 <= 0`: indicadores precisam tratar divisão por zero (ver `DensidadeDemografica`).
	- `historico` vazio ou único elemento: `getPenultimaPopulacao()` retorna `null` — `CrescimentoPopulacional` deve tratar.
- Complexidade:
	- `addPopulacao`: O(1) (LinkedList)
	- `getUltimaPopulacao`: O(1)

---

### `model/Estado.java`
- Local: `src/model/Estado.java`
- Propósito: representar um estado que pode agregar municípios.
- Campos públicos/contrato:
	- `private String uf` (sigla)
	- `private List<Municipio> municipios`
	- `public void addMunicipio(Municipio m)` — associa e adiciona
	- `public long getPopulacaoTotalUltima()` — soma a população do último ano de todos os municípios (se municípios não vazios)
- Comportamento:
	- `addMunicipio` além de incluir no `municipios`, chama `m.setEstado(this)` garantindo a bidirecionalidade lógica.
	- `getPopulacaoTotalUltima` itera sobre municípios e soma `m.getUltimaPopulacao().getHabitantes()` quando presente.
- Casos de borda:
	- Municípios sem histórico: ignorados na soma.
	- Sem municípios: retorna 0.
- Complexidade:
	- `getPopulacaoTotalUltima`: O(nMunicipios)

---

### `model/Municipio.java`
- Local: `src/model/Municipio.java`
- Propósito: representar um município com referência opcional ao `Estado` pai.
- Campos/assinaturas:
	- `private String codigoEstado`
	- `private Estado estado` (pode ser `null` até associação)
	- getters/setters padrão
- Observações:
	- Associação entre município e estado é feita por `Entrada` ao final do parsing, quando há mapeamento por código.

---

### `model/Populacao.java`
- Local: `src/model/Populacao.java`
- Propósito: registro simples com `int ano` e `long habitantes`.
- Assinaturas:
	- `public int getAno()` / `public void setAno(int ano)`
	- `public long getHabitantes()` / `public void setHabitantes(long h)`
- Uso: armazenado em `Regiao.historico` em ordem cronológica.

---

### `indicadores/Indicador.java`
- Local: `src/indicadores/Indicador.java`
- Contrato:
	- `double calcular(Regiao r)` — calcula o valor do indicador para a região fornecida. Deve documentar comportamento quando entradas ausentes.
	- `String getNome()` — nome do indicador (usado em saída).
- Observação: permitir futuras implementações (ex.: taxa de mortalidade, IDH) sem tocar nas classes de domínio.

---

### `indicadores/DensidadeDemografica.java`
- Local: `src/indicadores/DensidadeDemografica.java`
- Descrição técnica:
	- Fórmula: densidade = habitantes_ultimo_ano / areaKm2
	- Implementação: obtém `Populacao last = r.getUltimaPopulacao()` e `area = r.getAreaKm2()`; se `last == null || area <= 0` retorna `0.0` (ou `Double.NaN` se preferir destacar caso inválido).
- Assinatura:
	- `public double calcular(Regiao r)`
- Casos de borda tratados:
	- `area <= 0` -> evita divisão por zero, retorna 0.
	- `last == null` -> retorna 0.
- Complexidade: O(1)

Exemplo:
```
Indicador id = new DensidadeDemografica();
double d = id.calcular(regiao);
```

---

### `indicadores/CrescimentoPopulacional.java`
- Local: `src/indicadores/CrescimentoPopulacional.java`
- Descrição técnica:
	- Fórmula (percentual): ((ultimo - penultimo) / (double) penultimo) * 100
	- Implementação segura: se `penultimo == null || penultimo.habitantes == 0` retorna `0.0` (evita divisão por zero).
- Assinatura:
	- `public double calcular(Regiao r)`
- Casos de borda:
	- Históricos com menos de 2 entradas: retorna 0.
	- Penúltimo com 0 habitantes: define resultado como 0 para evitar NaN/Inf (pode ser alterado para `Double.POSITIVE_INFINITY` se desejado).
- Complexidade: O(1)

---

### `service/Entrada.java`
- Local: `src/service/Entrada.java`
- Propósito: leitura e validação do CSV, criação de objetos de domínio e associação entre municípios e estados.
- Contrato:
	- Assinatura: `public static List<Regiao> lerArquivo(String path) throws ArquivoInvalidoException`
	- Entrada: caminho para arquivo CSV com cabeçalho esperado (definido no código).
	- Saída: `List<Regiao>` contendo todos os `Estado` e `Municipio` lidos.
	- Erros: lança `ArquivoInvalidoException` quando o arquivo não segue o formato esperado ou ocorre erro de I/O.
- Comportamento / algoritmo:
	1. Lê todas as linhas com `Files.readAllLines`.
	2. Verifica se header corresponde ao esperado (por exemplo: `tipo,codigo,nome,area,ano,habitantes,codigoEstado,uf`).
	3. Para cada linha válida:
		 - Se `tipo == Estado` cria `Estado` (usa `codigo` como chave) e adiciona `Populacao` ao seu `historico`.
		 - Se `tipo == Municipio` cria `Municipio`, registra `codigoEstado` temporariamente e adiciona `Populacao`.
	4. Mantém um `Map<String, Estado>` para procurar estados por código e ao final associa cada `Municipio` ao seu `Estado` (chamada `estado.addMunicipio(m)`).
	5. Retorna `List<Regiao>` (pode conter instâncias misturadas de `Estado` e `Municipio`).
- Validações realizadas:
	- Número de colunas correto por linha.
	- Conversões numéricas válidas (`Integer.parseInt`, `Long.parseLong`, `Double.parseDouble`).
	- Arquivo não vazio e header válido.
- Casos de borda e recomendações:
	- Município referenciando códigoEstado inexistente: atualmente o município permanece sem `estado` (pode ser registrado em `orphanMunicipios` para auditoria).
	- Linhas duplicadas (mesma combinação tipo+codigo+ano): estratégia atual: append — caso real deveria substituir/atualizar.
	- CSV com separador diferente (ponto-e-vírgula): atualizar o split no código ou fazer detecção automática.
- Complexidade:
	- Leitura: O(nLinhas)
	- Associação final: O(nMunicipios) com acesso O(1) ao `Map` de estados.

Exemplo de uso:
```java
try {
		List<Regiao> regioes = Entrada.lerArquivo("data/populacao.csv");
		// usa regioes
} catch (ArquivoInvalidoException e) {
		System.err.println("Arquivo inválido: " + e.getMessage());
}
```

---

### `exceptions/ArquivoInvalidoException.java`
- Local: `src/exceptions/ArquivoInvalidoException.java`
- Propósito: sinalizar erros de formato e validação do arquivo de entrada.
- Assinatura:
	- `public class ArquivoInvalidoException extends Exception`
	- Construtores típicos: `(String message)` e `(String message, Throwable cause)`

---

## Verificações rápidas (runtime)
- Compilar com `javac` e executar `java -cp out App` deve imprimir um relatório com:
	- Lista de regiões (Estado / Município) com área e última população
	- Valores de `DensidadeDemografica` e `CrescimentoPopulacional` calculados
	- Um ranking Top-5 por população do último ano

Se ocorrer `ArquivoInvalidoException`, verifique o conteúdo de `data/populacao.csv` e o header.

---

## Observações finais e sugestões de melhorias
- Testes unitários (JUnit) para `Entrada`, `DensidadeDemografica` e `CrescimentoPopulacional` ajudam a garantir robustez ao adicionar mais casos de borda.
- Melhorar `Entrada` para suportar diferentes formatos (CSV com encoding, delimitador) e políticas de deduplicação/merge para registros repetidos.
- Adicionar logging (slf4j/logback) em vez de `System.out` para facilitar depuração em produção.
- Se desejar que eu gere testes JUnit ou um ZIP para entrega, diga qual opção prefere e eu gero em seguida.

---

## Interface gráfica (Swing)
Adicionei uma interface desktop simples usando Swing para facilitar o uso interativo (opção a mais fácil de trabalhar). A interface permite:

- Abrir um arquivo CSV via diálogo (`Abrir CSV...`).
- Visualizar todas as regiões em uma tabela com ordenação por coluna.
- Ver colunas: Tipo, Código, Nome, Área (km²), Última População, Densidade e Crescimento (%).

Arquivos adicionados:
- `src/ui/RegiaoTableModel.java` — TableModel que converte `Regiao` em linhas/colunas para a tabela Swing.
- `src/ui/GuiApp.java` — Janela Swing com `JFileChooser` para abrir o CSV e popular a tabela.

Como executar a GUI:
1) Compile como de costume:
```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out -sourcepath src $files
```
2) Execute com a flag `--gui`:
```powershell
java -cp out App --gui
```

Observações sobre o parser/Entrada:
- O parser (`Entrada.lerArquivo`) agora detecta automaticamente o delimitador `;` ou `,` e aceita arquivos com ou sem header.
- Para `ESTADO` linhas do tipo `ESTADO;52;Goiás;GO` o parser cria o `Estado` (código, nome, UF). Se houver campos numéricos na mesma linha (ex.: área, ano, habitantes) tenta extrair e acrescentar ao histórico.
- Para `MUNICIPIO` o parser tenta detectar ano (token com 4 dígitos) e habitantes, e também a área em posições próximas; se não encontrar, usa campos padrão.
- Associação município→estado: tenta usar `codigoEstado` quando presente; caso contrário, usa um fallback simples derivado dos primeiros caracteres do código do município (ajustável conforme regra local).

