package tmp.uqam.stage.a2a;

import org.jgrapht.Graph;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import tmp.uqam.stage.structure.Service;
import tmp.uqam.stage.structure.ServicePair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that implements a bipartite graph to compute the optimal number of moves to transform an architecture
 */
public class MovesCalculator {

    private Set<Service> reference;
    private Set<Service> sample;
    private Graph<Service, DefaultWeightedEdge> g;
    private final Map<ServicePair, Integer> serviceCoupling;

    /**
     * Initialize the graph and the architectures
     * @param reference ref archi
     * @param sample sample archi
     */
    public MovesCalculator(Set<Service> reference, Set<Service> sample) {
        g = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        this.reference = reference;
        this.sample = sample;
        // Build th graph
        serviceCoupling = new HashMap<>();
        buildGraph();
    }

    /**
     * Initialize the bipartite graph
     */
    private void buildGraph() {
        for (Service refService : reference) {
            for (Service sampleService : sample) {
                serviceCoupling.put(new ServicePair(refService, sampleService), refService.intersect(sampleService).size());
            }
        }

        // Add to graph and add group to prevent the equals to trigger if the services have the same structure
        for (Service s : reference) {
            s.setGroup(1);
            g.addVertex(s);
        }

        for (Service s : sample) {
            s.setGroup(2);
            g.addVertex(s);
        }
    }

    /**
     * Utility method to add a weighted edge
     * @param s1 Service 1
     * @param s2 Service 2
     * @param weight number of intersections
     */
    private void addEdge(Service s1, Service s2, Integer weight) {
        DefaultWeightedEdge e = g.addEdge(s1, s2);
        g.setEdgeWeight(e, weight);
    }

    /**
     * Calculates the minimum number of moves to do to reach the ref archi and removes the items moved
     * @return the minimum number of moves
     */
    public int calculateMovesAndTrimObjects() {
        // Set the weight according to the intersection of each service pair
        for (Map.Entry<ServicePair, Integer> servicePair : serviceCoupling.entrySet()) {
            addEdge(servicePair.getKey().getService1(), servicePair.getKey().getService2(), servicePair.getValue());
        }

        // Apply the algorithm to maximize the weight of the matching pairs
        MaximumWeightBipartiteMatching<Service, DefaultWeightedEdge> alg =
                new MaximumWeightBipartiteMatching<>(g, reference, sample);

        // Remove the classes that are matched from the sets of services
        for (DefaultWeightedEdge e : alg.getMatching()) {
            Service refService = g.getEdgeSource(e);
            Service sampleService = g.getEdgeTarget(e);
            Service intersection = refService.intersect(sampleService);
            refService.removeAll(intersection);
            sampleService.removeAll(intersection);
        }

        cleanEmptyServices();

        // Delete items to move
        int moves = 0;
        for (Service refService : reference) {
            Iterator<String> refObjectIterator = refService.iterator();
            while (refObjectIterator.hasNext()) {
                String refObject = refObjectIterator.next();
                caca: for (Service sampleService : sample) {
                    Iterator<String> sampleObjectIterator = sampleService.iterator();
                    while (sampleObjectIterator.hasNext()) {
                        String sampleObject = sampleObjectIterator.next();
                        if (refObject.equals(sampleObject)) {
                            refObjectIterator.remove();
                            sampleObjectIterator.remove();
                            moves++;
                            break caca;
                        }
                    }
                }
            }
        }

        moves += reference.stream().mapToInt(Service::size).sum();
        moves += sample.stream().mapToInt(Service::size).sum();

        return moves;
    }

    /**
     * Utility method to remove empty services from the set of services
     */
    private void cleanEmptyServices() {
        reference = reference.stream().filter(service -> !service.getClasses().isEmpty()).collect(Collectors.toSet());
        sample = sample.stream().filter(service -> !service.getClasses().isEmpty()).collect(Collectors.toSet());
    }

}
