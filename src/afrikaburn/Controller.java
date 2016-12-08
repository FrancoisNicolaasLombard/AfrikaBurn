package afrikaburn;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author: FN Lombard
 * @Company: VASTech
 *
 * @Description: This class sets controls for the View.fxml file, enabling the
 * desired actions for each action done in the window.
 */
public class Controller implements Initializable {

    // Local Variables
    private Stage window;
    private Scene content;
    private Parent root;
    private boolean onPolygon = false;
    private boolean dragging = false;
    private double dragX = 0;
    private double dragY = 0;
    private MapController mapBuild;
    private Pane map;
    private Booking[] clients;

    // References to FXML components
    @FXML
    BorderPane borderPane;
    @FXML
    HBox labelBox;
    @FXML
    VBox menuBar;
    @FXML
    Region region;
    @FXML
    Label infoLabel;
    @FXML
    VBox clientList;

    /**
     *
     * @param m
     * @Description: Moves the map on mouse click and drag.
     */
    @FXML
    public void mouseDrag(MouseEvent m) {
        if (dragging) {
            mapBuild.dragMap((m.getX() - dragX), (m.getY() - dragY));
            dragX = m.getX();
            dragY = m.getY();
        }
    }

    /**
     *
     * @param m
     * @Description: Gets mouse location on mouse down.
     */
    @FXML
    public void mouseDown(MouseEvent m) {

        dragging = true;
        dragX = m.getX();
        dragY = m.getY();
    }

    /**
     *
     * @param m
     * @Description: Stops drag.
     */
    @FXML
    public void mouseUp(MouseEvent m) {
        dragging = false;
    }

    /**
     *
     * @param m
     * @Description: Scales holder mouse zoom.
     */
    @FXML
    public void mouseZoom(ScrollEvent m) {
        double zoom = m.getDeltaY();
        if (zoom > 0) {
            mapBuild.zoomIn();
        } else {
            mapBuild.zoomOut();
        }
    }

    /**
     *
     * @param location
     * @param resources
     * @Description: This method gets called on the FXML file's create
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //<< LOAD USER DATA FROM CSV FILE>>//
        CSVReader read = new CSVReader();
        clients = read.getClients();

        // Populate the listview with camper names
        for (Booking client : clients) {
            Text tmpClient = new Text(client.getName());
            tmpClient.setFill(Color.WHITE);
            tmpClient.setOnDragDetected(e -> {
                Dragboard db = tmpClient.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(tmpClient.toString());
                db.setContent(content);
                e.consume();
            });
            clientList.getChildren().add(tmpClient);
        }

        //Build the map
        map = new Pane();
        mapBuild = new MapController(infoLabel, map);

        mapListeners();

        // Stops the map from clipping over the other components
        borderPane.setCenter(map);
        map.toBack();

    }

    /**
     * All of the listeners used for the map canvas
     */
    private void mapListeners() {
        map.setOnMouseDragged(e -> {
            mouseDrag(e);
        });
        map.setOnMousePressed(e -> {
            mouseDown(e);
        });
        map.setOnMouseReleased(e -> {
            mouseUp(e);
        });
        map.setOnScroll(e -> {
            mouseZoom(e);
        });
    }

    /**
     *
     * @param window
     * @param root
     * @Description: Initializes the window and fills the components
     */
    public void setWindow(Stage window, Parent root) {
        this.window = window;
        this.root = root;

        content = new Scene(root, 900, 600);

        window.setTitle("Afrika Burn Map");
        window.setScene(content);
        window.show();
    }

    /**
     * FIX - DRAW NEW TMP MAP WITH POLYGONS, WHEN TRANSLATING - INCREASING RES.
     */
    @FXML
    public void exportMap() {
        map.setScaleX(20);
        map.setScaleY(20);
        WritableImage image = map.snapshot(new SnapshotParameters(), null);
        map.setScaleX(1);
        map.setScaleY(1);

        // TODO: probably use a file chooser here
        File file = new File("Map.png");

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            // TODO: handle exception here
        }
    }

    /**
     * @Description: Close command for the exit button.
     */
    @FXML
    public void btnExit() {
        window.close();
    }
}