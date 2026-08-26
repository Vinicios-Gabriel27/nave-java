package missao;

public enum Dificuldade {
    FACIL("Fácil"),
    NORMAL("Normal"),
    DIFICIL("Difícil");

    private final String descricao;

    Dificuldade(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Dificuldade deString(String s) {
        if (s == null) {
            return NORMAL;
        }

        switch (s.trim().toLowerCase()) {
            case "facil":
            case "fácil":
                return FACIL;
            case "dificil":
            case "difícil":
                return DIFICIL;
            case "normal":
            case "medio":
            case "médio":
                return NORMAL;
            default:
                return NORMAL;
        }
    }

    @Override
    public String toString() {
        return descricao;
    }
}
