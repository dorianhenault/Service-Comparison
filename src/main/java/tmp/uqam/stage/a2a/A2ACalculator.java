package tmp.uqam.stage.a2a;

import tmp.uqam.stage.structure.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to calculate the ArchitectureToArchitecture (A2A metric)
 */
public class A2ACalculator {

    private Set<Service> reference;
    private Set<Service> sample;

    /**
     * Main calculation of the algorithm
     *
     * @param reference reference model
     * @param sample model to confront to the reference
     * @return a percentage a2a value
     */
    public double a2aResult(Set<Service> reference, Set<Service> sample) {
        this.sample = new HashSet<>();
        this.reference = new HashSet<>();

        // copy sets of services
        sample.forEach(service -> this.sample.add(new Service(service)));
        reference.forEach(service -> this.reference.add(new Service(service)));

        int acoRef = aco(reference);
        int acoSample = aco(sample);
        double mto = mto();
        return (1 - (mto / (acoRef + acoSample))) * 100;
    }

    /**
     * Calculates the number of steps to reach an architecture from an empty one
     * @param architecture the architecture to reach
     * @return the number of moves and cluster creations
     */
    private int aco(Set<Service> architecture) {
        return architecture.size() + (architecture.stream().mapToInt(Service::size).sum()*2);
    }

    /**
     * Calculates the number of moves/addition/deletions to go from the sample architecture to the reference one
     * @return the number of steps
     */
    private int mto() {
        int movE = new MovesCalculator(reference, sample).calculateMovesAndTrimObjects();

        reference = reference.stream().filter(service -> !service.getClasses().isEmpty()).collect(Collectors.toSet());
        sample = sample.stream().filter(service -> !service.getClasses().isEmpty()).collect(Collectors.toSet());

        int addE = reference.stream().mapToInt(Service::size).sum();
        int remE = sample.stream().mapToInt(Service::size).sum();
        // clean empty services

        // compute addC or remC
        int addremC;
        int diffServices = reference.size() - sample.size();
        if (diffServices >= 0) {
            addremC = diffServices;
        } else {
            addremC = -diffServices;
        }
        return addremC + remE + addE + movE;
    }
}
