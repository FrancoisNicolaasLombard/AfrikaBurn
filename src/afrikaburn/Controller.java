package afrikaburn;

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
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;

/**
 *
 * @author: FN Lombard
 * @Company: VASTech
 *
 * @Description: This class sets controls for the View.fxml file, enabling all
 * of the commands for the interface. This class is responsible for the logical
 * flow of the program.
 */
public class Controller implements Initializable {

    // Local Variables
    private Stage window;
    private Scene content;
    private boolean dragging = false;
    private double dragX = 0;
    private double dragY = 0;
    private MapController mapController;
    private Pane pane_Map, pane_Manage_Maps;
    private ArrayList<Booking> bookings;
    private Button btnExportMapPng, btnExportMapJSON, btnExportMapKML, btnChooseMap, btnChooseCSV, btnExportMapGPX;
    private Label lblExportMapPng, lblExportMapJSON, lblExportMapKML, lblChooseMap, lblChooseCSV, lblExportMapGPX;
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

    /**
     * Method that removes client from the CSV file.
     */
    @FXML
    void remove() {
        int bookingNr = cbClients.getSelectionModel().getSelectedIndex() - 1;
        pane_Map.getChildren().removeAll(bookings.get(bookingNr).getArea(), bookings.get(bookingNr).getText());
        clientList.getChildren().remove(bookingNr);
        bookings.remove(bookings.get(bookingNr));
        for (int i = bookingNr; i < bookings.size(); i++) {
            bookings.get(i).setId(i);
        }
        mapController.updateFile();
        refreshCBClients();
        clearFields();
        infoLabel.setText("Client Removed.");
    }

    /**
     * Adds/Updates a client, depending which option is selected in the ComboBox
     */
    @FXML
    void addUpdate() {
        if (fieldsValid()) {
            // Update
            if (cbClients.getSelectionModel().getSelectedIndex() != 0) {
                int bookingNr = cbClients.getSelectionModel().getSelectedIndex() - 1;

                bookings.get(bookingNr).setName(tfName.getText());
                bookings.get(bookingNr).setSize(Double.parseDouble(tfArea.getText()));
                bookings.get(bookingNr).setSexy(cbExplicit.getSelectionModel().getSelectedIndex() == 0);
                bookings.get(bookingNr).setNoisy(cbLoud.getSelectionModel().getSelectedIndex() == 0);
                if (bookings.get(bookingNr) instanceof Booking) {
                    bookings.get(bookingNr).setFront(Double.parseDouble(tfFront.getText()));
                }
                bookings.get(bookingNr).getArea().setFill(cpColour.getValue());

                // Removes the polygon from the booking map
                ((Text) clientList.getChildren().get(bookingNr)).setFill(Color.WHITE);
                bookings.get(bookingNr).clearShape();
                bookings.get(bookingNr).getArea().getTransforms().removeAll(bookings.get(bookingNr).getArea().getTransforms());
                bookings.get(bookingNr).getText().getTransforms().removeAll(bookings.get(bookingNr).getText().getTransforms());
                pane_Map.getChildren().removeAll(bookings.get(bookingNr).getArea(), bookings.get(bookingNr).getText());
                infoLabel.setText("Client Updated.");
            } // Add
            else {
                Booking newBooking = new Booking(bookings.size(),
                        tfName.getText(),
                        Double.parseDouble(tfFront.getText()),
                        Double.parseDouble(tfArea.getText()),
                        cbExplicit.getSelectionModel().getSelectedIndex() == 0,
                        cbExplicit.getSelectionModel().getSelectedIndex() == 0,
                        "[-1.0; -1.0]", cpColour.getValue().toString(), "[0.0; 0.0; 0.0; 0.0]");
                bookings.add(newBooking);
                mapController.addBooking(newBooking); //A dd listeners
                addList(newBooking); // Add to client list
                cbClients.getItems().add((newBooking.getId() + 1) + " " + newBooking.getName());
                infoLabel.setText("Client Added.");
            }
            mapController.updateFile();
            clearFields();
        } else {
            infoLabel.setText("Please enter a value in all of the fields.");
        }
    }

    /**
     * Changes scene to manageBookings
     */
    @FXML
    void btnManageBookings() {
        if (pane_Map.isVisible()) {
            pane_Map.setVisible(false);
            manageBookings.setVisible(true);
            borderPane.getChildren().remove(pane_Map);
            borderPane.setCenter(manageBookings);
        } else if (pane_Manage_Maps.isVisible()) {
            pane_Manage_Maps.setVisible(false);
            manageBookings.setVisible(true);
            borderPane.getChildren().remove(pane_Manage_Maps);
            borderPane.setCenter(manageBookings);
        }
    }

