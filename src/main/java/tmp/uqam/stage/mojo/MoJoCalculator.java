package tmp.uqam.stage.mojo;

import tmp.uqam.stage.structure.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that implements the MojoFM algorithm to compare an sample architecture to a ground truth one,
 * implementation by Zhihua Wen with a little bit of modification
 */
public class MoJoCalculator {

    /* File info */
    private Set<Service> sample = new HashSet<>();
    private Set<Service> reference = new HashSet<>();

    /* The mapping between objects and clusters in B */
    private Map<String, String> mapObjectClusterInB = new Hashtable<>();

    /* The mappings of clusters to tags in both A and B */
    private Map<String, Integer> mapClusterTagA = new Hashtable<>();

    private Map<String, Integer> mapClusterTagB = new Hashtable<>();

    // Stores the number of objects in each cluster in partition B
    // Used in calculating the max distance from partition B
    private Vector<Integer> cardinalitiesInB = new Vector<>();

    /* This vector contains a vector for each cluster in A */
    private Vector<Vector<String>> partitionA = new Vector<>();

    private int l = 0; /* number of clusters in A */

    private int m = 0; /* number of clusters in B */


    private int trimedClasses = 0; /* number of classes in A and not in B and the opposite */

    private int duplicateClasses = 0; /* number of classes that are multiple times in A and in B */

    private long numberOfObjectsInA;

    private Cluster A[] = null;


    /*
     * record the capacity of each group, if the group is empty ,the count is
     * zero, otherwise >= 1
     */
    private int groupscount[] = null;

    /*
     * after join operations, each group will have only one cluster left, we use
     * grouptags[i] to indicate the remain cluster in group i
     */
    private Cluster grouptags[] = null; /*
     * every none empty group have a tag
     * point to a cluster in A
     */

    /**
     * Compute the mojofm metric
     * @param ref ground truth architecture
     * @param sample sample architecture
     * @return the metric computed in a mojofm result
     */
    public MojoFMResult mojofm(Set<Service> ref, Set<Service> sample) {
        this.sample = new HashSet<>();
        this.reference = new HashSet<>();
        mapObjectClusterInB = new Hashtable<>();
        mapClusterTagA = new Hashtable<>();
        cardinalitiesInB = new Vector<>();
        partitionA = new Vector<>();
        l = 0;
        m = 0;
        trimedClasses = 0;
        duplicateClasses = 0;
        A = null;
        groupscount = null;
        grouptags = null;

        sample.forEach(service -> this.reference.add(new Service(service)));
        ref.forEach(service -> this.sample.add(new Service(service)));

        setUp();

        /* tag assigment */
        tagAssignment();

        /* draw graph and matching */
        maxbipartiteMatching();

        /* Calculate MoJoFM value */
        return new MojoFMResult(mojofmValue(cardinalitiesInB, numberOfObjectsInA, calculateCost()), duplicateClasses);
    }

    /**
     * Set up the graph and the services and converts them to the right format
     */
    private void setUp() {

        numberOfObjectsInA = 0;

        /* Read target file first to update mapObjectClusterInB */
        cleanDuplicates();
        convertRef();
        convertSample();

        l = mapClusterTagA.size(); /* number of clusters in A */
        m = mapClusterTagB.size(); /* number of clusters in B */

        A = new Cluster[l]; /* create A */
        groupscount = new int[m]; /* the count of each group, 0 if empty */
        grouptags = new Cluster[m]; /*
         * the first cluster in each group, null if
         * empty
         */

        /* init group tags */
        for (int j = 0; j < m; j++) {
            grouptags[j] = null;
        }

        /* create each cluster in A */
        for (int i = 0; i < l; i++) {
            A[i] = new Cluster(i, l, m);
        }
    }

