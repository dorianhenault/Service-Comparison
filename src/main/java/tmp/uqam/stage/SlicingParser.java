package tmp.uqam.stage;

import tmp.uqam.stage.structure.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse a txt file containing an architecture
 */
public class SlicingParser {

    private String content;

    /**
     * Initialize the content of the parser with the content of the file
     * @param file the file to parse
     */
    public SlicingParser(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            content = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * parse a set of service from the content of the file provided
     * @return the architecture found from the file
     */
    public Set<Service> parse() {
        Set<Service> services = new HashSet<>();

        Pattern servicePattern = Pattern.compile("[^}{]+(?=})");
        Pattern classPattern = Pattern.compile("[^,\\s][^\\,]*[^,\\s]*");

        Matcher serviceMatcher = servicePattern.matcher(content);
        Matcher classMatcher;

        while (serviceMatcher.find()) {
            Service service = new Service();
            classMatcher = classPattern.matcher(serviceMatcher.group(0));
            while (classMatcher.find()) {
                service.add(classMatcher.group(0));
            }
            services.add(service);
        }
        return services;
    }
}
