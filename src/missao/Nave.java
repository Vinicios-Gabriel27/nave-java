package missao;

import java.util.ArrayList;
import java.util.List;

public class Nave {
    private String id;
    private int vidas;
    private int x;
    private int y;
    private int capacidade;
    private List<Passageiro> passageiros = new ArrayList<>();

    public Nave(String id, int capacidade, int vidas) {
        this.id = id;
        this.vidas = vidas;
        this.capacidade = capacidade;
        this.x = 0;
        this.y = 0;
    }

    public String getId() {
        return id;
    }

    public int getVidas() {
        return vidas;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public List<Passageiro> getPassageiros() {
        return passageiros;
    }

    public void moveUp() {
        y--;
    }

    public void moveDown() {
        y++;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    public boolean moverComLimites(char comando, int minX, int maxX, int minY, int maxY) {
        int novoX = x;
        int novoY = y;

        switch (Character.toLowerCase(comando)) {
            case 'w':
                novoY--;
                break;
            case 's':
                novoY++;
                break;
            case 'a':
                novoX--;
                break;
            case 'd':
                novoX++;
                break;
            default:
                return false;
        }

        if (novoX < minX || novoX > maxX || novoY < minY || novoY > maxY) {
            return false;
        }

        x = novoX;
        y = novoY;
        return true;
    }

    public boolean embarcar(Passageiro p) {
        if (p == null) {
            return false;
        }

        if (passageiros.size() < capacidade) {
            passageiros.add(p);
            return true;
        }

        return false;
    }

    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }

    public void reposicionar(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
