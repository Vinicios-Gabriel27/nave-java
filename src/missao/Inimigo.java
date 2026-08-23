package missao;

import java.util.Random;

public class Inimigo {
    private int x;
    private int y;

    public Inimigo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    private void moverAleatorio(Random r, int minX, int maxX, int minY, int maxY) {
        int direcao = r.nextInt(4);
        switch (direcao) {
            case 0: if (y > minY) y--; break;
            case 1: if (y < maxY) y++; break;
            case 2: if (x > minX) x--; break;
            case 3: if (x < maxX) x++; break;
        }
    }


    private void moverPerseguindo(Nave nave, int minX, int maxX, int minY, int maxY) {
        int diffX = nave.getX() - x;
        int diffY = nave.getY() - y;

        if (Math.abs(diffX) >= Math.abs(diffY)) {
            if (diffX > 0 && x < maxX) {
                x++;
            } else if (diffX < 0 && x > minX) {
                x--;
            } else if (diffY > 0 && y < maxY) {
                y++;
            } else if (diffY < 0 && y > minY) {
                y--;
            }
        } else {
            if (diffY > 0 && y < maxY) {
                y++;
            } else if (diffY < 0 && y > minY) {
                y--;
            } else if (diffX > 0 && x < maxX) {
                x++;
            } else if (diffX < 0 && x > minX) {
                x--;
            }
        }
    }


    public void moverComChance(Random r, Nave nave, int minX, int maxX, int minY, int maxY, double chancePerseguir) {
        if (r.nextDouble() < chancePerseguir) {
            moverPerseguindo(nave, minX, maxX, minY, maxY);
        } else {
            moverAleatorio(r, minX, maxX, minY, maxY);
        }
    }

    public boolean colideCom(Nave n) {
        return n.getX() == x && n.getY() == y;
    }
}