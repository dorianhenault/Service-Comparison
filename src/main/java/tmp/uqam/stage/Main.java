package tmp.uqam.stage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tmp.uqam.stage.a2a.A2ACalculator;
import tmp.uqam.stage.homemade.SimilarityCalculator;
import tmp.uqam.stage.mojo.MoJoCalculator;
import tmp.uqam.stage.structure.ComparisonResult;
import tmp.uqam.stage.structure.Service;
import tmp.uqam.stage.turbomq.NullTurboMQCalculator;
import tmp.uqam.stage.turbomq.TurboMQCalculator;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Main flow of the program
 */
public class Main extends Application {

    /**
     * Start the visualization, compare the services and save the results
     */
    @Override
    public void start(Stage stage) {

        List<String> parameters = getParameters().getUnnamed();
        if (parameters.size() != 2 && parameters.size() != 4) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Wrong number of parameters, Usage :\n" +
                    "{Path to a directory with the model files to check (txt files)} \n" +
                    "{the name of the reference model in the directory without the path}" +
                    " (There can be up to 5 models loaded)\n" +
                    "{Path to a kdm_xmi file for the turboMQ metric} (optional)\n" +
                    "{Name of the kdm model} (should be the name of the project (needed if there is the third arg)");
            System.exit(1);
        }

        File directory = new File(parameters.get(0));
        File[] files = directory.listFiles();
        if (files == null) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "The first argument was not a directory");
            System.exit(1);
        }

        List<Set<Service>> serviceModels = new ArrayList<>();
        List<String> modelNames = new ArrayList<>();
        Map<String, ComparisonResult> preCalculatedResults = new HashMap<>();

        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (fileName.equals(parameters.get(1))) {
                    serviceModels.add(0, new SlicingParser(file).parse());
                    modelNames.add(0, fileName.split("\\.")[0]);
                } else {
                    int i = fileName.lastIndexOf('.');
                    if (i > 0 && fileName.substring(i + 1).equals("txt")) {
                        serviceModels.add(new SlicingParser(file).parse());
                        modelNames.add(fileName.split("\\.")[0]);
                    }
                }
            }
        }

        // compare the different service architectures

        SimilarityCalculator similarityCalculator = new SimilarityCalculator();
        MoJoCalculator moJoCalculator = new MoJoCalculator();
        A2ACalculator a2aCalculator = new A2ACalculator();
        TurboMQCalculator turboMQCalculator;
        if (parameters.size() == 4) {
            turboMQCalculator = new TurboMQCalculator(parameters.get(2), parameters.get(3));
        } else {
            turboMQCalculator = new NullTurboMQCalculator();
        }

        preCalculatedResults.put(modelNames.get(0), new ComparisonResult(null, null, null, turboMQCalculator.calculateTurboMQ(serviceModels.get(0))));

        for (int i = 1; i < serviceModels.size(); i++) {
            preCalculatedResults.put(modelNames.get(i), new ComparisonResult(
                    similarityCalculator.compare(serviceModels.get(0), serviceModels.get(i)),
                    moJoCalculator.mojofm(serviceModels.get(0), serviceModels.get(i)),
                    a2aCalculator.a2aResult(serviceModels.get(0), serviceModels.get(i)),
                    turboMQCalculator.calculateTurboMQ(serviceModels.get(i))
            ));
        }
        // init visualization
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("FXML/main.fxml"));
            Parent root = loader.load();
            VisualizationController child = loader.getController();
            child.initialize(serviceModels, modelNames, preCalculatedResults);
            Scene scene = new Scene(root, 1200, 900);
            stage.setTitle("Service Slicing Comparison");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
        }

        String rootPath = System.getProperty("user.home");
        File dir = new File(rootPath + File.separator + "bench");
        if (!dir.exists())
            dir.mkdirs();
        try {
            String name;
            if (parameters.size() == 4) {
                name = parameters.get(3) + "-results";
            } else {
                name = "bench-results";
            }
            PrintWriter writer = new PrintWriter(System.getProperty("user.home") + File.separator + "bench" + File.separator + name, "UTF-8");
            for (Map.Entry<String, ComparisonResult> result : preCalculatedResults.entrySet()) {
                System.out.println(result.getKey() + " : " + result.getValue());
                writer.print(result.getKey() + " : " + result.getValue() + '\n');
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
