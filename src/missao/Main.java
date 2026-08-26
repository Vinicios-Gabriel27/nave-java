package missao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static final DateTimeFormatter DATA_HORA_FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        Random random = new Random();
        Path rankingPath = Paths.get(
                System.getProperty("user.home"),
                ".ranking-missao-marte.json"
        );
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
                    exibirRankingCompleto(ranking);
                    pausar(scanner);
                    break;
                case "3":
                    ranking = resetarRanking(scanner, rankingPath);
                    break;
                case "4":
                    executando = false;
                    System.out.println("Saindo do jogo. Até a próxima missão!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
                    pausar(scanner);
                    break;
            }
        }

        scanner.close();
    }

    private static void exibirMenuInicial() {
        System.out.println();
        System.out.println("================================================================");
        System.out.println("             MISSÃO MARTE UNIFOR - CONSOLE");
        System.out.println("================================================================");
        System.out.println("1. Iniciar Nova Missão");
        System.out.println("2. Visualizar Ranking Top 5");
        System.out.println("3. Resetar Histórico de Ranking");
        System.out.println("4. Sair do Jogo");
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

        if (pilotoNome.isEmpty()) {
            pilotoNome = "Piloto Anônimo";
        }

        Dificuldade dificuldade = selecionarDificuldade(scanner);
        int tamanho = lerTamanhoMapa(scanner, dificuldade);

        int minX = -tamanho;
        int maxX = tamanho;
        int minY = -tamanho;
        int maxY = tamanho;

        System.out.println();
        System.out.println("================================================================");
        System.out.println("Objetivo:");
        System.out.println(" - Encontrar e embarcar todos os passageiros");
        System.out.println(" - Evitar colisões com asteroides e inimigos");
        System.out.println(" - Manter a pontuação acima de zero");
        System.out.println(" - Depois de resgatar todos, retorne para (0,0)");
        System.out.println();
        System.out.println("Comandos:");
        System.out.println(" - w: cima");
        System.out.println(" - s: baixo");
        System.out.println(" - a: esquerda");
        System.out.println(" - d: direita");
        System.out.println(" - c: embarcar passageiro");
        System.out.println(" - q: abandonar a missão");
        System.out.println("================================================================");
        pausar(scanner);

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
            int movimentos = 0;
            int passageirosPlanejados = definirQtdPassageiros(dificuldade);
            long tempoInicio = System.currentTimeMillis();
            boolean partidaAtiva = true;
            boolean missaoConcluida = false;

            while (partidaAtiva) {
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
                        "Nave (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes: %d | Movimentos: %d%n",
                        nave.getX(),
                        nave.getY(),
                        score,
                        nave.getVidas(),
                        nave.getPassageiros().size(),
                        passageirosPlanejados,
                        missao.getPassageiros().size(),
                        movimentos
                );

                if (missao.todosEmbarcados()) {
                    if (nave.getX() == 0 && nave.getY() == 0) {
                        long tempoJogo = (System.currentTimeMillis() - tempoInicio) / 1000;
                        exibirEstatisticas(
                                score,
                                movimentos,
                                tempoJogo,
                                nave.getPassageiros().size(),
                                ranking
                        );

                        missaoConcluida = true;
                        partidaAtiva = false;
                        salvarRankingSeMerece(
                                ranking,
                                rankingPath,
                                pilotoNome,
                                score,
                                dificuldade,
                                nave.getPassageiros().size(),
                                tempoJogo,
                                movimentos
                        );
                        break;
                    } else {
                        System.out.println(">> Todos embarcados! Retorne para a plataforma em (0,0).");
                    }
                } else {
                    Passageiro passageiroAqui = missao.passagemNaPosicao();
                    if (passageiroAqui != null) {
                        System.out.printf(
                                ">> Passageiro %s (%s) aqui. Pressione 'c' para embarcar. (+%d)%n",
                                passageiroAqui.getNome(),
                                passageiroAqui.getTipo(),
                                passageiroAqui.getPontuacao()
                        );
                    }
                }

                System.out.print("Comando: ");
                String line = scanner.nextLine().trim().toLowerCase();

                if (line.isEmpty()) {
                    continue;
                }

                char cmd = line.charAt(0);

                switch (cmd) {
                    case 'w':
                    case 's':
                    case 'a':
                    case 'd':
                        int xAnterior = nave.getX();
                        int yAnterior = nave.getY();

                        if (nave.moverComLimites(cmd, minX, maxX, minY, maxY)) {
                            movimentos++;
                            score--;
                            System.out.printf(
                                    "Movido de (%d,%d) para (%d,%d). -1 ponto.%n",
                                    xAnterior,
                                    yAnterior,
                                    nave.getX(),
                                    nave.getY()
                            );
                        } else {
                            System.out.println("Movimento inválido: a nave chegou ao limite do mapa.");
                        }
                        break;

                    case 'c':
                        if (missao.todosEmbarcados()) {
                            System.out.println("Todos os passageiros já estão a bordo.");
                        } else {
                            Passageiro p = missao.passagemNaPosicao();

                            if (p == null) {
                                System.out.println("Nenhum passageiro nesta posição.");
                            } else if (missao.embarcarPassageiroNaPosicao()) {
                                score += p.getPontuacao();
                                System.out.println("Passageiro embarcado: " + p.getNome());
                                System.out.println("Tipo: " + p.getTipo());
                                System.out.println("+" + p.getPontuacao() + " pontos!");
                            } else {
                                System.out.println("Nave cheia, não foi possível embarcar.");
                            }
                        }
                        break;

                    case 'q':
                        partidaAtiva = false;
                        System.out.println("Missão abandonada.");
                        break;

                    default:
                        System.out.println("Comando desconhecido. Use w, s, a, d, c ou q.");
                        break;
                }

                if (!partidaAtiva) {
                    break;
                }

                if (score <= 0) {
                    System.out.println("Pontuação zerada. Missão perdida.");
                    System.out.printf("Pontuação final: %d%n", score);
                    partidaAtiva = false;
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
                    System.out.println();
                    System.out.println("!!! COLISÃO !!!");
                    System.out.println("Vidas restantes: " + nave.getVidas());

                    if (nave.getVidas() <= 0) {
                        long tempoJogo = (System.currentTimeMillis() - tempoInicio) / 1000;
                        exibirEstatisticas(
                                score,
                                movimentos,
                                tempoJogo,
                                nave.getPassageiros().size(),
                                ranking
                        );
                        System.out.println("Game Over!");
                        partidaAtiva = false;
                    } else {
                        nave.reposicionar(0, 0);
                        System.out.println("Nave reposicionada em (0,0).");
                    }
                }
            }

            if (missaoConcluida) {
                System.out.println("*** POUSO REALIZADO COM SUCESSO! ***");
                System.out.println("*** Missão concluída! Todos os passageiros foram salvos! ***");
            }

            if (!ranking.isEmpty()) {
                System.out.println();
                System.out.println("Ranking Top 5:");
                exibirRankingCompleto(ranking);
            }

            System.out.print("Deseja iniciar nova missão? (s/n): ");
            String resposta = scanner.nextLine().trim().toLowerCase();
            playAgain = resposta.equals("s") || resposta.equals("sim");
        }
    }

    private static Dificuldade selecionarDificuldade(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("Escolha a dificuldade:");
            System.out.println("1. Fácil   (2 passageiros, 3 asteroides, 30 pontos)");
            System.out.println("2. Normal  (6 passageiros, 5 asteroides, 20 pontos)");
            System.out.println("3. Difícil (9 passageiros, 8 asteroides, 15 pontos)");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    return Dificuldade.FACIL;
                case "2":
                    return Dificuldade.NORMAL;
                case "3":
                    return Dificuldade.DIFICIL;
                default:
                    System.out.println("Opção inválida. Escolha 1, 2 ou 3.");
            }
        }
    }

    private static int lerTamanhoMapa(Scanner scanner, Dificuldade dificuldade) {
        int minimoEntidades =
                1 +
                definirQtdPassageiros(dificuldade) +
                definirQtdAsteroides(dificuldade) +
                definirQtdInimigos(dificuldade);

        int tamanhoMinimo = 1;
        while ((2 * tamanhoMinimo + 1) * (2 * tamanhoMinimo + 1) < minimoEntidades) {
            tamanhoMinimo++;
        }

        while (true) {
            System.out.print(
                    "Tamanho do mapa (-X a +X, mínimo " + tamanhoMinimo + "): "
            );
            String valor = scanner.nextLine().trim();

            try {
                int tamanho = Integer.parseInt(valor);
                if (tamanho >= tamanhoMinimo) {
                    return tamanho;
                }

                System.out.println(
                        "Mapa muito pequeno. Digite um tamanho >= " + tamanhoMinimo + "."
                );
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    private static int definirPontuacaoInicial(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL:
                return 30;
            case DIFICIL:
                return 15;
            default:
                return 20;
        }
    }

    private static int definirQtdPassageiros(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL:
                return 2;
            case DIFICIL:
                return 9;
            default:
                return 6;
        }
    }

    private static int definirQtdAsteroides(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL:
                return 3;
            case DIFICIL:
                return 8;
            default:
                return 5;
        }
    }

    private static int definirQtdInimigos(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL:
                return 1;
            case DIFICIL:
                return 3;
            default:
                return 2;
        }
    }

    private static Missao criarNovaMissao(
            Random random,
            int minX,
            int maxX,
            int minY,
            int maxY,
            Dificuldade dificuldade) {

        int qtdPassageiros = definirQtdPassageiros(dificuldade);
        int qtdAsteroides = definirQtdAsteroides(dificuldade);
        int qtdInimigos = definirQtdInimigos(dificuldade);

        Nave nave = new Nave("A-1", qtdPassageiros, 3);
        Missao missao = new Missao(nave);

        while (missao.getPassageiros().size() < qtdPassageiros) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;

            if (posicaoOcupada(missao, x, y)) {
                continue;
            }

            int indice = missao.getPassageiros().size();
            missao.addPassageiro(criarPassageiroPolimorfico(indice, x, y));
        }

        while (missao.getAsteroides().size() < qtdAsteroides) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;

            if (posicaoOcupada(missao, x, y)) {
                continue;
            }

            missao.addAsteroide(new Asteroide(x, y));
        }

        while (missao.getInimigos().size() < qtdInimigos) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;

            if (posicaoOcupada(missao, x, y)) {
                continue;
            }

            missao.addInimigo(new Inimigo(x, y));
        }

        return missao;
    }

    private static Passageiro criarPassageiroPolimorfico(int indice, int x, int y) {
        switch (indice % 5) {
            case 0:
                return new Professor("Dr. Silva", x, y);
            case 1:
                return new Engenheiro("Eng. Rosa", x, y);
            case 2:
                return new Professor("Dr. Lima", x, y);
            case 3:
                return new Engenheiro("Eng. Carlos", x, y);
            default:
                return new Astronauta("Ast. Maria", x, y);
        }
    }

    private static boolean posicaoOcupada(Missao missao, int x, int y) {
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
            return true;
        }

        for (Passageiro p : missao.getPassageiros()) {
            if (p.getX() == x && p.getY() == y) {
                return true;
            }
        }

        for (Asteroide a : missao.getAsteroides()) {
            if (a.getX() == x && a.getY() == y) {
                return true;
            }
        }

        for (Inimigo i : missao.getInimigos()) {
            if (i.getX() == x && i.getY() == y) {
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

                if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
                    symbol = '@';
                } else {
                    for (Passageiro p : missao.getPassageiros()) {
                        if (p.getX() == x && p.getY() == y) {
                            if (p instanceof Engenheiro) {
                                symbol = 'E';
                            } else if (p instanceof Astronauta) {
                                symbol = 'T';
                            } else {
                                symbol = 'P';
                            }
                            break;
                        }
                    }

                    if (symbol == '.') {
                        for (Asteroide a : missao.getAsteroides()) {
                            if (a.getX() == x && a.getY() == y) {
                                symbol = '#';
                                break;
                            }
                        }
                    }

                    if (symbol == '.') {
                        for (Inimigo i : missao.getInimigos()) {
                            if (i.getX() == x && i.getY() == y) {
                                symbol = 'X';
                                break;
                            }
                        }
                    }

                    if (symbol == '.' && x == 0 && y == 0) {
                        symbol = 'L';
                    }
                }

                System.out.printf(" %2c", symbol);
            }

            System.out.println();
        }

        System.out.println(
                "Legenda: @=Nave, L=Plataforma, P=Professor, E=Engenheiro, T=Astronauta, #=Asteroide, X=Inimigo, .=Vazio"
        );
        System.out.println("Comandos: w/s/a/d mover, c embarcar, q sair");
        System.out.println();
    }

    private static void exibirEstatisticas(
            int score,
            int movimentos,
            long tempoSegundos,
            int passageiros,
            List<RankingEntry> ranking) {

        System.out.println();
        System.out.println("================ ESTATÍSTICAS ================");
        System.out.printf("Pontuação: %d%n", score);
        System.out.printf("Movimentos: %d%n", movimentos);
        System.out.printf("Tempo de jogo: %d segundos%n", tempoSegundos);
        System.out.printf("Passageiros coletados: %d%n", passageiros);

        if (ranking.isEmpty()) {
            System.out.println("Primeira pontuação registrada nesta sessão.");
        } else {
            int recorde = ranking.get(0).score;
            System.out.printf(
                    "Recorde atual: %d pontos (%s)%n",
                    recorde,
                    ranking.get(0).name
            );

            if (score > recorde) {
                System.out.println("NOVO RECORDE!");
            }
        }

        System.out.println("===============================================");
    }

    private static void salvarRankingSeMerece(
            List<RankingEntry> ranking,
            Path rankingPath,
            String pilotoNome,
            int score,
            Dificuldade dificuldade,
            int passageirosColetados,
            long tempoJogo,
            int movimentos) {

        if (score <= 0) {
            return;
        }

        if (!isTopScore(ranking, score)) {
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
                                (RankingEntry e) -> e.score
                        ).reversed()
                )
                .limit(5)
                .collect(Collectors.toList());

        ranking.clear();
        ranking.addAll(ordenado);
        saveRanking(rankingPath, ranking);

        System.out.println("Pontuação salva no Ranking Top 5!");
    }

    private static boolean isTopScore(List<RankingEntry> ranking, int score) {
        if (ranking.size() < 5) {
            return true;
        }

        return score > ranking.get(ranking.size() - 1).score;
    }

    private static void exibirRankingCompleto(List<RankingEntry> ranking) {
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
                        entry.name,
                        entry.score,
                        entry.dificuldade,
                        entry.passageirosColetados,
                        entry.movimentos,
                        entry.tempoJogo,
                        entry.dataHora
                );
            }
        }

        System.out.println("================================================");
    }

    private static List<RankingEntry> resetarRanking(
            Scanner scanner,
            Path rankingPath) {

        System.out.print(
                "Você realmente deseja apagar o histórico de ranking? (s/n): "
        );
        String confirmacao = scanner.nextLine().trim().toLowerCase();

        if (confirmacao.equals("s") || confirmacao.equals("sim")) {
            try {
                Files.deleteIfExists(rankingPath);
                System.out.println("Histórico de ranking resetado!");
            } catch (IOException e) {
                System.out.println("Erro ao apagar ranking: " + e.getMessage());
            }
            return new ArrayList<>();
        }

        System.out.println("Operação cancelada.");
        return rankingPath.toFile().exists()
                ? loadRanking(rankingPath)
                : new ArrayList<>();
    }

    private static List<RankingEntry> loadRanking(Path path) {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try {
            String json = new String(
                    Files.readAllBytes(path),
                    StandardCharsets.UTF_8
            ).trim();

            return parseRankingJson(json);
        } catch (IOException e) {
            System.out.println("Não foi possível carregar o ranking: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void saveRanking(Path path, List<RankingEntry> ranking) {
        StringBuilder builder = new StringBuilder();
        builder.append("[\n");

        for (int i = 0; i < ranking.size(); i++) {
            RankingEntry entry = ranking.get(i);

            builder.append("  {")
                    .append("\"name\":\"").append(escapeJson(entry.name)).append("\",")
                    .append("\"score\":").append(entry.score).append(",")
                    .append("\"dificuldade\":\"").append(entry.dificuldade.name()).append("\",")
                    .append("\"passageirosColetados\":").append(entry.passageirosColetados).append(",")
                    .append("\"dataHora\":\"").append(escapeJson(entry.dataHora)).append("\",")
                    .append("\"tempoJogo\":").append(entry.tempoJogo).append(",")
                    .append("\"movimentos\":").append(entry.movimentos)
                    .append("}");

            if (i < ranking.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }

        builder.append("]\n");

        try {
            Files.write(
                    path,
                    builder.toString().getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            System.out.println(
                    "Não foi possível salvar o ranking: " + e.getMessage()
            );
        }
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static List<RankingEntry> parseRankingJson(String json) {
        List<RankingEntry> ranking = new ArrayList<>();

        if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
            return ranking;
        }

        Pattern objectPattern = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
        Matcher matcher = objectPattern.matcher(json);

        while (matcher.find()) {
            String object = matcher.group(1);

            String name = extrairString(object, "name");
            Integer score = extrairInt(object, "score");
            String dificuldadeTexto = extrairString(object, "dificuldade");
            Integer passageiros = extrairInt(object, "passageirosColetados");
            String dataHora = extrairString(object, "dataHora");
            Long tempo = extrairLong(object, "tempoJogo");
            Integer movimentos = extrairInt(object, "movimentos");

            if (name == null || score == null) {
                continue;
            }

            Dificuldade dificuldade = Dificuldade.deString(dificuldadeTexto);
            int passageirosColetados = passageiros == null ? 0 : passageiros;
            String data = dataHora == null ? "" : dataHora;
            long tempoJogo = tempo == null ? 0L : tempo;
            int qtdMovimentos = movimentos == null ? 0 : movimentos;

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
                        (RankingEntry e) -> e.score
                ).reversed()
        );

        if (ranking.size() > 5) {
            ranking = ranking.subList(0, 5);
        }

        return new ArrayList<>(ranking);
    }

    private static String extrairString(String objeto, String chave) {
        Pattern p = Pattern.compile(
                "\\\"" + Pattern.quote(chave) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\""
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

    private static Integer extrairInt(String objeto, String chave) {
        Long valor = extrairLong(objeto, chave);
        return valor == null ? null : valor.intValue();
    }

    private static Long extrairLong(String objeto, String chave) {
        Pattern p = Pattern.compile(
                "\\\"" + Pattern.quote(chave) + "\\\"\\s*:\\s*(-?\\d+)"
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

    private static void pausar(Scanner scanner) {
        System.out.println();
        System.out.print("Pressione Enter para continuar...");
        scanner.nextLine();
    }

    private static class RankingEntry {
        private final String name;
        private final int score;
        private final Dificuldade dificuldade;
        private final int passageirosColetados;
        private final String dataHora;
        private final long tempoJogo;
        private final int movimentos;

        private RankingEntry(
                String name,
                int score,
                Dificuldade dificuldade,
                int passageirosColetados,
                String dataHora,
                long tempoJogo,
                int movimentos) {
            this.name = name;
            this.score = score;
            this.dificuldade = dificuldade;
            this.passageirosColetados = passageirosColetados;
            this.dataHora = dataHora;
            this.tempoJogo = tempoJogo;
            this.movimentos = movimentos;
        }
    }
}
