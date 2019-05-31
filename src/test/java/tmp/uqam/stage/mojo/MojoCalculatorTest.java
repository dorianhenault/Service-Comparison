package tmp.uqam.stage.mojo;

import org.junit.Before;
import org.junit.Test;
import tmp.uqam.stage.TestScenarios;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MojoCalculatorTest extends TestScenarios {

    private MoJoCalculator comparator;
    private MojoFMResult result;

    @Before
    public void setUp() {
        comparator = new MoJoCalculator();
    }

    @Test
    public void testSameArchitecture() {
        sameArchitecture();

        result = comparator.mojofm(slicing1, slicing2);
        assertThat(result.getResult(), equalTo(100.0));
        assertThat(result.getInefficiency(), equalTo(0));
    }

    @Test
    public void testTotallyDifferentArchitecture() {
        totallyDifferentArchitecture();

        result = comparator.mojofm(slicing1, slicing2);
        assertThat(result.getResult(), equalTo(0.0));
        assertThat(result.getInefficiency(), equalTo(0));

    }

    @Test
    public void testSameClassesDifferentArchi() {
        sameClassesDifferentArchitecture();

        result = comparator.mojofm(slicing1, slicing2);
        assertThat(result.getResult(), closeTo(33.3, 5));
        assertThat(result.getInefficiency(), equalTo(0));

    }

    @Test
    public void testCheckBestSimilitudeDifferentSize() {
        sameClassesDifferentSize();

        result = comparator.mojofm(slicing1, slicing2);
        assertThat(result.getResult(), closeTo(25.0,5));
        assertThat(result.getInefficiency(), equalTo(0));
    }

    @Test
    public void testCheckBestSimilitudeDifficult() {
        complex();

        result = comparator.mojofm(slicing1, slicing2);
        // Inefficency cause bad result
        assertThat(result.getResult(), closeTo(0.0,5));
        assertThat(result.getInefficiency(), equalTo(6));
    }
}
