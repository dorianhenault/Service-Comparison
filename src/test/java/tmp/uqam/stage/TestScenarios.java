package tmp.uqam.stage;

import org.junit.Before;
import tmp.uqam.stage.structure.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestScenarios {
    protected Service s1;
    protected Service s2;
    protected Service s3;
    protected Service ss1;
    protected Service ss2;
    protected Service ss3;
    protected Service ss4;
    protected Set<Service> slicing1;
    protected Set<Service> slicing2;

    @Before
    public void setUpSuper() {
        s1 = new Service();
        s2 = new Service();
        s3 = new Service();
        ss1 = new Service();
        ss2 = new Service();
        ss3 = new Service();
        ss4 = new Service();
    }

    public void sameArchitecture() {
        s1.add("C1");
        s1.add("C2");

        s2.add("C4");
        s2.add("C3");

        s3.add("C5");
        slicing1 = Stream.of(s1, s2, s3).collect(Collectors.toSet());

        ss1.add("C1");
        ss1.add("C2");

        ss2.add("C3");
        ss2.add("C4");

        ss3.add("C5");
        slicing2 = Stream.of(ss2, ss1, ss3).collect(Collectors.toSet());
    }

    public void totallyDifferentArchitecture() {
        s1.add("C1");
        s1.add("C2");

        s2.add("C4");
        s2.add("C3");

        s3.add("C5");
        slicing1 = Stream.of(s1, s2, s3).collect(Collectors.toSet());

        ss1.add("C7");
        ss1.add("C8");

        ss2.add("C9");
        ss2.add("C10");

        slicing2 = Stream.of(ss1, ss2).collect(Collectors.toSet());
    }

    public void sameClassesDifferentArchitecture() {
        s1.add("C1");
        s1.add("C2");

        s2.add("C4");
        s2.add("C3");

        s3.add("C5");
        slicing1 = Stream.of(s1, s2, s3).collect(Collectors.toSet());

        ss1.add("C5");
        ss1.add("C3");

        ss2.add("C4");
        ss2.add("C2");

        ss3.add("C1");

        slicing2 = Stream.of(ss1, ss2, ss3).collect(Collectors.toSet());
    }

    public void sameClassesDifferentSize() {
        s1.add("C1");
        s1.add("C2");

        s2.add("C4");
        s2.add("C3");

        s3.add("C5");
        slicing1 = Stream.of(s1, s2, s3).collect(Collectors.toSet());

        ss1.add("C4");
        ss1.add("C3");

        slicing2 = Stream.of(ss1).collect(Collectors.toSet());
    }

    public void complex() {
        s1.add("C1");
        s1.add("C2");

        s2.add("C2");
        s2.add("C3");

        s3.add("C5");
        s3.add("C8");
        slicing1 = Stream.of(s1, s2, s3).collect(Collectors.toSet());

        ss1.add("C2");
        ss1.add("C5");

        ss2.add("C4");

        ss3.add("C5");
        ss3.add("C20");
        ss3.add("C1");

        ss4.add("C1");
        ss4.add("C2");
        ss4.add("C3");
        ss4.add("C4");
        ss4.add("C5");
        slicing2 = Stream.of(ss1, ss2, ss3, ss4).collect(Collectors.toSet());
    }
}

