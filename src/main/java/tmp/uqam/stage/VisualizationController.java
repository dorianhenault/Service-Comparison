package tmp.uqam.stage;

import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import tmp.uqam.stage.structure.ComparisonResult;
import tmp.uqam.stage.structure.Service;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to control the visualization
 */
public class VisualizationController extends Region {

    public WebView browser;
    public Button buttonReference;
    public Button buttonModel1;
    public Button buttonModel2;
    public Button buttonModel3;
    public Button buttonModel4;

    public Text info;
    public Label similarity;
    public Label mojoFM;
    public Label a2a;
    public Label turboMQ;

    private WebEngine webEngine;
    private Map<String, ComparisonResult> results;
    private Set<Service> reference;
    private SlicingSerializer serializer;
    private String currentModelName;

    /**
     * Initialize all the visual components of the visualizer as well as the data structures
     * Also prints the dendrogram of the reference service
     *
     * @param serviceModels        all the set of services found
     * @param modelNames           the names of these models
     * @param preCalculatedResults the results of all the comparisons foreach model
     */
    public void initialize(List<Set<Service>> serviceModels, List<String> modelNames, Map<String, ComparisonResult> preCalculatedResults) {
        serializer = new SlicingSerializer();
        webEngine = browser.getEngine();
        webEngine.load((getClass().getClassLoader().getResource("FXML/index.html")).toString());
        browser.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent e) -> {
            if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.EQUALS || e.getCode() == KeyCode.PLUS) {
                browser.setZoom(browser.getZoom() * 1.1);
            } else if (e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS) {
                browser.setZoom(browser.getZoom() / 1.1);
            }
        });
        Map<Button, Set<Service>> buttonModels = new HashMap<>();
        List<Button> buttonContainers = Arrays.asList(buttonReference, buttonModel1, buttonModel2, buttonModel3, buttonModel4);
        results = preCalculatedResults;
        reference = serviceModels.get(0);
        currentModelName = modelNames.get(0);
        for (int i = 0; i < serviceModels.size(); i++) {
            Button currentButton = buttonContainers.get(i);
            buttonModels.put(currentButton, serviceModels.get(i));
            int finalI = i;
            currentButton.setOnMouseClicked(e -> changeComparison(serviceModels.get(finalI), modelNames.get(finalI)));
            currentButton.setText(modelNames.get(i));
        }
        for (int i = serviceModels.size(); i < buttonContainers.size(); i++) {
            ((StackPane) buttonContainers.get(i).getParent()).getChildren().remove(buttonContainers.get(i));
        }
        initializeWebView(serializer.serialize(buttonModels.get(buttonReference), modelNames.get(0)), modelNames.get(0));
    }

    /**
     * Start the scriptworker and prints the reference model dendrogram
     *
     * @param csvModel      the csv string to create the dendrogram
     * @param referenceName the name of the reference
     */
    private void initializeWebView(String csvModel, String referenceName) {
        String script = "createDendogram(\"" + csvModel + "\");";
        initializeReferenceView(results.get(referenceName));
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                webEngine.executeScript(script);
            }
        });
    }

    /**
     * Change the dendrogram that is printed on the webview
     *
     * @param model     the set of services to serialize
     * @param modelName the name of the model to print
     */
    private void changeComparison(Set<Service> model, String modelName) {
        currentModelName = modelName;
        ComparisonResult result = results.get(modelName);
        printDendogram(serializer.serialize(model, modelName));
        info.setText(modelName);
        if (model.equals(reference)) {
            initializeReferenceView(result);
        } else {
            similarity.setText(String.format("%.1f", result.getSimilarity()) + "%\n");
            mojoFM.setText(String.format("%.1f", result.getMojoFM().getResult()) + "%" + (
                    (result.getMojoFM().getInefficiency() == 0) ? "\n" : (" (" + result.getMojoFM().getInefficiency() + ")\n"))
            );
            if (result.getMojoFM().getInefficiency() == 0) {
                mojoFM.setTextFill(Color.BLACK);
            } else {
                mojoFM.setTextFill(Color.RED);
            }
            a2a.setText(String.format("%.1f", result.getA2a()) + "%\n");
            turboMQ.setText(String.format("%.3f", result.getTurboMQ()));
        }
    }

    private void initializeReferenceView(ComparisonResult result) {
        similarity.setText("ref");
        mojoFM.setText("ref");
        mojoFM.setTextFill(Color.BLACK);
        a2a.setText("ref");
        turboMQ.setText(String.format("%.3f", result.getTurboMQ()));
    }

    /**
     * Displays the dendrogram
     *
     * @param rawCSV csv string to build the dendrogram
     */
    public void printDendogram(String rawCSV) {
        String script = "createDendogram(\"" + rawCSV + "\");";
        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript(script);
        }
    }

    /**
     * Ensure that the dendrogram is centered
     */
    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    /**
     * save the current dendrogram as an svg file in the root of the project
     */
    public void saveSVG() {
        String script = "saveSVG();";
        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            String result = (String) webEngine.executeScript(script);
            if (result != null) {
                try {
                    PrintWriter writer = new PrintWriter(currentModelName + ".svg", "UTF-8");
                    writer.println(result);
                    writer.close();
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Saved " + currentModelName + ".svg in root directory");
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage());
                }
            }
        }
    }
}