    /**
     * Maximize the matching between the set of clusters
     */
    private void maxbipartiteMatching() {

        /* Create the graph and add all the edges */
        BipartiteGraph bgraph = new BipartiteGraph(l + m, l, m);

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < A[i].groupList.size(); j++) {
                bgraph.addedge(i, l + A[i].groupList.get(j));
            }
        }

        /* Use maximum bipartite matching to calculate the groups */
        bgraph.matching();

        /*
         * Assign group after matching, for each Ai in matching, assign the
         * corresponding group, for other cluster in A, just leave them alone
         */
        for (int i = l; i < l + m; i++) {
            if (bgraph.vertex[i].matched) {
                int index = bgraph.adjacentList.get(i).get(0);
                A[index].setGroup(i - l);
            }
        }

    }

    /**
     * Calculates the MoJoFM value, using the formula MoJoFM(M) = 1 - mno(A,B)/
     * max(mno(any_A,B)) * 100%
     */
    private double mojofmValue(Vector number_of_B, long obj_number, long totalCost) {
        long maxDis = maxDistanceTo(number_of_B, obj_number);

        maxDis += trimedClasses;
        totalCost += trimedClasses;
        if (maxDis == 0) {
            return 100.0;
        }
        return Math.rint((1 - (double) totalCost / (double) maxDis) * 10000) / 100;
    }

    /* calculate the max(mno(B, any_A)), which is also the max(mno(any_A, B)) */
    private long maxDistanceTo(Vector number_of_B, long obj_number) {
        int group_number = 0;
        int[] B = new int[number_of_B.size()];

        for (int i = 0; i < B.length; i++) {
            B[i] = (Integer) number_of_B.elementAt(i);
        }
        /* sort the array in ascending order */
        Arrays.sort(B);

        for (int aB : B) {
            /* calculate the minimum maximum possible groups for partition B */
            /*
             * after sort the B_i in ascending order B_i: 1, 2, 3, 4, 5, 6, 7,
             * 8, 10, 10, 10, 15 we can calculate g in this way g: 1, 2, 3, 4,
             * 5, 6, 7, 8, 9, 10, 10, 11
             */
            if (group_number < aB) group_number++;
        }
        /* return n - l + l - g = n - g */
        return obj_number - group_number;

    }

    private long calculateCost() {
        int moves = 0; /* total number of move operations */
        int no_of_nonempty_group = 0; /* number of total noneempty groups */
        long totalCost = 0; /* total cost of MoJo */

        /* find none empty groups and find total number of moves */
        for (int i = 0; i < l; i++) {
            /* caculate the count of nonempty groups */
            /*
             * when we found that a group was set to empty but in fact is not
             * empty, we increase the number of noneempty group by 1
             */
            if (groupscount[A[i].getGroup()] == 0) {
                no_of_nonempty_group += 1;
            }
            /* assign group tags */
            /* if this group has no tag, then we assign A[i] to its tag */
            if (grouptags[A[i].getGroup()] == null) {
                grouptags[A[i].getGroup()] = A[i];
            }
            /* assign the group count */
            groupscount[A[i].getGroup()] += 1;
            /* calculate the number of move opts for each cluster */
            moves += A[i].gettotalTags() - A[i].getMaxtag();
        }
        totalCost = moves + l - no_of_nonempty_group;
        return totalCost;
    }

    private void tagAssignment() {
        for (int i = 0; i < l; i++) {
            int tag;
            String clusterName;
            for (int j = 0; j < partitionA.elementAt(i).size(); j++) {
                String objName = partitionA.elementAt(i).elementAt(j);
                clusterName = mapObjectClusterInB.get(objName);
                tag = mapClusterTagB.get(clusterName);
                A[i].addobject(tag, objName);
            }
        }
    }

    /**
     * Convert sample architecture (Sercive set) to a Cluster
     */
    private void convertSample() {
        int i = 0;
        for (Service service : sample) {
            String clusterName = "Service" + ++i;
            for (String objectName : service.getClasses()) {
                int index;
                numberOfObjectsInA++;
                Integer objectIndex = mapClusterTagA.get(clusterName);
                if (objectIndex == null) {
                    index = mapClusterTagA.size();
                    mapClusterTagA.put(clusterName, index);
                    partitionA.addElement(new Vector<>());
                } else {
                    index = objectIndex;
                }
                partitionA.elementAt(index).addElement(objectName);
            }
        }
    }

    /**
     * Convert ref architecture from service set to cluster
     */
    private void convertRef() {
        int i = 0;
        for (Service service : reference) {
            String clusterName = "Service" + ++i;
            cardinalitiesInB.addElement(service.size());
            mapClusterTagB.put(clusterName, i - 1);
            for (String objectName : service.getClasses()) {
                mapObjectClusterInB.put(objectName, clusterName);
            }
        }
    }

    /**
     * removes extra classes by counting them as a move step and add duplicate to the ineffiency part of the result
     */
    private void cleanDuplicates() {
        List<String> servicesInSample = new ArrayList<>();
        List<String> servicesInReference = new ArrayList<>();
        sample.forEach(service -> servicesInReference.addAll(new ArrayList<String>(service.getClasses())));
        reference.forEach(service -> servicesInSample.addAll(new ArrayList<String>(service.getClasses())));
        int sampleSize = servicesInSample.size();
        int refSize = servicesInReference.size();
        int originalSize = sampleSize + refSize;
        int sampleSetSize = new HashSet<>(servicesInSample).size();
        int refSetSize = new HashSet<>(servicesInReference).size();
        duplicateClasses = originalSize - (sampleSetSize + refSetSize);
        List<String> tmp = new ArrayList<>(servicesInReference);
        servicesInReference.removeAll(servicesInSample);
        servicesInSample.removeAll(tmp);
        for (Service service : sample) {
            service.getClasses().removeAll(servicesInReference);
        }
        for (Service service : reference) {
            service.getClasses().removeAll(servicesInSample);
        }
        sample = sample.stream().filter(service -> !service.getClasses().isEmpty()).collect(Collectors.toSet());
        reference = reference.stream().filter(service -> !service.getClasses().isEmpty()).collect(Collectors.toSet());
        trimedClasses = originalSize - (totalClassesInSet(sample) + totalClassesInSet(reference));

    }

    private int totalClassesInSet(Set<Service> serviceSet) {
        return serviceSet.stream().mapToInt(Service::size).sum();
    }
}
