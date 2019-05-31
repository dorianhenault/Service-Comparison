package tmp.uqam.stage;

import tmp.uqam.stage.structure.Service;

import java.util.Set;

/**
 * Serialize a Set of service in a string (csv) for a SVG dendogram representation
 */
public class SlicingSerializer {

    /**
     * Serialize the set of services as a csv string
     * @param services the services to serialize
     * @param name the name of the model
     * @return the string that contains the architecture in a csv format
     */
    public String serialize(Set<Service> services, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("id\\n").append(name).append("\\n");
        int id = 1;
        for (Service service : services) {
            sb.append(service.serialize(name, id++));
        }
        return sb.toString();
    }
}
