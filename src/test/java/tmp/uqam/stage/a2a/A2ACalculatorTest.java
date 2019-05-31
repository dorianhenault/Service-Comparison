package tmp.uqam.stage.a2a;

import org.junit.Before;
import org.junit.Test;
import tmp.uqam.stage.TestScenarios;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class A2ACalculatorTest extends TestScenarios {

    private A2ACalculator comparator;

    @Before
    public void setUp() {
        comparator = new A2ACalculator();
    }

    @Test
    public void testSameArchitecture() {
        sameArchitecture();

        assertThat(comparator.a2aResult(slicing1, slicing2), equalTo(100.0));

    }

    @Test
    public void testTotallyDifferentArchitecture() {
        totallyDifferentArchitecture();

        // Not 0 because the number of service is close
        assertThat(comparator.a2aResult(slicing1, slicing2), closeTo(17.0,5));
    }

    @Test
    public void testSameClassesDifferentArchi() {
        sameClassesDifferentArchitecture();

        assertThat(comparator.a2aResult(slicing1, slicing2), closeTo(90.0, 5));

    }

    @Test
    public void testCheckBestSimilitudeDifferentSize() {
        sameClassesDifferentSize();

        assertThat(comparator.a2aResult(slicing1, slicing2), closeTo(55, 5));
    }

    @Test
    public void testCheckBestSimilitudeDifficult() {
        complex();

        assertThat(comparator.a2aResult(slicing1, slicing2), closeTo(55, 5));
    }
}
