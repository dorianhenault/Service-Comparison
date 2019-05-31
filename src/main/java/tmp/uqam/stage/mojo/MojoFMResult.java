package tmp.uqam.stage.mojo;

/**
 * Utility structure class to represent a mojoFM result (double) and the number of duplicated class to show
 * the potential impact on the result
 */
public class MojoFMResult {

    private double result;
    private int inefficiency;

    public MojoFMResult(double result, int inefficiency) {
        this.result = result;
        this.inefficiency = inefficiency;
    }

    public double getResult() {
        return result;
    }

    public int getInefficiency() {
        return inefficiency;
    }

    @Override
    public String toString() {
        if (inefficiency == 0) {
            return String.format("%.4f", result);
        } else {
            return String.format("%.4f", result) + "[" + inefficiency + " duplicates]";
        }
    }
}
