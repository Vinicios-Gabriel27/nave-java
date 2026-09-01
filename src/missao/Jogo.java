package missao;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Jogo {

    private final Random random;
    private final Scanner scanner;
    private final Menu menu;
    private final GeradorMissao geradorMissao;
    private final Mapa mapa;
    private final Ranking ranking;

    public Jogo() {
        random = new Random();
        scanner = new Scanner(System.in);
        menu = new Menu();
        geradorMissao = new GeradorMissao();
        mapa = new Mapa();

        Path rankingPath = Paths.get("ranking.json");

        ranking = new Ranking(rankingPath);
    }

    public void iniciar() {
        boolean executando = true;

        while (executando) {
            menu.exibirMenuInicial();

            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    iniciarNovaMissao();
                    break;

                case "2":
                    ranking.exibirRankingCompleto();
                    menu.pausar(scanner);
                    break;

                case "3":
                    ranking.resetarRanking(scanner);
                    break;

                case "4":
                    executando = false;
                    System.out.println("Saindo do jogo. Até a próxima missão!");
                    break;

                default:
                    System.out.println("Opção inválida. Tente novamente.");
                    menu.pausar(scanner);
                    break;
            }
        }

        scanner.close();
    }

    private void iniciarNovaMissao() {
        System.out.println();
        System.out.print("Digite o nome do piloto: ");

        String pilotoNome = scanner.nextLine().trim();

        if (pilotoNome.isEmpty()) {
            pilotoNome = "Piloto Anônimo";
        }

        Dificuldade dificuldade = menu.selecionarDificuldade(scanner);
        int tamanho = menu.lerTamanhoMapa(scanner, dificuldade);

        int minX = -tamanho;
        int maxX = tamanho;
        int minY = -tamanho;
        int maxY = tamanho;

        menu.exibirObjetivo();
        menu.pausar(scanner);

        boolean playAgain = true;

        while (playAgain) {
            Missao missao = geradorMissao.criarNovaMissao(
                    random,
                    minX,
                    maxX,
                    minY,
                    maxY,
                    dificuldade
            );

            Nave nave = missao.getNave();
            int score = GeradorMissao.definirPontuacaoInicial(dificuldade);
            int movimentos = 0;
            int passageirosPlanejados = GeradorMissao.definirQtdPassageiros(dificuldade);
            long tempoInicio = System.currentTimeMillis();
            boolean partidaAtiva = true;
            boolean missaoConcluida = false;

            while (partidaAtiva) {
                mapa.desenharMapa(
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
                        long tempoJogo =
                                (System.currentTimeMillis() - tempoInicio) / 1000;

                        ranking.exibirEstatisticas(
                                score,
                                movimentos,
                                tempoJogo,
                                nave.getPassageiros().size()
                        );

                        missaoConcluida = true;
                        partidaAtiva = false;

                        ranking.salvarRankingSeMerece(
                                pilotoNome,
                                score,
                                dificuldade,
                                nave.getPassageiros().size(),
                                tempoJogo,
                                movimentos
                        );

                        break;
                    } else {
                        System.out.println(
                                ">> Todos embarcados! Retorne para a plataforma em (0,0)."
                        );
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

                        if (nave.moverComLimites(
                                cmd,
                                minX,
                                maxX,
                                minY,
                                maxY)) {

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
                            System.out.println(
                                    "Movimento inválido: a nave chegou ao limite do mapa."
                            );
                        }
                        break;

                    case 'c':
                        if (missao.todosEmbarcados()) {
                            System.out.println(
                                    "Todos os passageiros já estão a bordo."
                            );
                        } else {
                            Passageiro p = missao.passagemNaPosicao();

                            if (p == null) {
                                System.out.println(
                                        "Nenhum passageiro nesta posição."
                                );
                            } else if (missao.embarcarPassageiroNaPosicao()) {
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
                            } else {
                                System.out.println(
                                        "Nave cheia, não foi possível embarcar."
                                );
                            }
                        }
                        break;

                    case 'q':
                        partidaAtiva = false;
                        System.out.println("Missão abandonada.");
                        break;

                    default:
                        System.out.println(
                                "Comando desconhecido. Use w, s, a, d, c ou q."
                        );
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
                    System.out.println(
                            "Vidas restantes: " + nave.getVidas()
                    );

                    if (nave.getVidas() <= 0) {
                        long tempoJogo =
                                (System.currentTimeMillis() - tempoInicio) / 1000;

                        ranking.exibirEstatisticas(
                                score,
                                movimentos,
                                tempoJogo,
                                nave.getPassageiros().size()
                        );

                        System.out.println("Game Over!");
                        partidaAtiva = false;
                    } else {
                        nave.reposicionar(0, 0);
                        System.out.println(
                                "Nave reposicionada em (0,0)."
                        );
                    }
                }
            }

            if (missaoConcluida) {
                System.out.println(
                        "*** POUSO REALIZADO COM SUCESSO! ***"
                );
                System.out.println(
                        "*** Missão concluída! Todos os passageiros foram salvos! ***"
                );
            }

            if (!ranking.getRanking().isEmpty()) {
                System.out.println();
                System.out.println("Ranking Top 5:");
                ranking.exibirRankingCompleto();
            }

            System.out.print("Deseja iniciar nova missão? (s/n): ");
            String resposta = scanner.nextLine().trim().toLowerCase();

            playAgain = resposta.equals("s") || resposta.equals("sim");
        }
    }
}
