package tmp.uqam.stage.structure;

import tmp.uqam.stage.mojo.MojoFMResult;

/**
 * Utility class to store all the results of the comparison metrics
 */
public class ComparisonResult {

    private Double similarity;
    private MojoFMResult mojoFM;
    private Double a2a;
    private Double turboMQ;

    public ComparisonResult(Double similarity, MojoFMResult mojoFM, Double a2a, Double turboMQ) {
        this.similarity = similarity;
        this.mojoFM = mojoFM;
        this.a2a = a2a;
        this.turboMQ = turboMQ;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public MojoFMResult getMojoFM() {
        return mojoFM;
    }

    public Double getA2a() {
        return a2a;
    }

    public Double getTurboMQ() {
        return turboMQ;
    }

    @Override
    public String toString() {
        return "similarity=" + String.format("%.4f", similarity) +
                ", mojoFM=" + mojoFM +
                ", a2a=" + String.format("%.4f", a2a) +
                ", turboMQ=" + String.format("%.4f", turboMQ);
    }
}
