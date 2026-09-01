package missao;

public class RankingEntry {

    private final String name;
    private final int score;
    private final Dificuldade dificuldade;
    private final int passageirosColetados;
    private final String dataHora;
    private final long tempoJogo;
    private final int movimentos;

    public RankingEntry(
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

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public int getPassageirosColetados() {
        return passageirosColetados;
    }

    public String getDataHora() {
        return dataHora;
    }

    public long getTempoJogo() {
        return tempoJogo;
    }

    public int getMovimentos() {
        return movimentos;
    }
}
