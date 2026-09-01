package missao;

public class Mapa {

    public void desenharMapa(
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
}
