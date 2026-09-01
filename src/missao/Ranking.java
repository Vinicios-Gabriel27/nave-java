package missao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Ranking {

    private static final DateTimeFormatter DATA_HORA_FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path rankingPath;
    private final List<RankingEntry> ranking;

    public Ranking(Path rankingPath) {
        this.rankingPath = rankingPath;
        this.ranking = loadRanking();


        if (!Files.exists(rankingPath)) {
            saveRanking();
        }
    }

    public List<RankingEntry> getRanking() {
        return ranking;
    }

    public void exibirRankingCompleto() {
        System.out.println();
        System.out.println("================ RANKING TOP 5 ================");

        if (ranking.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
        } else {
            int posicao = 1;

            for (RankingEntry entry : ranking) {
                System.out.printf(
                        "%d. %s - %d pts | %s | Passageiros: %d | Movimentos: %d | Tempo: %ds | %s%n",
                        posicao++,
                        entry.getName(),
                        entry.getScore(),
                        entry.getDificuldade(),
                        entry.getPassageirosColetados(),
                        entry.getMovimentos(),
                        entry.getTempoJogo(),
                        entry.getDataHora()
                );
            }
        }

        System.out.println("================================================");
    }

    public void exibirEstatisticas(
            int score,
            int movimentos,
            long tempoSegundos,
            int passageiros) {

        System.out.println();
        System.out.println("================ ESTATÍSTICAS ================");
        System.out.printf("Pontuação: %d%n", score);
        System.out.printf("Movimentos: %d%n", movimentos);
        System.out.printf("Tempo de jogo: %d segundos%n", tempoSegundos);
        System.out.printf("Passageiros coletados: %d%n", passageiros);

        if (ranking.isEmpty()) {
            System.out.println("Primeira pontuação registrada nesta sessão.");
        } else {
            int recorde = ranking.get(0).getScore();

            System.out.printf(
                    "Recorde atual: %d pontos (%s)%n",
                    recorde,
                    ranking.get(0).getName()
            );

            if (score > recorde) {
                System.out.println("NOVO RECORDE!");
            }
        }

        System.out.println("===============================================");
    }

    public void salvarRankingSeMerece(
            String pilotoNome,
            int score,
            Dificuldade dificuldade,
            int passageirosColetados,
            long tempoJogo,
            int movimentos) {

        if (score <= 0) {
            return;
        }

        if (!isTopScore(score)) {
            System.out.println("Pontuação concluída, mas fora do Top 5.");
            return;
        }

        RankingEntry novaEntrada = new RankingEntry(
                pilotoNome,
                score,
                dificuldade,
                passageirosColetados,
                LocalDateTime.now().format(DATA_HORA_FORMATO),
                tempoJogo,
                movimentos
        );

        ranking.add(novaEntrada);

        List<RankingEntry> ordenado = ranking.stream()
                .sorted(
                        Comparator.comparingInt(
                                RankingEntry::getScore
                        ).reversed()
                )
                .limit(5)
                .collect(Collectors.toList());

        ranking.clear();
        ranking.addAll(ordenado);

        saveRanking();

        System.out.println("Pontuação salva no Ranking Top 5!");
    }

    private boolean isTopScore(int score) {
        if (ranking.size() < 5) {
            return true;
        }

        return score > ranking.get(ranking.size() - 1).getScore();
    }

    public void resetarRanking(Scanner scanner) {
        System.out.print(
                "Você realmente deseja apagar o histórico de ranking? (s/n): "
        );

        String confirmacao = scanner.nextLine().trim().toLowerCase();

        if (confirmacao.equals("s") || confirmacao.equals("sim")) {
            try {
                Files.deleteIfExists(rankingPath);
                ranking.clear();

                // Cria novamente o arquivo vazio
                saveRanking();

                System.out.println("Histórico de ranking resetado!");
            } catch (IOException e) {
                System.out.println("Erro ao apagar ranking: " + e.getMessage());

                ranking.clear();
                ranking.addAll(loadRanking());
            }
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private List<RankingEntry> loadRanking() {
        if (!Files.exists(rankingPath)) {
            return new ArrayList<>();
        }

        try {
            String json = new String(
                    Files.readAllBytes(rankingPath),
                    StandardCharsets.UTF_8
            ).trim();

            return parseRankingJson(json);

        } catch (IOException e) {
            System.out.println(
                    "Não foi possível carregar o ranking: " + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    private void saveRanking() {
        StringBuilder builder = new StringBuilder();

        builder.append("[\n");

        for (int i = 0; i < ranking.size(); i++) {

            RankingEntry entry = ranking.get(i);

            builder.append("  {")
                    .append("\"name\":\"")
                    .append(escapeJson(entry.getName()))
                    .append("\",")

                    .append("\"score\":")
                    .append(entry.getScore())
                    .append(",")

                    .append("\"dificuldade\":\"")
                    .append(entry.getDificuldade().name())
                    .append("\",")

                    .append("\"passageirosColetados\":")
                    .append(entry.getPassageirosColetados())
                    .append(",")

                    .append("\"dataHora\":\"")
                    .append(escapeJson(entry.getDataHora()))
                    .append("\",")

                    .append("\"tempoJogo\":")
                    .append(entry.getTempoJogo())
                    .append(",")

                    .append("\"movimentos\":")
                    .append(entry.getMovimentos())

                    .append("}");

            if (i < ranking.size() - 1) {
                builder.append(",");
            }

            builder.append("\n");
        }

        builder.append("]\n");

        try {

            // Garante que a pasta do arquivo exista
            Path parent = rankingPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.write(
                    rankingPath,
                    builder.toString().getBytes(StandardCharsets.UTF_8)
            );

        } catch (IOException e) {

            System.out.println(
                    "Não foi possível salvar o ranking: " + e.getMessage()
            );
        }
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private List<RankingEntry> parseRankingJson(String json) {

        List<RankingEntry> ranking = new ArrayList<>();

        if (json == null ||
                json.trim().isEmpty() ||
                json.trim().equals("[]")) {

            return ranking;
        }

        Pattern objectPattern =
                Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);

        Matcher matcher = objectPattern.matcher(json);

        while (matcher.find()) {

            String object = matcher.group(1);

            String name = extrairString(object, "name");
            Integer score = extrairInt(object, "score");
            String dificuldadeTexto =
                    extrairString(object, "dificuldade");

            Integer passageiros =
                    extrairInt(object, "passageirosColetados");

            String dataHora =
                    extrairString(object, "dataHora");

            Long tempo =
                    extrairLong(object, "tempoJogo");

            Integer movimentos =
                    extrairInt(object, "movimentos");

            if (name == null || score == null) {
                continue;
            }

            Dificuldade dificuldade =
                    Dificuldade.deString(dificuldadeTexto);

            int passageirosColetados =
                    passageiros == null ? 0 : passageiros;

            String data =
                    dataHora == null ? "" : dataHora;

            long tempoJogo =
                    tempo == null ? 0L : tempo;

            int qtdMovimentos =
                    movimentos == null ? 0 : movimentos;

            ranking.add(
                    new RankingEntry(
                            name,
                            score,
                            dificuldade,
                            passageirosColetados,
                            data,
                            tempoJogo,
                            qtdMovimentos
                    )
            );
        }

        ranking.sort(
                Comparator.comparingInt(
                        RankingEntry::getScore
                ).reversed()
        );

        if (ranking.size() > 5) {
            ranking = ranking.subList(0, 5);
        }

        return new ArrayList<>(ranking);
    }

    private String extrairString(
            String objeto,
            String chave) {

        Pattern p = Pattern.compile(
                "\\\"" +
                        Pattern.quote(chave) +
                        "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\""
        );

        Matcher m = p.matcher(objeto);

        if (!m.find()) {
            return null;
        }

        return m.group(1)
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\\", "\\");
    }

    private Integer extrairInt(
            String objeto,
            String chave) {

        Long valor = extrairLong(objeto, chave);

        return valor == null
                ? null
                : valor.intValue();
    }

    private Long extrairLong(
            String objeto,
            String chave) {

        Pattern p = Pattern.compile(
                "\\\"" +
                        Pattern.quote(chave) +
                        "\\\"\\s*:\\s*(-?\\d+)"
        );

        Matcher m = p.matcher(objeto);

        if (!m.find()) {
            return null;
        }

        try {
            return Long.parseLong(m.group(1));

        } catch (NumberFormatException e) {
            return null;
        }
    }
}