package missao;

import java.util.Random;

public class GeradorMissao {

    public static int definirPontuacaoInicial(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL:
                return 30;
            case DIFICIL:
                return 15;
            default:
                return 20;
        }
    }

    public static int definirQtdPassageiros(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL:
                return 2;
            case DIFICIL:
                return 9;
            default:
                return 6;
        }
    }

    public static int definirQtdAsteroides(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL:
                return 3;
            case DIFICIL:
                return 8;
            default:
                return 5;
        }
    }

    public static int definirQtdInimigos(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL:
                return 1;
            case DIFICIL:
                return 3;
            default:
                return 2;
        }
    }

    public Missao criarNovaMissao(
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
            missao.addPassageiro(
                    criarPassageiroPolimorfico(indice, x, y)
            );
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

    private Passageiro criarPassageiroPolimorfico(
            int indice,
            int x,
            int y) {

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

    private boolean posicaoOcupada(Missao missao, int x, int y) {
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
}
