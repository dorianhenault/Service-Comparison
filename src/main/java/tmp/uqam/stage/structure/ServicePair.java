package tmp.uqam.stage.structure;

import java.util.Objects;

/**
 * A class to represent a pair of service, useful for the similarity maps
 */
public class ServicePair {

    private Service service1;
    private Service service2;

    public ServicePair(Service service1, Service service2) {
        this.service1 = service1;
        this.service2 = service2;
    }

    public Service getService1() {
        return service1;
    }

    public Service getService2() {
        return service2;
    }

    @Override
    public String toString() {
        return "[" + service1 + "|" + service2 + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServicePair that = (ServicePair) o;
        return Objects.equals(service1, that.service1) &&
                Objects.equals(service2, that.service2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service1, service2);
    }
}
