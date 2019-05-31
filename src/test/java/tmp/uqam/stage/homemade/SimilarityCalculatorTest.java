package tmp.uqam.stage.homemade;

import org.junit.Before;
import org.junit.Test;
import tmp.uqam.stage.TestScenarios;
import tmp.uqam.stage.structure.ServicePair;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class SimilarityCalculatorTest extends TestScenarios {


    private SimilarityCalculator comparator;


    @Before
    public void setUp() {
        comparator = new SimilarityCalculator();
    }

    @Test
    public void testSameArchitecture() {
        sameArchitecture();

        assertThat(comparator.compare(slicing1, slicing2), equalTo(100.0));

    }

    @Test
    public void testTotallyDifferentArchitecture() {
        totallyDifferentArchitecture();

        assertThat(comparator.compare(slicing1, slicing2), equalTo(0.0));
    }

    @Test
    public void testSameClassesDifferentArchi() {
        sameClassesDifferentArchitecture();

        assertThat(comparator.compare(slicing1, slicing2), closeTo(45.0,5));

    }

    @Test
    public void testCheckBestSimilitudeDifferentSize() {
        sameClassesDifferentSize();

        comparator.initSimilitudePairs(slicing1, slicing2);
        assertThat(comparator.getPairs(), hasItem(new ServicePair(s2, ss1)));

        assertThat(comparator.compare(slicing1, slicing2), closeTo(33, 5));
    }

    @Test
    public void testCheckBestSimilitudeDifficult() {
        complex();

        comparator.initSimilitudePairs(slicing1, slicing2);

        assertThat(comparator.getPairs(), hasItem(new ServicePair(s1, ss1)));
        assertThat(comparator.getPairs(), hasItem(new ServicePair(s2, ss4)));
        assertThat(comparator.getPairs(), hasItem(new ServicePair(s3, ss3)));

        assertThat(comparator.compare(slicing1, slicing2), closeTo(25, 5));
    }

    @Test
    public void testCheckBestSimilitude1() {
        s1.add("C1");
        s1.add("C2");

        s2.add("C4");
        s2.add("C3");

        s3.add("C5");
        slicing1 = Stream.of(s1, s2, s3).collect(Collectors.toSet());

        ss1.add("C5");
        ss1.add("C10");

        ss2.add("C4");
        ss2.add("C21");

        ss3.add("C1");
        ss3.add("C2");

        slicing2 = Stream.of(ss1, ss2, ss3).collect(Collectors.toSet());

        comparator.initSimilitudePairs(slicing1, slicing2);
        assertThat(comparator.getPairs(), hasItem(new ServicePair(s1, ss3)));
        assertThat(comparator.getPairs(), hasItem(new ServicePair(s3, ss1)));
        assertThat(comparator.getPairs(), hasItem(new ServicePair(s2, ss2)));

        assertThat(comparator.compare(slicing1, slicing2), closeTo(60, 5));
    }
}
