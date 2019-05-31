package tmp.uqam.stage.turbomq;

import tmp.uqam.stage.kdmparser.ClassPair;
import tmp.uqam.stage.kdmparser.KDMMetaModelParser;
import tmp.uqam.stage.kdmparser.MetaModelParser;
import tmp.uqam.stage.structure.Service;

import java.util.Map;
import java.util.Set;

/**
 * Class to calculate the turboMQ metric
 */
public class TurboMQCalculator {

    private Map<ClassPair, Double> linkMap;

    /**
     * Extract the model from the file provided into the linkMap
     *
     * @param modelLocation file path
     * @param modelName     model name
     */
    public TurboMQCalculator(String modelLocation, String modelName) {
        MetaModelParser parser = new KDMMetaModelParser(modelLocation, modelName);
        this.linkMap = parser.extractMetaModel();
    }

    protected TurboMQCalculator() {
    }

    /**
     * Calculate the metric as a double (not a percentage but a score)
     *
     * @param sample the service to test (does not need ground truth)
     * @return the score
     */
    public double calculateTurboMQ(Set<Service> sample) {
        return sample.stream().mapToDouble(this::clusterFactor).average().getAsDouble();
    }

    /**
     * The cluster Factor to apply to every cluster to get their cohesion vs coupling
     *
     * @param cluster the cluster to test
     * @return the cluster factor score
     */
    public double clusterFactor(Service cluster) {
        double mu = intraRelationships(cluster);
        double epsilon = interRelationships(cluster);
        if (mu == 0 && epsilon == 0) {
            return 0;
        }
        return (mu / (mu + 0.5 * epsilon));
    }

    /**
     * Relationships inside a cluster
     *
     * @param cluster the cluster to test
     * @return the weighted number of relationships
     */
    private double intraRelationships(Service cluster) {
        double intraRelationShipWeight = 0;
        for (ClassPair classPair : linkMap.keySet()) {
            if (cluster.containsPair(classPair)) {
                intraRelationShipWeight += linkMap.get(classPair);
            }
        }
        return intraRelationShipWeight;
    }

    /**
     * RelationShips between clusters
     *
     * @param cluster the cluster to test
     * @return the weight of relationships between this cluster and the other
     */
    private double interRelationships(Service cluster) {
        double interRelationShipWeight = 0;
        for (ClassPair classPair : linkMap.keySet()) {
            if (cluster.contains(classPair.getClass1()) && !cluster.contains(classPair.getClass2())
                    || (!cluster.contains(classPair.getClass1()) && cluster.contains(classPair.getClass2()))) {
                interRelationShipWeight += linkMap.get(classPair);
            }
        }
        return interRelationShipWeight;
    }
}
