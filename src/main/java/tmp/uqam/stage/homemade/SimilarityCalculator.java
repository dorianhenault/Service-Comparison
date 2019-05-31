package tmp.uqam.stage.homemade;

import tmp.uqam.stage.structure.Service;
import tmp.uqam.stage.structure.ServicePair;
import tmp.uqam.stage.structure.SimilitudeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compare two services with a greedy approach to try to maximize the jaccard similarity
 */
public class SimilarityCalculator {

    private List<SimilitudeMap> scoresList;
    private List<ServicePair> pairs;

    /**
     * Computes the similitude between two architectures
     * <p>
     * Get the best pairs and then get mean score for the similitude with each pair
     *
     * @param reference the reference architecture
     * @param sample    the architecture to test
     * @return a similitude score
     */
    public double compare(Set<Service> reference, Set<Service> sample) {

        initSimilitudePairs(reference, sample);

        int maxSize = Math.max(reference.size(), sample.size());
        Double score = 0.0;
        for (int i = 0; i < pairs.size(); i++) {
            score += scoresList.get(i).get(pairs.get(i));
        }
        return (score / maxSize) * 100;
    }

    /**
     * Init similitude pairs by reseting them, intializing them and refining them
     *
     * @param reference reference model
     * @param sample    model to test
     */
    public void initSimilitudePairs(Set<Service> reference, Set<Service> sample) {
        scoresList = new ArrayList<>();
        pairs = new ArrayList<>();

        initSimilitudeModel(reference, sample);

        refineDuplicates();
    }

    /**
     * private method to initialize the similitude pair by computing each service of the reference model to a service
     * of the sample model
     * then for now we initialize pairs with the pair with the best similitude
     *
     * @param reference intial model
     * @param sample    model to test
     */
    private void initSimilitudeModel(Set<Service> reference, Set<Service> sample) {
        for (Service referenceService : reference) {
            SimilitudeMap similitudeMap = new SimilitudeMap();
            for (Service sampleService : sample) {
                similitudeMap.put(new ServicePair(referenceService, sampleService), referenceService.similarity(sampleService));
            }
            scoresList.add(similitudeMap);
        }
        for (SimilitudeMap similitudeMap : scoresList) {
            pairs.add(similitudeMap.getBestServicePair(0));
        }
    }

    /**
     * Private method to greedily remove duplicates from the pairs as it would be a fake sign of similitude
     */
    private void refineDuplicates() {
        ServicePair weakPair = duplicatePair();
        int offset = 1;
        while (weakPair != null) {
            Service weakPairOrigin = weakPair.getService1();
            int index = pairs.lastIndexOf(weakPair);
            ServicePair newCandidate = scoresList.get(index).getBestServicePair(offset);
            if (newCandidate == null) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "no Candidate left for " + pairs.get(index));
                pairs.remove(index);
                scoresList.remove(index);
            } else {
                pairs.set(index, newCandidate);
            }
            weakPair = duplicatePair();
            if (weakPair != null && weakPair.getService1().equals(weakPairOrigin)) {
                offset++;
            } else {
                offset = 1;
            }
        }
    }

    /**
     * If a member of a pair is a duplicate of another returns the weakest duplicate to be changed
     *
     * @return the weakest service from a pair where the second member is duplicated or null if there are no diplicates
     */
    private ServicePair duplicatePair() {
        for (int i = 0; i < pairs.size(); i++) {
            for (int j = i + 1; j < pairs.size(); j++) {
                ServicePair firstPair = pairs.get(i);
                ServicePair secondPair = pairs.get(j);
                if (firstPair.getService2().equals(secondPair.getService2())) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Found duplicate dependency : " + firstPair + " and " + secondPair);
                    if (scoresList.get(i).get(firstPair) > scoresList.get(j).get(secondPair)) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Switch : " + secondPair);
                        return secondPair;
                    } else {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Switch : " + firstPair);
                        return firstPair;
                    }
                }
            }
        }
        return null;
    }

    public List<ServicePair> getPairs() {
        return pairs;
    }
}