    /**
     * Changes scene to manageMaps
     */
    @FXML
    void btnManageMaps() {
        if (pane_Map.isVisible()) {
            pane_Map.setVisible(false);
            pane_Manage_Maps.setVisible(true);
            borderPane.getChildren().remove(pane_Map);
            borderPane.setCenter(pane_Manage_Maps);
        } else if (manageBookings.isVisible()) {
            manageBookings.setVisible(false);
            pane_Manage_Maps.setVisible(true);
            borderPane.getChildren().remove(manageBookings);
            borderPane.setCenter(pane_Manage_Maps);
        }
    }

    /**
     * Changes scene to the map
     */
    @FXML
    void mapLayout() {
        if (manageBookings.isVisible()) {
            manageBookings.setVisible(false);
            pane_Map.setVisible(true);
            borderPane.getChildren().remove(manageBookings);
            borderPane.setCenter(pane_Map);
            pane_Map.toBack();
        } else if (pane_Manage_Maps.isVisible()) {
            pane_Manage_Maps.setVisible(false);
            pane_Map.setVisible(true);
            borderPane.getChildren().remove(pane_Manage_Maps);
            borderPane.setCenter(pane_Map);
            pane_Map.toBack();
        }
    }

    private double getMapScale() {
        double mapScale = 1;
        mapScale = pane_Map.getTransforms().stream()
                .filter((x) -> (x instanceof Scale))
                .map((x) -> x.getMxx())
                .reduce(mapScale, (accumulator, _item) -> accumulator * _item);
        return mapScale;
    }

