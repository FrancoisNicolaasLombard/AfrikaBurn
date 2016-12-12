package afrikaburn;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

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
    private boolean dragging = false;
    private double dragX = 0;
    private double dragY = 0;
    private MapController mapBuild;
    private Pane map;
    private Booking[] clients;
    private Polygon[] clientPolygons;
    private Dimension screenSize;

    // References to FXML components
    @FXML
    BorderPane borderPane;

    @FXML
    HBox labelBox;
    @FXML
    Label infoLabel;

    @FXML
    VBox menuBar;
    @FXML
    Region region;
    @FXML
    Button btnMapLayout;
    @FXML
    Button btnManageMaps;
    @FXML
    Button btnManageBookings;
    @FXML
    Button btnExportMap;
    @FXML
    ScrollPane spClientHolder;
    @FXML
    VBox clientList;
    @FXML
    Button btnExit;

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
     * FIX - DRAW NEW TMP MAP WITH POLYGONS, WHEN TRANSLATING - INCREASING RES.
     */
    @FXML
    public void exportMap() {
        // Restores image resolution
        mapBuild.removeTrans();
        map.setScaleX(20);
        map.setScaleY(20);
        map.autosize();
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(map.snapshot(new SnapshotParameters(), null), null), "png", new File("test.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        map.setScaleX((screenSize.getWidth() * 0.75) / GV.MAP_WIDTH);
        map.setScaleY((screenSize.getWidth() * 0.75) / GV.MAP_WIDTH);
    }

    /**
     * @Description: Close command for the exit button.
     */
    @FXML
    public void btnExit() {
        window.close();
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
        //Build the map
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        map = new Pane(); // Keep reference of map in this class to add bookings
        CSVReader reader = new CSVReader();
        clients = reader.getClients();
        clientPolygons = new Polygon[reader.getNrClients()];

        for (Booking client : clients) {
            // Populate the client list 
            Text tmpClient = new Text(client.getName());
            tmpClient.setFill(Color.WHITE);
            tmpClient.setOnDragDetected(e -> {
                Dragboard db = tmpClient.startDragAndDrop(TransferMode.COPY);
                ClipboardContent cb = new ClipboardContent();
                cb.putString(client.toString());
                db.setContent(cb);
                e.consume();
            });
            clientList.getChildren().add(tmpClient);

            client.getArea().setOnDragDetected(e -> {
                Dragboard db = tmpClient.startDragAndDrop(TransferMode.COPY);
                ClipboardContent cb = new ClipboardContent();
                cb.putString(client.toString());
                db.setContent(cb);
                e.consume();
            });
            clientPolygons[client.getId()] = client.getArea();
        }

        mapBuild = new MapController(infoLabel, map, clients, clientPolygons);
        mapListeners();

        // Add map to the GUI
        borderPane.setCenter(map);
        map.toBack();
        map.autosize();
        map.resize(screenSize.getWidth() - 173, screenSize.getHeight());
        map.setScaleX((screenSize.getWidth() * 0.75) / GV.MAP_WIDTH);
        map.setScaleY((screenSize.getWidth() * 0.75) / GV.MAP_WIDTH);
        mapBuild.dragMap((screenSize.getWidth() * 0.75) / 2.5, screenSize.getHeight() / 3.5);
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

        content = new Scene(root, screenSize.getWidth(), screenSize.getHeight());

        window.setTitle("Afrika Burn Map");
        window.setScene(content);
        window.show();
    }
}
