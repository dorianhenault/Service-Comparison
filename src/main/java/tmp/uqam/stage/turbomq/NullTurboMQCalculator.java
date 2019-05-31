package tmp.uqam.stage.turbomq;

import tmp.uqam.stage.structure.Service;

import java.util.Set;

/**
 * Helper class to create an empty TurboMQCalculator if no KDM model was provided
 */
public class NullTurboMQCalculator extends TurboMQCalculator {

    @Override
    public double calculateTurboMQ(Set<Service> sample) {
        return 0.0;
    }
}