    /**
     * MAKE THIS A THREEAD - Loads long and cannot give a warning message
     */
    public void exportMap() {
        infoLabel.setText("Please wait while the map is saved");
        try {
            pane_Map.setVisible(true);
            double mapScale = getMapScale();

            pane_Map.getChildren().forEach((component) -> {
                component.getTransforms().removeAll(component.getTransforms());
            });

            pane_Map.getTransforms().add(new Scale(1 / mapScale,
                    1 / mapScale,
                    (GV.SCREEN_W / 5 + GV.SCREEN_W * 2 / 5),
                    (GV.SCREEN_H * 0.9) / 2));

            double pixelScale = 3.6;
            double width = (mapController.getLayout()[2] - mapController.getLayout()[0]) / 2.0 * pixelScale;
            double height = (mapController.getLayout()[3] - mapController.getLayout()[1]) / 2.0 * pixelScale;

            WritableImage frame = new WritableImage((int) Math.rint(width), (int) Math.rint(height));
            SnapshotParameters spa = new SnapshotParameters();
            spa.setTransform(Transform.scale(pixelScale, pixelScale));

            // Image One
            spa.setViewport(new Rectangle2D(0.0, 0.0, width, height));
            ImageIO.write(SwingFXUtils.fromFXImage(pane_Map.snapshot(spa, frame), null), "png", new File("test01.png"));

            // Image Two
            spa.setViewport(new Rectangle2D(0, height, width, height));
            ImageIO.write(SwingFXUtils.fromFXImage(pane_Map.snapshot(spa, frame), null), "png", new File("test02.png"));

            // Image Three
            spa.setViewport(new Rectangle2D(width, 0, width, height));
            ImageIO.write(SwingFXUtils.fromFXImage(pane_Map.snapshot(spa, frame), null), "png", new File("test03.png"));

            // Image Four
            spa.setViewport(new Rectangle2D(width, height, width, height));
            ImageIO.write(SwingFXUtils.fromFXImage(pane_Map.snapshot(spa, frame), null), "png", new File("test04.png"));

            if (pane_Manage_Maps.isVisible() || manageBookings.isVisible()) {
                pane_Map.setVisible(false);
            }

            mapController.resetMap();
            // Not enough RAM to export as one image
        } catch (IOException e) {
            System.out.println(e.getMessage());
            infoLabel.setText("Map not saved - insufficient heap space.");
        } finally {
            infoLabel.setText("Map saved successfully");
            //pane_Map.setVisible(true);
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
     * Gets triggered when the combobox is clicked
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

            // Fetch Client information to display
            if (clientId != 0) {
                btnRemove.disableProperty().set(false);
                clientId--;
                tfName.setText(bookings.get(clientId).getName());
                if (bookings.get(clientId) instanceof Booking) {
                    tfFront.disableProperty().set(false);
                    tfFront.setText("" + ((Booking) bookings.get(clientId)).front());
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
            }// RClear all of the fields
            else {
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

    // Add bookings to the client list
    private void addList(Booking booking) {
        Text tmpClient = new Text((booking.getId() + 1) + " " + booking.getName());
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

        tmpClient.setOnMouseClicked(e -> {
            mapController.dragMap(GV.SCREEN_W * 3 / 5 - (booking.getText().getX() + mapController.getDrag()[0]),
                    GV.SCREEN_H * 0.9 / 2 - (booking.getText().getY() + mapController.getDrag()[1]));
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

    // Gets called everytime a new map or csv file is loaded
    private void setUpProgram() {
        pane_Map = new Pane();
        pane_Map.resize(GV.MAP_WIDTH, GV.MAP_HEIGHT);
        bookings = new CSVReader(csvData).getClients();

        mapController = new MapController(infoLabel, pane_Map, bookings, clientList, jsonData, csvData);
        borderPane.setCenter(null);
        borderPane.setCenter(pane_Map);
        pane_Map.setVisible(true);
        mapListeners(borderPane.getCenter());
        pane_Map.toBack(); // Stops from map clipping over other components

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
        pane_Manage_Maps = new Pane();
        fileChooser = new FileChooser();
        csvData = new File("resources/campers.csv");
        jsonData = new File("resources/afrikaburnmapv2.json");
        menuBar.setMinWidth(GV.SCREEN_W * 1 / 5);
        manageBookings.resize(GV.SCREEN_W * 4 / 5, GV.SCREEN_H * 0.9);
        manageBookings.setVisible(false);
        pane_Manage_Maps.setStyle(manageBookings.getStyle());
        pane_Manage_Maps.resize(manageBookings.getWidth(), manageBookings.getHeight());
        cbExplicit.getItems().addAll("TRUE", "FALSE");
        cbLoud.getItems().addAll("TRUE", "FALSE");
        populateManageMaps();
        pane_Manage_Maps.setVisible(false);
        infoLabel.setText("This is the info label.");

        setUpProgram();

        clientList.setOnDragEntered((DragEvent e) -> {
            if (e.getDragboard().hasString() && bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).isPlaced()) {
                int bookingNr = bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getId();
                ((Text) clientList.getChildren().get(bookingNr)).setFill(Color.WHITE);
                bookings.get(bookingNr).clearShape();
                bookings.get(bookingNr).getArea().getTransforms().removeAll(bookings.get(bookingNr).getArea().getTransforms());
                bookings.get(bookingNr).getText().getTransforms().removeAll(bookings.get(bookingNr).getText().getTransforms());
                pane_Map.getChildren().removeAll(bookings.get(bookingNr).getArea(), bookings.get(bookingNr).getText());
                mapController.updateFile();
                infoLabel.setText("Client Booking Removed.");
            }
        });
    }

    /**
     * Populates the manage maps screen
     */
    private void populateManageMaps() {
        // Initialises the buttons
        btnExportMapPng = new Button("Export Map to PNG");
        btnExportMapKML = new Button("Export Map to KML");
        btnExportMapJSON = new Button("Export Map to JSON");
        btnExportMapGPX = new Button("Export Map to GPX");
        btnChooseMap = new Button("Choose Map");
        btnChooseCSV = new Button("Choose CSV");

        // Assign layout values to buttons
        btnExportMapPng.setLayoutX(20);
        btnExportMapPng.setLayoutY(20);
        btnExportMapKML.setLayoutX(20);
        btnExportMapKML.setLayoutY(60);
        btnExportMapJSON.setLayoutX(20);
        btnExportMapJSON.setLayoutY(100);
        btnExportMapGPX.setLayoutX(20);
        btnExportMapGPX.setLayoutY(140);
        btnChooseMap.setLayoutX(20);
        btnChooseMap.setLayoutY(180);
        btnChooseCSV.setLayoutX(20);
        btnChooseCSV.setLayoutY(220);
        btnExportMapPng.setMinWidth(200);
        btnExportMapKML.setMinWidth(200);
        btnExportMapJSON.setMinWidth(200);
        btnExportMapGPX.setMinWidth(200);
        btnChooseMap.setMinWidth(200);
        btnChooseCSV.setMinWidth(200);

        // Assign listeners to buttons
        btnExportMapPng.setOnAction(e -> {
            infoLabel.setText("Please wait while the map saves."); //This does not execute - run export map in other thread
            exportMap();
            infoLabel.setText("Map has been Exported as Four PNG files.");
            e.consume();
        });
        btnExportMapKML.setOnAction(e -> {
            mapController.portMapFrom();
            KMLWriter kmlWriter = new KMLWriter(mapController.getMapPolygons(), bookings);
            mapController.portMapTo();
            infoLabel.setText("Map has been Exported in KML format.");
            e.consume();
        });
        btnExportMapJSON.setOnAction(e -> {
            mapController.portMapFrom();
            JSONWriter jsonWriter = new JSONWriter(mapController.getMapPolygons(), bookings);
            mapController.portMapTo();
            infoLabel.setText("Map has been Exported in JSON format.");
            e.consume();
        });
        btnExportMapGPX.setOnAction(e -> {
            GPXWriter gpxWriter = new GPXWriter(bookings);
            infoLabel.setText("Map has been Exported in GPX format.");
            e.consume();
        });
        btnChooseMap.setOnAction(e -> {
            fileChooser.setTitle("Choose a JSON File to read in the Map");
            mapController = null;
            jsonData = fileChooser.showOpenDialog(window);
            if (jsonData != null) {
                setUpProgram();
                pane_Manage_Maps.setVisible(false);
                infoLabel.setText("New Map has been Loaded.");
            } else {
                infoLabel.setText("No file selected.");
            }
            e.consume();
        });
        btnChooseCSV.setOnAction(e -> {
            fileChooser.setTitle("Choose a CSV File to read in the Clients");
            csvData = null;
            mapController = null;
            csvData = fileChooser.showOpenDialog(window);
            if (csvData != null) {
                setUpProgram();
                pane_Manage_Maps.setVisible(false);
                infoLabel.setText("New Clients have been Loaded.");
            } else {
                infoLabel.setText("No file selected.");
            }
            e.consume();
        });

        // Initiates labels
        lblExportMapPng = new Label("Exports the map to four 9900x6419 PNG files\n - required 7.5 GB of RAM.");
        lblExportMapKML = new Label("Exports the map to a KML file\n - can load into Google Maps/Earth.");
        lblExportMapJSON = new Label("Exports the map to a JSON file\n - can load into GeoJSON/MapShaper to change format.");
        lblExportMapGPX = new Label("Exports the map to a GPX file that Garmin can Read.");
        lblChooseMap = new Label("Choose a GeoJSON file to load the map.");
        lblChooseCSV = new Label("Choose a CSV file in the format: \nbookingType, "
                + "Name, "
                + "Front(m), "
                + "Area(m\u00B2), "
                + "isNoisy, "
                + "isExplicit, \n"
                + "PolygonPoints [x;y x;y..] ([-1.0;-1.0] for default), "
                + "Colour in hex, "
                + "Two frontage coords.");

        // Assign layout to labels
        lblExportMapPng.setLayoutX(240);
        lblExportMapPng.setLayoutY(20);
        lblExportMapKML.setLayoutX(240);
        lblExportMapKML.setLayoutY(60);
        lblExportMapJSON.setLayoutX(240);
        lblExportMapJSON.setLayoutY(100);
        lblExportMapGPX.setLayoutX(240);
        lblExportMapGPX.setLayoutY(140);
        lblChooseMap.setLayoutX(240);
        lblChooseMap.setLayoutY(180);
        lblChooseCSV.setLayoutX(240);
        lblChooseCSV.setLayoutY(220);

        // Add components to the pane
        pane_Manage_Maps.getChildren().addAll(btnExportMapPng,
                btnExportMapKML,
                btnChooseMap,
                btnChooseCSV,
                btnExportMapJSON,
                lblExportMapPng,
                lblExportMapKML,
                lblExportMapJSON,
                lblChooseMap,
                lblChooseCSV,
                btnExportMapGPX,
                lblExportMapGPX);
    }

    /**
     * Can remove method and place these functions where called
     */
    private void populateManageBookings() {
        refreshCBClients();
        clearFields();
    }

    /**
     * Refreshes the combobox's client list
     */
    private void refreshCBClients() {
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
            mapController.dragMap((m.getX() - dragX), (m.getY() - dragY));
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
            mapController.zoomIn(m);
        } else {
            mapController.zoomOut(m);
        }
    }

    /**
     * Ensures no null fields when adding a client
     *
     * @return
     */
    private boolean fieldsValid() {
        return !(tfName.getText().equals("")
                || tfFront.getText().equals("")
                || tfArea.getText().equals("")
                || cbExplicit.getSelectionModel().getSelectedIndex() == -1
                || cbLoud.getSelectionModel().getSelectedIndex() == -1);
    }

    /**
     * Clear all of the input fields after a client has been added, edited or
     * removed
     */
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
        window.getIcons().add(new Image("Icon.png"));
        window.setTitle("Afrika Burn Map");
        window.setScene(content);
        window.show();
    }
}
