package afrikaburn;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
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
 * @Description:
 */
public class Controller implements Initializable {

    private Stage window;
    private Scene content;
    private Parent root;
    private boolean onPolygon = false;
    private boolean dragging = false;
    private double dragX = 0;
    private double dragY = 0;
    private MapBuilder mapBuild;

    private Pane map;

    @FXML
    BorderPane borderPane;
    @FXML
    AnchorPane bkground;
    @FXML
    HBox labelBox;
    @FXML
    VBox menuBar;
    @FXML
    Region region;
    @FXML
    Label infoLabel;
    @FXML
    Pane holder;

    @FXML
    public void mouseDrag(MouseEvent m) {
        if (dragging) {
            mapBuild.dragMap((m.getX() - dragX) / map.getScaleX(), (m.getY() - dragY) / map.getScaleY());

            dragX = m.getX();
            dragY = m.getY();
        }
    }

    @FXML
    public void mouseDown(MouseEvent m) {
        dragging = true;
        dragX = m.getX();
        dragY = m.getY();
    }

    @FXML
    public void mouseUp(MouseEvent m) {
        dragging = false;
    }

    @FXML
    public void mouseZoom(ScrollEvent m) {
        double zoom = m.getDeltaY();
        if (zoom > 0) {
            map.setScaleX(map.getScaleX() * 1.15);
            map.setScaleY(map.getScaleY() * 1.15);
        } else {
            map.setScaleX(map.getScaleX() / 1.15);
            map.setScaleY(map.getScaleY() / 1.15);
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Load user data
        mapBuild = new MapBuilder();
        map = mapBuild.getGroup();
        
        bkground.toBack();
        holder.getChildren().add(map);

        labelBox.setStyle("-fx-background-color: #383838;");
        menuBar.setStyle("-fx-background-color: #383838;");
    }

    public void setWindow(Stage window, Parent root) {
        this.window = window;
        this.root = root;
        
        content = new Scene(root, 900, 600);
        //bkground.toBack();

        window.setTitle("Afrika Burn Map");
        window.setScene(content);
        window.setResizable(false);
        window.show();
    }

    public void btnExit() {
        window.close();
    }
}
