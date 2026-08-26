package missao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Random random = new Random();
        Path rankingPath = Paths.get(System.getProperty("user.home"), ".ranking-missao-marte.json");
        List<RankingEntry> ranking = loadRanking(rankingPath);
        Scanner scanner = new Scanner(System.in);

        boolean executando = true;

        while (executando) {
            exibirMenuInicial();
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    iniciarNovaMissao(scanner, random, ranking, rankingPath);
                    break;
                case "2":
                    System.out.println();
                    System.out.println("=== Ranking Top 5 ===");
                    if (ranking.isEmpty()) {
                        System.out.println("Ainda não há pontuações registradas.");
                    } else {
                        printRanking(ranking);
                    }
                    System.out.println();
                    System.out.println("Pressione Enter para voltar ao menu...");
                    scanner.nextLine();
                    break;
                case "3":
                    ranking.clear();
                    saveRanking(rankingPath, ranking);
                    System.out.println();
                    System.out.println("Histórico de ranking resetado com sucesso!");
                    System.out.println("Pressione Enter para voltar ao menu...");
                    scanner.nextLine();
                    break;
                case "4":
                    executando = false;
                    System.out.println("Saindo do jogo. Até a próxima missão!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
                    System.out.println("Pressione Enter para continuar...");
                    scanner.nextLine();
            }
        }

        scanner.close();
        System.out.println("Fim da execução.");
    }

    private static void exibirMenuInicial() {
        System.out.println();
        System.out.println("================================================================");
        System.out.println("       MISSÃO MARTE UNIFOR — Console");
        System.out.println("================================================================");
        System.out.println();
        System.out.println("  1. Iniciar Nova Missão");
        System.out.println("  2. Visualizar Ranking Top 5");
        System.out.println("  3. Resetar Histórico de Ranking");
        System.out.println("  4. Sair do Jogo");
        System.out.println();
        System.out.print("Escolha uma opção: ");
    }

    private static void iniciarNovaMissao(
            Scanner scanner,
            Random random,
            List<RankingEntry> ranking,
            Path rankingPath) {

        System.out.println();
        System.out.print("Digite o nome do piloto: ");
        String pilotoNome = scanner.nextLine().trim();

        System.out.print("Tamanho do mapa (-X a +X): ");
        int tamanho = Integer.parseInt(scanner.nextLine());

        if (pilotoNome.isEmpty()) {
            pilotoNome = "Piloto Anônimo";
        }

        int minX = -tamanho;
        int maxX = tamanho;
        int minY = -tamanho;
        int maxY = tamanho;

        System.out.println();
        System.out.println("================================================================");
        System.out.println("Objetivo:");
        System.out.println(" - Mover a nave pelo mapa");
        System.out.println(" - Encontrar e embarcar todos os passageiros");
        System.out.println(" - Evitar colisões com asteroides e inimigos");
        System.out.println(" - Manter a pontuação acima de zero");
        System.out.println(" - Após salvar todos, retorne à Plataforma de Pouso (L) em (0,0)");
        System.out.println();
        System.out.println("Comandos:");
        System.out.println(" - w: mover para cima");
        System.out.println(" - s: mover para baixo");
        System.out.println(" - a: mover para a esquerda");
        System.out.println(" - d: mover para a direita");
        System.out.println(" - c: embarcar passageiro na posição atual");
        System.out.println(" - q: sair do jogo");
        System.out.println();
        System.out.println("Pressione Enter para iniciar a missão...");
        scanner.nextLine();
        System.out.println("================================================================");

        Dificuldade dificuldade = selecionarDificuldade(scanner);

        boolean playAgain = true;

        while (playAgain) {
            Missao missao = criarNovaMissao(
                    random,
                    minX,
                    maxX,
                    minY,
                    maxY,
                    dificuldade
            );

            Nave nave = missao.getNave();
            int score = definirPontuacaoInicial(dificuldade);
            boolean running = true;
            boolean todosEmbarcados = false;

            while (running) {
                desenharMapa(
                        missao,
                        minX,
                        maxX,
                        minY,
                        maxY,
                        score,
                        pilotoNome
                );

                System.out.printf(
                        "Nave em (%d,%d) | Pontos: %d | Vidas: %d | Passageiros a bordo: %d | Passageiros restantes: %d%n",
                        nave.getX(),
                        nave.getY(),
                        score,
                        nave.getVidas(),
                        nave.getPassageiros().size(),
                        missao.todosEmbarcados() ? 0 : missao.getPassageiros().size()
                );

                if (todosEmbarcados && !(nave.getX() == 0 && nave.getY() == 0)) {
                    System.out.println(">> Todos embarcados! Retorne à Plataforma de Pouso (L) em (0,0)!");
                }

                Passageiro passageiroAqui = missao.passagemNaPosicao();

                if (passageiroAqui != null && !todosEmbarcados) {
                    System.out.printf(
                            ">> Passageiro %s (%s) aqui! Pressione 'c' para embarcar.%n",
                            passageiroAqui.getNome(),
                            passageiroAqui.getTipo()
                    );
                }

                if (missao.verificaColisao()) {
                    nave.perderVida();

                    if (nave.getVidas() == 0) {
                        System.out.println("Game Over!");
                        System.out.printf("Pontuação final: %d%n", score);
                        salvarRankingSeMerece(ranking, rankingPath, pilotoNome, score);
                        running = false;
                        break;
                    } else {
                        System.out.println(
                                "Bateu em asteroide ou inimigo! Vidas restantes: "
                                        + nave.getVidas()
                        );

                        nave.reposicionar(0, 0);
                        System.out.println("Nave reposicionada em (0,0).");
                    }
                }

                System.out.print("Para onde ir? ");
                String line = scanner.nextLine().trim().toLowerCase();

                if (line.isEmpty()) {
                    continue;
                }

                char cmd = line.charAt(0);

                switch (cmd) {
                    case 'w':
                        nave.moveUp();
                        score--;
                        break;

                    case 's':
                        nave.moveDown();
                        score--;
                        break;

                    case 'a':
                        nave.moveLeft();
                        score--;
                        break;

                    case 'd':
                        nave.moveRight();
                        score--;
                        break;

                    case 'c': {
                        if (todosEmbarcados) {
                            System.out.println(
                                    "Todos já estão embarcados. Retorne à plataforma de pouso!"
                            );
                        } else {
                            Passageiro p = missao.passagemNaPosicao();

                            if (p == null) {
                                System.out.println(
                                        "Nenhum passageiro nesta posição."
                                );
                            } else {
                                boolean ok =
                                        missao.embarcarPassageiroNaPosicao();

                                if (ok) {
                                    score += p.getPontuacao();

                                    System.out.println(
                                            "Passageiro embarcado: " + p.getNome()
                                    );
                                    System.out.println(
                                            "Tipo: " + p.getTipo()
                                    );
                                    System.out.println(
                                            "+" + p.getPontuacao() + " pontos!"
                                    );

                                    if (missao.todosEmbarcados()) {
                                        todosEmbarcados = true;
                                        System.out.println();
                                        System.out.println("*** Todos os passageiros foram embarcados! ***");
                                        System.out.println("*** Agora retorne à Plataforma de Pouso (L) em (0,0)! ***");
                                    }
                                } else {
                                    System.out.println(
                                            "Nave cheia, não foi possível embarcar."
                                    );
                                }
                            }
                        }

                        break;
                    }

                    case 'q':
                        running = false;
                        break;

                    default:
                        System.out.println("Comando desconhecido.");
                }

                if (!running) {
                    break;
                }

                missao.moverInimigos(
                        random,
                        minX,
                        maxX,
                        minY,
                        maxY
                );

                if (missao.verificaColisao()) {
                    nave.perderVida();

                    if (nave.getVidas() == 0) {
                        System.out.println("Game Over! Vidas esgotadas.");
                        System.out.printf("Pontuação final: %d%n", score);
                        salvarRankingSeMerece(ranking, rankingPath, pilotoNome, score);
                        running = false;
                        break;
                    } else {
                        System.out.println(
                                "Inimigo ou asteroide na sua posição! Vidas restantes: "
                                        + nave.getVidas()
                        );

                        nave.reposicionar(0, 0);

                        System.out.println("Nave reposicionada em (0,0).");
                    }
                }

                if (score <= 0) {
                    System.out.println(
                            "Pontuação zerada. Missão perdida."
                    );
                    System.out.printf("Pontuação final: %d%n", score);
                    salvarRankingSeMerece(ranking, rankingPath, pilotoNome, score);
                    running = false;
                    break;
                }

                if (todosEmbarcados && nave.getX() == 0 && nave.getY() == 0) {
                    System.out.println();
                    System.out.println("*** POUSO REALIZADO COM SUCESSO! ***");
                    System.out.println("*** Missão concluída! Todos os passageiros foram salvos! ***");
                    System.out.printf("*** Pontuação final: %d ***%n", score);

                    salvarRankingSeMerece(ranking, rankingPath, pilotoNome, score);

                    running = false;
                }
            }

            if (!ranking.isEmpty()) {
                System.out.println();
                System.out.println("Ranking Top 5:");
                printRanking(ranking);
            } else {
                System.out.println();
                System.out.println(
                        "Ranking vazio. Seja o primeiro a marcar pontos!"
                );
            }

            System.out.print(
                    "Deseja iniciar nova missão? (s/n): "
            );

            String resposta =
                    scanner.nextLine().trim().toLowerCase();

            if (resposta.equals("s") || resposta.equals("sim")) {
                System.out.println(
                        "Preparando nova missão..."
                );
            } else {
                playAgain = false;
            }
        }
    }

    private static void printRanking(
            List<RankingEntry> ranking) {

        int position = 1;

        for (RankingEntry entry : ranking) {
            System.out.printf(
                    "%d. %s - %d pontos%n",
                    position++,
                    entry.name,
                    entry.score
            );
        }
    }

    private static void salvarRankingSeMerece(
            List<RankingEntry> ranking,
            Path rankingPath,
            String pilotoNome,
            int score) {

        if (score > 0 && isTopScore(ranking, score)) {
            ranking.add(
                    new RankingEntry(pilotoNome, score)
            );

            List<RankingEntry> sorted = ranking.stream()
                    .sorted(
                            Comparator.comparingInt(
                                    (RankingEntry e) -> e.score
                            ).reversed()
                    )
                    .limit(5)
                    .collect(Collectors.toList());

            ranking.clear();
            ranking.addAll(sorted);

            saveRanking(rankingPath, ranking);

            System.out.println(
                    "Pontuação salva no ranking! Você está entre os 5 maiores."
            );
        }
    }

    private static Dificuldade selecionarDificuldade(
            Scanner scanner) {

        System.out.println();
        System.out.println("Escolha a dificuldade:");
        System.out.println(
                " 1. Fácil   (3 asteroides, 30 pontos, 2 passageiros)"
        );
        System.out.println(
                " 2. Normal  (5 asteroides, 20 pontos, 6 passageiros)"
        );
        System.out.println(
                " 3. Difícil (8 asteroides, 15 pontos, 9 passageiros)"
        );
        System.out.print("Opção: ");

        String opcao =
                scanner.nextLine().trim();

        switch (opcao) {
            case "1":
                return Dificuldade.FACIL;

            case "3":
                return Dificuldade.DIFICIL;

            default:
                return Dificuldade.NORMAL;
        }
    }

    private static int definirPontuacaoInicial(
            Dificuldade dificuldade) {

        switch (dificuldade) {
            case FACIL:
                return 30;

            case DIFICIL:
                return 15;

            default:
                return 20;
        }
    }

    private static int definirQtdPassageiros(
            Dificuldade dificuldade) {

        switch (dificuldade) {
            case FACIL:
                return 3;

            case DIFICIL:
                return 5;

            default:
                return 4;
        }
    }

    private static int definirQtdAsteroides(
            Dificuldade dificuldade) {

        switch (dificuldade) {
            case FACIL:
                return 3;

            case DIFICIL:
                return 8;

            default:
                return 5;
        }
    }

    private static Missao criarNovaMissao(
            Random random,
            int minX,
            int maxX,
            int minY,
            int maxY,
            Dificuldade dificuldade) {

        Nave nave = new Nave("A-1", 5, 3);

        Missao missao = new Missao(nave);

        int qtdPassageiros =
                definirQtdPassageiros(dificuldade);

        int qtdAsteroides =
                definirQtdAsteroides(dificuldade);

        while (missao.getPassageiros().size()
                < qtdPassageiros) {

            int x =
                    random.nextInt(maxX - minX + 1)
                            + minX;

            int y =
                    random.nextInt(maxY - minY + 1)
                            + minY;

            if (x == nave.getX()
                    && y == nave.getY()) {
                continue;
            }

            if (posicaoOcupada(missao, x, y)) {
                continue;
            }

            if (missao.getPassageiros().isEmpty()) {
                missao.addPassageiro(
                        new Astronauta(
                                "Dr. vinicios",
                                x,
                                y
                        )
                );
            } else if (missao.getPassageiros().size() == 1) {
                missao.addPassageiro(
                        new Engenheiro(
                                "Eng. Rosa",
                                x,
                                y
                        )
                );
            } else {
                missao.addPassageiro(
                        new Professor(
                                "Dr. Lima",
                                x,
                                y
                        )
                );
            }
        }

        while (missao.getAsteroides().size()
                < qtdAsteroides) {

            int x =
                    random.nextInt(maxX - minX + 1)
                            + minX;

            int y =
                    random.nextInt(maxY - minY + 1)
                            + minY;

            if (x == nave.getX()
                    && y == nave.getY()) {
                continue;
            }

            if (posicaoOcupada(missao, x, y)) {
                continue;
            }

            missao.addAsteroide(
                    new Asteroide(x, y)
            );
        }

        while (missao.getInimigos().size() < 2) {
            int x =
                    random.nextInt(maxX - minX + 1)
                            + minX;

            int y =
                    random.nextInt(maxY - minY + 1)
                            + minY;

            if (x == nave.getX()
                    && y == nave.getY()) {
                continue;
            }

            if (posicaoOcupada(missao, x, y)) {
                continue;
            }

            missao.addInimigo(
                    new Inimigo(x, y)
            );
        }

        return missao;
    }

    private static boolean posicaoOcupada(
            Missao missao,
            int x,
            int y) {

        if (missao.getNave().getX() == x
                && missao.getNave().getY() == y) {
            return true;
        }

        for (Passageiro p : missao.getPassageiros()) {
            if (p.getX() == x
                    && p.getY() == y) {
                return true;
            }
        }

        for (Asteroide a : missao.getAsteroides()) {
            if (a.getX() == x
                    && a.getY() == y) {
                return true;
            }
        }

        for (Inimigo i : missao.getInimigos()) {
            if (i.getX() == x
                    && i.getY() == y) {
                return true;
            }
        }

        return false;
    }

    private static void desenharMapa(
            Missao missao,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int score,
            String pilotoNome) {

        System.out.println();

        System.out.printf(
                "Mapa da Missão (Pontos: %d) - Piloto: %s%n",
                score,
                pilotoNome
        );

        System.out.print("    ");

        for (int x = minX; x <= maxX; x++) {
            System.out.printf(" %2d", x);
        }

        System.out.println();

        System.out.print("    ");

        for (int x = minX; x <= maxX; x++) {
            System.out.print(" __");
        }

        System.out.println();

        for (int y = minY; y <= maxY; y++) {
            System.out.printf("%3d|", y);

            for (int x = minX; x <= maxX; x++) {

                char symbol = '.';

                if (x == 0 && y == 0) {
                    symbol = 'L';
                }

                if (missao.getNave().getX() == x
                        && missao.getNave().getY() == y) {

                    symbol = '@';

                } else if (symbol == 'L') {
                    // Mantém o 'L' da plataforma de pouso
                } else {

                    for (Passageiro p :
                            missao.getPassageiros()) {

                        if (p.getX() == x
                                && p.getY() == y) {

                            if (p instanceof Engenheiro) {
                                symbol = 'E';
                            } else if (p instanceof Astronauta) {
                                symbol = 'A';
                            } else {
                                symbol = 'P';
                            }

                            break;
                        }
                    }

                    if (symbol == '.') {
                        for (Asteroide a :
                                missao.getAsteroides()) {

                            if (a.getX() == x
                                    && a.getY() == y) {

                                symbol = '#';
                                break;
                            }
                        }
                    }

                    if (symbol == '.') {
                        for (Inimigo i :
                                missao.getInimigos()) {

                            if (i.getX() == x
                                    && i.getY() == y) {

                                symbol = 'X';
                                break;
                            }
                        }
                    }
                }

                System.out.printf(
                        " %2c",
                        symbol
                );
            }

            System.out.println();
        }

        System.out.println(
                "Legenda: @=Nave, L=Landing Pad, P=Professor, E=Engenheiro, A=Astronauta, #=Asteroide, X=Inimigo, .=Vazio"
        );

        System.out.println(
                "Resumo de comandos: w(cima)/s(baixo)/a(esquerda)/d(direita) mover, c embarcar, q sair"
        );

        System.out.println(
                "Passageiros restantes:"
        );

        for (Passageiro p :
                missao.getPassageiros()) {

            System.out.printf(
                    " - %s (%s) em (%d,%d)%n",
                    p.getNome(),
                    p.getTipo(),
                    p.getX(),
                    p.getY()
            );
        }

        System.out.println();
    }

    private static boolean isTopScore(
            List<RankingEntry> ranking,
            int score) {

        if (ranking.size() < 5) {
            return true;
        }

        return score >
                ranking.get(
                        ranking.size() - 1
                ).score;
    }

    private static List<RankingEntry> loadRanking(
            Path path) {

        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try {
            String json =
                    new String(
                            Files.readAllBytes(path),
                            StandardCharsets.UTF_8
                    ).trim();

            return parseRankingJson(json);

        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void saveRanking(
            Path path,
            List<RankingEntry> ranking) {

        StringBuilder builder =
                new StringBuilder();

        builder.append("[");

        for (int i = 0;
             i < ranking.size();
             i++) {

            RankingEntry entry =
                    ranking.get(i);

            builder.append("{\"name\":\"")
                    .append(
                            entry.name.replace(
                                    "\"",
                                    "\\\""
                            )
                    )
                    .append("\",\"score\":")
                    .append(entry.score)
                    .append("}");

            if (i < ranking.size() - 1) {
                builder.append(",");
            }
        }

        builder.append("]");

        try {
            Files.write(
                    path,
                    builder.toString()
                            .getBytes(StandardCharsets.UTF_8)
            );

        } catch (IOException e) {
            System.out.println(
                    "Não foi possível salvar o ranking: "
                            + e.getMessage()
            );
        }
    }

    private static List<RankingEntry> parseRankingJson(
            String json) {

        List<RankingEntry> ranking =
                new ArrayList<>();

        if (json.isEmpty()
                || json.equals("[]")) {
            return ranking;
        }

        json = json.trim();

        if (json.startsWith("[")) {
            json = json.substring(1);
        }

        if (json.endsWith("]")) {
            json =
                    json.substring(
                            0,
                            json.length() - 1
                    );
        }

        int index = 0;

        while (index < json.length()) {

            int start =
                    json.indexOf(
                            '{',
                            index
                    );

            if (start < 0) {
                break;
            }

            int end =
                    json.indexOf(
                            '}',
                            start
                    );

            if (end < 0) {
                break;
            }

            String object =
                    json.substring(
                            start + 1,
                            end
                    );

            String name = null;
            Integer score = null;

            for (String part :
                    object.split(",")) {

                String[] pair =
                        part.split(":", 2);

                if (pair.length != 2) {
                    continue;
                }

                String key =
                        pair[0]
                                .trim()
                                .replaceAll(
                                        "\"",
                                        ""
                                );

                String value =
                        pair[1].trim();

                if (key.equals("name")) {

                    if (value.startsWith("\"")
                            && value.endsWith("\"")) {

                        name =
                                value.substring(
                                        1,
                                        value.length() - 1
                                ).replace(
                                        "\\\"",
                                        "\""
                                );
                    }

                } else if (key.equals("score")) {

                    try {
                        score =
                                Integer.parseInt(
                                        value
                                );

                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (name != null
                    && score != null) {

                ranking.add(
                        new RankingEntry(
                                name,
                                score
                        )
                );
            }

            index = end + 1;
        }

        ranking.sort(
                Comparator.comparingInt(
                        (RankingEntry e) -> e.score
                ).reversed()
        );

        return ranking;
    }

    private static class RankingEntry {
        private final String name;
        private final int score;

        private RankingEntry(
                String name,
                int score) {

            this.name = name;
            this.score = score;
        }
    }
}