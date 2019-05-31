package tmp.uqam.stage.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class to represent a map between a pair of service and the similitude between them
 */
public class SimilitudeMap extends HashMap<ServicePair, Double> {

    public SimilitudeMap() {
        super();
    }

    /**
     * Gets the most similar pair of services from the map with an offset
     * @param offset the offset from the best, 0 is the best, 1 the second best pair etc...
     * @return the best pair with the offset or null if the offset is too big
     */
    public ServicePair getBestServicePair(int offset) {
        offset++;
        List<Entry<ServicePair, Double>> list = new ArrayList<>(entrySet());
        list.sort(Entry.comparingByValue());
        if (list.size() - offset < 0) {
            return null;
        } else return list.get(list.size() - offset).getKey();
    }
}
