package missao;

public enum Dificuldade {
    FACIL, NORMAL, DIFICIL;

    public static Dificuldade deString(String s) {
        if (s == null) return NORMAL;
        switch (s.trim().toLowerCase()) {
            case "facil":
            case "fácil":
                return FACIL;
            case "dificil":
            case "difícil":
                return DIFICIL;
            default:
                return NORMAL;
        }
    }
}
