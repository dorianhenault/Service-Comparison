package tmp.uqam.stage.turbomq;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import tmp.uqam.stage.TestScenarios;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

public class TurboMQTest extends TestScenarios {

    private TurboMQCalculator turboMQCalculator;

    @Before
    public void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("test/miniTest_kdm.xmi").getFile());
        turboMQCalculator = new TurboMQCalculator(file.getAbsolutePath(), "miniTest");
    }

    @Test
    public void testBadArchi() {
        s1.add("C1");
        s1.add("C2");
        s1.add("C3");

        s2.add("I1");
        s2.add("E1");

        s3.add("Main");
        slicing1 = Stream.of(s1, s2, s3).collect(Collectors.toSet());

        assertThat(turboMQCalculator.calculateTurboMQ(slicing1), closeTo(0.15, 0.05));
    }

    @Test
    public void testOkArchi() {
        ss1.add("C1");
        ss1.add("I1");

        ss2.add("C3");
        ss2.add("C2");
        ss2.add("E1");

        ss3.add("Main");

        slicing1 = Stream.of(ss1, ss2, ss3).collect(Collectors.toSet());

        assertThat(turboMQCalculator.calculateTurboMQ(slicing1), closeTo(0.5, 0.05));
    }
}
