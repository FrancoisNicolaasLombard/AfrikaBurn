package afrikaburn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private Pane map, manageMaps;
    private ArrayList<Booking> bookings;
    private Button exportMapPng, exportMapSVG, exportMapJSON, exportMapKMZ, chooseMap, chooseCSV;
    private FileChooser fileChooser;
    private File csvData, jsonData;

    // References to FXML components
    @FXML
    BorderPane borderPane;
    @FXML
    HBox labelBox;
    @FXML
    Label infoLabel;
    @FXML
    VBox menuBar, clientList;
    @FXML
    Region region;
    @FXML
    Button btnMapLayout, btnManageMaps, btnManageBookings, btnExit, btnAddUpdate, btnRemove;
    @FXML
    ScrollPane spClientHolder;
    @FXML
    Pane manageBookings;
    @FXML
    ComboBox cbClients, cbExplicit, cbLoud;
    @FXML
    TextField tfName, tfFront, tfArea;
    @FXML
    ColorPicker cpColour;

    @FXML
    void remove() {
        int bookingNr = cbClients.getSelectionModel().getSelectedIndex() - 1;
        map.getChildren().removeAll(bookings.get(bookingNr).getArea(), bookings.get(bookingNr).getText());
        clientList.getChildren().remove(bookingNr);
        bookings.remove(bookings.get(bookingNr));
        for (int i = bookingNr; i < bookings.size(); i++) {
            bookings.get(i).setId(i);
        }
        mapBuild.updateFile();
        updateCBClients();
        clearFields();
        infoLabel.setText("Client Removed.");
    }

    /**
     * MAKE NEAT
     */
    @FXML
    void addUpdate() {
        if (fieldsValid()) {
            if (cbClients.getSelectionModel().getSelectedIndex() != 0) {
                int bookingNr = cbClients.getSelectionModel().getSelectedIndex() - 1;

                bookings.get(bookingNr).setName(tfName.getText());
                bookings.get(bookingNr).setSize(Double.parseDouble(tfArea.getText()));
                bookings.get(bookingNr).setSexy(cbExplicit.getSelectionModel().getSelectedIndex() == 0);
                bookings.get(bookingNr).setNoisy(cbLoud.getSelectionModel().getSelectedIndex() == 0);
                if (bookings.get(bookingNr) instanceof ShowBooking) {
                    ((ShowBooking) bookings.get(bookingNr)).setFront(Double.parseDouble(tfFront.getText()));
                }
                bookings.get(bookingNr).getArea().setFill(cpColour.getValue());

                ((Text) clientList.getChildren().get(bookingNr)).setFill(Color.WHITE);
                bookings.get(bookingNr).clearShape();
                bookings.get(bookingNr).getArea().getTransforms().removeAll(bookings.get(bookingNr).getArea().getTransforms());
                bookings.get(bookingNr).getText().getTransforms().removeAll(bookings.get(bookingNr).getText().getTransforms());
                map.getChildren().removeAll(bookings.get(bookingNr).getArea(), bookings.get(bookingNr).getText());
                infoLabel.setText("Client Updated.");
            } else {
                ShowBooking newBooking = new ShowBooking(bookings.size(),
                        tfName.getText(),
                        Double.parseDouble(tfFront.getText()),
                        Double.parseDouble(tfArea.getText()),
                        cbExplicit.getSelectionModel().getSelectedIndex() == 0,
                        cbExplicit.getSelectionModel().getSelectedIndex() == 0,
                        "[-1.0; -1.0]", cpColour.getValue().toString());
                bookings.add(newBooking);
                mapBuild.addBooking(newBooking);
                addList(newBooking);
                cbClients.getItems().add((newBooking.getId() + 1) + " " + newBooking.getName());
                infoLabel.setText("Client Added.");
            }
            mapBuild.updateFile();
            clearFields();
        } else {
            infoLabel.setText("Please enter a value in all of the fields.");
        }
    }

    /**
     * Make neat
     */
    @FXML
    void btnManageBookings() {
        if (map.isVisible()) {
            map.setVisible(false);
            manageBookings.setVisible(true);
            borderPane.getChildren().remove(map);
            borderPane.setCenter(manageBookings);
        } else if (manageMaps.isVisible()) {
            manageMaps.setVisible(false);
            manageBookings.setVisible(true);
            borderPane.getChildren().remove(manageMaps);
            borderPane.setCenter(manageBookings);
        }
    }

    @FXML
    void btnManageMaps() {
        if (map.isVisible()) {
            map.setVisible(false);
            manageMaps.setVisible(true);
            borderPane.getChildren().remove(map);
            borderPane.setCenter(manageMaps);
        } else if (manageBookings.isVisible()) {
            manageBookings.setVisible(false);
            manageMaps.setVisible(true);
            borderPane.getChildren().remove(manageBookings);
            borderPane.setCenter(manageMaps);
        }
    }

    /**
     * Make Neat
     */
    @FXML
    void mapLayout() {
        if (manageBookings.isVisible()) {
            manageBookings.setVisible(false);
            map.setVisible(true);
            borderPane.getChildren().remove(manageBookings);
            borderPane.setCenter(map);
            map.toBack();
        } else if (manageMaps.isVisible()) {
            manageMaps.setVisible(false);
            map.setVisible(true);
            borderPane.getChildren().remove(manageMaps);
            borderPane.setCenter(map);
            map.toBack();
        }
    }

    /**
     * MAKE THIS A THREEAD - Loads long and cannot give a warning message
     */
    public void exportMap() {
        infoLabel.setText("Please wait while the map is saved");
        try {
            map.setVisible(true);
            double mapScale = 1;
            mapScale = map.getTransforms().stream()
                    .filter((x) -> (x instanceof Scale))
                    .map((x) -> x.getMxx())
                    .reduce(mapScale, (accumulator, _item) -> accumulator * _item);

            map.getChildren().forEach((component) -> {
                component.getTransforms().removeAll(component.getTransforms());
            });

            map.getTransforms().add(new Scale(1 / mapScale,
                    1 / mapScale,
                    (GV.SCREEN_W / 5 + GV.SCREEN_W * 2 / 5),
                    (GV.SCREEN_H * 0.9) / 2));

            double pixelScale = 3.6;
            double width = (mapBuild.getLayout()[2] - mapBuild.getLayout()[0]) / 2.0 * pixelScale;
            double height = (mapBuild.getLayout()[3] - mapBuild.getLayout()[1]) / 2.0 * pixelScale;

            WritableImage frame = new WritableImage((int) Math.rint(width), (int) Math.rint(height));
            SnapshotParameters spa = new SnapshotParameters();
            spa.setTransform(Transform.scale(pixelScale, pixelScale));

            // Image One
            spa.setViewport(new Rectangle2D(0.0, 0.0, width, height));
            ImageIO.write(SwingFXUtils.fromFXImage(map.snapshot(spa, frame), null), "png", new File("test01.png"));

            // Image Two
            spa.setViewport(new Rectangle2D(0, height, width, height));
            ImageIO.write(SwingFXUtils.fromFXImage(map.snapshot(spa, frame), null), "png", new File("test02.png"));

            // Image Three
            spa.setViewport(new Rectangle2D(width, 0, width, height));
            ImageIO.write(SwingFXUtils.fromFXImage(map.snapshot(spa, frame), null), "png", new File("test03.png"));

            // Image Four
            spa.setViewport(new Rectangle2D(width, height, width, height));
            ImageIO.write(SwingFXUtils.fromFXImage(map.snapshot(spa, frame), null), "png", new File("test04.png"));

            if (manageMaps.isVisible() || manageBookings.isVisible()) {
                map.setVisible(false);
            }

            // Not enough RAM to export as one image
        } catch (IOException e) {
            System.out.println(e.getMessage());
            infoLabel.setText("Map not saved - insufficient heap space.");
        } finally {
            infoLabel.setText("Map saved successfully");
            map.setVisible(true);
        }
    }

    /**
     * @Description: Close command for the exit button.
     */
    @FXML
    public void btnExit() {
        window.close();
    }

    /**
     * Make Neat
     *
     * @param e
     */
    @FXML
    public void clientSelected(Event e) {
        if (cbClients.getSelectionModel().getSelectedIndex() != -1) {
            tfName.disableProperty().set(false);
            tfFront.disableProperty().set(false);
            tfArea.disableProperty().set(false);
            cbLoud.disableProperty().set(false);
            cbExplicit.disableProperty().set(false);
            btnAddUpdate.disableProperty().set(false);
            cpColour.disableProperty().set(false);
            int clientId = cbClients.getSelectionModel().getSelectedIndex();

            if (clientId != 0) {
                btnRemove.disableProperty().set(false);
                clientId--;
                tfName.setText(bookings.get(clientId).getName());
                if (bookings.get(clientId) instanceof ShowBooking) {
                    tfFront.disableProperty().set(false);
                    tfFront.setText("" + ((ShowBooking) bookings.get(clientId)).front());
                } else {
                    tfFront.disableProperty().set(true);
                }
                tfArea.setText("" + bookings.get(clientId).getSize());
                cbExplicit.getSelectionModel().select(bookings.get(clientId).isSexy() ? "TRUE" : "FALSE");
                cbLoud.getSelectionModel().select(bookings.get(clientId).isNoisy() ? "TRUE" : "FALSE");

                Color c = (Color) bookings.get(clientId).getArea().getFill();
                String hex = String.format("#%02X%02X%02X",
                        (int) (c.getRed() * 255),
                        (int) (c.getGreen() * 255),
                        (int) (c.getBlue() * 255));

                cpColour.setValue(Color.valueOf(hex));
                btnAddUpdate.setText("Update");
            } else {
                btnRemove.disableProperty().set(true);
                tfName.clear();
                tfFront.clear();
                tfArea.clear();
                cbExplicit.getSelectionModel().select(-1);
                cbLoud.getSelectionModel().select(-1);
                btnAddUpdate.setText("Add");
            }
        }
    }

    private void addList(Booking booking) {
        Text tmpClient = new Text(booking.getName());
        tmpClient.setStyle("-fx-font-weight: bold");
        if (!booking.isPlaced()) {
            tmpClient.setFill(Color.WHITE);
        } else {
            tmpClient.setFill(Color.web("#6E6E6E"));
        }

        tmpClient.setOnDragDetected(e -> {
            Dragboard db = tmpClient.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cb = new ClipboardContent();
            cb.putString(booking.toString());
            db.setContent(cb);
            e.consume();
        });

        clientList.getChildren().add(tmpClient);

        booking.getArea().setOnDragDetected(e -> {
            Dragboard db = tmpClient.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cb = new ClipboardContent();
            cb.putString(booking.toString());
            db.setContent(cb);
            booking.getArea().setOpacity(0.5);
            e.consume();
        });

        booking.getText().setOnDragDetected(e -> {
            Dragboard db = tmpClient.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cb = new ClipboardContent();
            cb.putString(booking.toString());
            db.setContent(cb);
            booking.getArea().setOpacity(0.5);
            e.consume();
        });
    }

    private void setUpProgram() {
        map = new Pane();
        map.resize(GV.MAP_WIDTH, GV.MAP_HEIGHT);
        bookings = new CSVReader(csvData).getClients();

        mapBuild = new MapController(infoLabel, map, bookings, clientList, jsonData, csvData);
        borderPane.setCenter(null);
        borderPane.setCenter(map);
        map.setVisible(true);
        mapListeners(borderPane.getCenter());
        map.toBack(); // Stops from map clipping over other components

        clientList.getChildren().removeAll(clientList.getChildren());
        bookings.forEach((booking) -> {
            addList(booking);
        });
        populateManageBookings();
    }

    /**
     *
     * @param location
     * @param resources
     * @Description: This method gets called on the FXML file's create
     */
    @Override
    public void initialize(URL location, ResourceBundle resources
    ) {
        // Keep reference of map in this class to add bookings
        manageMaps = new Pane();
        fileChooser = new FileChooser();
        csvData = new File("resources/campers.csv");
        jsonData = new File("resources/afrikaburnmapv2.json");
        menuBar.setMinWidth(GV.SCREEN_W * 1 / 5);
        manageBookings.resize(GV.SCREEN_W * 4 / 5, GV.SCREEN_H * 0.9);
        manageBookings.setVisible(false);
        manageMaps.setStyle(manageBookings.getStyle());
        manageMaps.resize(manageBookings.getWidth(), manageBookings.getHeight());
        cbExplicit.getItems().addAll("TRUE", "FALSE");
        cbLoud.getItems().addAll("TRUE", "FALSE");
        populateManageMaps();
        manageMaps.setVisible(false);

        setUpProgram();

    }

    /**
     *
     */
    private void populateManageMaps() {
        exportMapPng = new Button("Export Map to PNG");
        exportMapSVG = new Button("Export Map to SVG");
        exportMapKMZ = new Button("Export Map to KMZ");
        exportMapJSON = new Button("Export Map to JSON");
        chooseMap = new Button("Choose Map");
        chooseCSV = new Button("Choose CSV");

        exportMapPng.setLayoutX(20);
        exportMapPng.setLayoutY(20);
        exportMapSVG.setLayoutX(20);
        exportMapSVG.setLayoutY(60);
        exportMapKMZ.setLayoutX(20);
        exportMapKMZ.setLayoutY(100);
        exportMapJSON.setLayoutX(20);
        exportMapJSON.setLayoutY(140);
        chooseMap.setLayoutX(20);
        chooseMap.setLayoutY(180);
        chooseCSV.setLayoutX(20);
        chooseCSV.setLayoutY(220);

        exportMapPng.setMinWidth(200);
        exportMapSVG.setMinWidth(200);
        exportMapKMZ.setMinWidth(200);
        exportMapJSON.setMinWidth(200);
        chooseMap.setMinWidth(200);
        chooseCSV.setMinWidth(200);

        exportMapPng.setOnAction(e -> {
            infoLabel.setText("Please wait while the map saves."); //This does not execute
            exportMap();
            e.consume();
        });

        exportMapSVG.setOnAction(e -> {
            SVGWriter svgWriter = new SVGWriter(mapBuild.getMapPolygons(),
                    bookings,
                    mapBuild.getLayout()[2] - mapBuild.getLayout()[0],
                    mapBuild.getLayout()[3] - mapBuild.getLayout()[1]);
        });

        exportMapKMZ.setOnAction(e -> {
            // TODO
        });

        exportMapJSON.setOnAction(e -> {
            mapBuild.portMapFrom();
            JSONWriter jsonWriter = new JSONWriter(mapBuild.getMapPolygons(), bookings);
            mapBuild.portMapTo();
        });

        chooseMap.setOnAction(e -> {
            fileChooser.setTitle("Choose a JSON File to read in the Map");
            mapBuild = null;
            jsonData = fileChooser.showOpenDialog(window);
            setUpProgram();
            manageMaps.setVisible(false);
            infoLabel.setText("New Map has been Loaded.");
        });

        chooseCSV.setOnAction(e -> {
            fileChooser.setTitle("Choose a CSV File to read in the Clients");
            csvData = null;
            mapBuild = null;
            csvData = fileChooser.showOpenDialog(window);
            setUpProgram();
            manageMaps.setVisible(false);
            infoLabel.setText("New Clients have been Loaded.");
        });

        manageMaps.getChildren().addAll(exportMapPng, exportMapSVG, exportMapKMZ, chooseMap, chooseCSV, exportMapJSON);
    }

    /**
     *
     */
    private void populateManageBookings() {
        updateCBClients();
        clearFields();
    }

    private void updateCBClients() {
        cbClients.getItems().removeAll(cbClients.getItems());
        cbClients.getItems().add("- ADD CLIENT -");
        bookings.forEach((client) -> {
            cbClients.getItems().add((client.getId() + 1) + " " + client.getName());
        });
    }

    /**
     * All of the listeners used for the map canvas
     */
    private void mapListeners(Node n) {
        n.setOnMouseDragged(e -> {
            mouseDrag(e);
        });
        n.setOnMousePressed(e -> {
            mouseDown(e);
        });
        n.setOnMouseReleased(e -> {
            mouseUp(e);
        });
        n.setOnScroll(e -> {
            mouseZoom(e);
        });
    }

    /**
     *
     * @param m
     * @Description: Moves the map on mouse click and drag.
     */
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
    public void mouseUp(MouseEvent m) {
        dragging = false;
    }

    /**
     *
     * @param m
     * @Description: Scales holder mouse zoom.
     */
    public void mouseZoom(ScrollEvent m) {
        double zoom = m.getDeltaY();
        if (zoom > 0) {
            mapBuild.zoomIn(m);
        } else {
            mapBuild.zoomOut(m);
        }
    }

    private boolean fieldsValid() {
        return !(tfName.getText().equals("")
                || tfFront.getText().equals("")
                || tfArea.getText().equals("")
                || cbExplicit.getSelectionModel().getSelectedIndex() == -1
                || cbLoud.getSelectionModel().getSelectedIndex() == -1);
    }

    private void clearFields() {
        cbClients.getSelectionModel().select(-1);
        cbExplicit.getSelectionModel().select(-1);
        cbLoud.getSelectionModel().select(-1);
        cpColour.setValue(Color.WHITE);
        tfName.clear();
        tfFront.clear();
        tfArea.clear();
        tfName.disableProperty().set(true);
        tfFront.disableProperty().set(true);
        tfArea.disableProperty().set(true);
        cbLoud.disableProperty().set(true);
        cbExplicit.disableProperty().set(true);
        btnRemove.disableProperty().set(true);
        btnAddUpdate.disableProperty().set(true);
        cpColour.disableProperty().set(true);
    }

    /**
     *
     * @param window
     * @param root
     * @Description: Initializes the window and fills the components
     */
    public void setWindow(Stage window, Parent root) {
        this.window = window;
        window.setMaximized(true);
        content = new Scene(root, window.getWidth(), window.getHeight());
        window.setTitle("Afrika Burn Map");
        window.setScene(content);
        window.show();
    }
}
