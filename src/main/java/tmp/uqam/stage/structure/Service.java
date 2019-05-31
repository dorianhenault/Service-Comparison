package tmp.uqam.stage.structure;

import tmp.uqam.stage.kdmparser.ClassPair;

import java.util.*;

/**
 * Class that represents a service a set of String (class names)
 */
public class Service implements Iterable<String> {

    private Set<String> classes;
    private int group;

    public Service() {
        this.classes = new HashSet<>();
        this.group = 0;
    }

    public Service(Service service) {
        this.classes = new HashSet<>(service.classes);
        this.group = service.group;
    }

    public Service(Set<String> classes) {
        this.classes = classes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(classes, service.classes) && Objects.equals(group, service.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classes);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        classes.forEach(sj::add);
        return sj.toString();
    }

    public void add(String classEntityName) {
        classes.add(classEntityName);
    }

    /**
     * Computes the Jaccard similarity between two services
     */
    public double similarity(Service s2) {
        Set<String> classes1 = new HashSet<>(classes);
        Set<String> classes2 = new HashSet<>(s2.classes);
        final int sa = classes1.size();
        final int sb = classes2.size();
        classes1.retainAll(classes2);
        final int intersection = classes1.size();
        return 1d / (sa + sb - intersection) * intersection;
    }

    /**
     * Serialize the service in a csv format for the dendrogram visualization
     *
     * @param name the name of the model
     * @param id   the id of the service
     */
    public String serialize(String name, int id) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(".Service").append(id).append("\\n");
        for (String classEntity : classes) {
            sb.append(name).append(".Service").append(id).append('.').append(classEntity).append("\\n");
        }
        return sb.toString();
    }

    public Integer size() {
        return classes.size();
    }

    public Set<String> getClasses() {
        return classes;
    }

    /**
     * Intersection between two services (number of classes that are in the two)
     * @param otherService the service to compare to
     * @return the number of classes that are in both
     */
    public Service intersect(Service otherService) {
        Set<String> intersection = new HashSet<String>(classes); // use the copy constructor
        intersection.retainAll(otherService.classes);
        return new Service(intersection);
    }

    /**
     * Set a group value to prevent equality mishaps
     * @param value an int value to mark a service with
     */
    public void setGroup(int value) {
        this.group = value;
    }

    public void removeAll(Service toRemove) {
        this.classes.removeAll(toRemove.classes);
    }

    /**
     * Checks if a service contains two classes provided in the pair
     * @param classPair a pair of classes
     * @return true if they are in the service or false otherwise
     */
    public boolean containsPair(ClassPair classPair) {
        return (classes.contains(classPair.getClass1()) && classes.contains(classPair.getClass2()));
    }

    public boolean contains(String object) {
        return (classes.contains(object));
    }

    @Override
    public Iterator<String> iterator() {
        return classes.iterator();
    }
}
