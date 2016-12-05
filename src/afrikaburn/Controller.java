package afrikaburn;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
    private MapBuilder mapBuild;
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
            map.setScaleX(map.getScaleX() * GV.ZOOM_AMOUNT);
            map.setScaleY(map.getScaleY() * GV.ZOOM_AMOUNT);
        } else {
            map.setScaleX(map.getScaleX() / GV.ZOOM_AMOUNT);
            map.setScaleY(map.getScaleY() / GV.ZOOM_AMOUNT);
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
        mapBuild = new MapBuilder(infoLabel);
        map = mapBuild.getGroup();

        mapListeners();

        // Stops the map from clipping over the other components
        borderPane.setCenter(map);
        map.toBack();
    }

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
        window.setResizable(false);
        window.show();
    }

    /**
     * @Description: Close command for the exit button.
     */
    public void btnExit() {
        window.close();
    }
}
