package missao;

import java.util.Scanner;

public class Menu {

    public void exibirMenuInicial() {
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

    public Dificuldade selecionarDificuldade(Scanner scanner) {
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

    public int lerTamanhoMapa(Scanner scanner, Dificuldade dificuldade) {
        int minimoEntidades = 1
                + GeradorMissao.definirQtdPassageiros(dificuldade)
                + GeradorMissao.definirQtdAsteroides(dificuldade)
                + GeradorMissao.definirQtdInimigos(dificuldade);

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

    public void exibirObjetivo() {
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
    }

    public void pausar(Scanner scanner) {
        System.out.println();
        System.out.print("Pressione Enter para continuar...");
        scanner.nextLine();
    }
}
