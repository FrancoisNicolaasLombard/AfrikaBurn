package afrikaburn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * @Author FN Lombard
 * @Company: VASTech
 * @Description:
 */
public final class MapController {

    // Class variables
    private final Polygon[] mapPolygons;// High Resolution Polygons
    private final ArrayList<Booking> bookings;// All of the Clients
    private final double[] yMin;    // Smallest Y-Value of map
    private final double[] yMax;    // Largest Y-Value of map
    private final double[] xMin;    // Smallest X-Value of map
    private final double[] xMax;    // Largest X-Value of map
    private final Label infoLabel;
    private final Pane pane_Map;
    private boolean isEditing = false;
    private final VBox clientList;
    private final File csvData;

    /**
     * @Description: Reads the map from the file (name must be
     * afrikaburnmap.json) and loads the coordinates.
     */
    MapController(Label infoLabel, Pane map, ArrayList<Booking> bookings, VBox clientList, File jsonData, File csvData) {
        this.bookings = bookings;
        this.infoLabel = infoLabel;
        this.pane_Map = map;
        this.clientList = clientList;
        this.csvData = csvData;

        yMin = new double[2];
        yMax = new double[2];
        xMin = new double[2];
        xMax = new double[2];

        // Gets Map
        JSONReader reader = new JSONReader(jsonData);
        mapPolygons = reader.polygons();
        minMax();

        //Find the smallest edge of map
        final double DEL_Y = Math.abs(yMax[0] - yMin[0]);
        final double DEL_X = Math.abs(xMax[0] - xMin[0]);
        final double BIGGEST_EDGE = DEL_X < DEL_Y ? DEL_Y : DEL_X;

        // Normalise the mapPolygons
        for (Polygon mapPolygon : mapPolygons) {
            ObservableList<Double> tmpPolygon = mapPolygon.getPoints();
            for (int coords = 0; coords < tmpPolygon.size(); coords += 2) {
                tmpPolygon.set(coords, (tmpPolygon.get(coords) - xMin[0]) / BIGGEST_EDGE * (map.getWidth() > map.getHeight() ? map.getWidth() : map.getHeight()));
                tmpPolygon.set(coords + 1, (tmpPolygon.get(coords + 1) - yMin[0]) / BIGGEST_EDGE * (map.getWidth() > map.getHeight() ? map.getWidth() : map.getHeight()));
            }
        }

        // Get the layout of the normalised map
        yMin[1] = yMin[0] / BIGGEST_EDGE * (map.getWidth() > map.getHeight() ? map.getWidth() : map.getHeight());
        yMax[1] = yMax[0] / BIGGEST_EDGE * (map.getWidth() > map.getHeight() ? map.getWidth() : map.getHeight());
        xMin[1] = xMin[0] / BIGGEST_EDGE * (map.getWidth() > map.getHeight() ? map.getWidth() : map.getHeight());
        xMax[1] = xMax[0] / BIGGEST_EDGE * (map.getWidth() > map.getHeight() ? map.getWidth() : map.getHeight());

        setupMap();
        resetMap();
    }

    /**
     * Resets the map to the starting position - after exported to PNG
     */
    public void resetMap() {
        Scale scale = new Scale();
        scale.setPivotX((GV.SCREEN_W / 5 + GV.SCREEN_W * 2 / 5));
        scale.setPivotY((GV.SCREEN_H * 0.9) / 2);
        scale.setX(1 / GV.ZOOM_AMOUNT / 5);
        scale.setY(1 / GV.ZOOM_AMOUNT / 5);
        pane_Map.getTransforms().add(scale);
        dragMap(-1500, -1200);
    }

    /**
     * @return @Author FN Lombard
     * @Company: VASTech
     * @Description: Adds all of the polygons to the pane
     */
    private void setupMap() {
        for (Polygon current : mapPolygons) {
            current.setStroke(Color.BLACK);
            setMapListeners(current);
            pane_Map.getChildren().add(current);
        }

        bookings.stream().map((current) -> {
            setClientListeners(current.getArea());
            setClientListeners(current.getText());
            return current;
        }).forEachOrdered((current) -> {
            double[] center = getCenter(current.getArea());
            current.isPlaced(false);
            for (Polygon island : mapPolygons) {
                if (island.contains(center[0], center[1])) {
                    pane_Map.getChildren().addAll(current.getArea(), current.getText());
                    current.isPlaced(true);
                }
            }
        });

        // Makes shapes slightly invisible when dragging
        pane_Map.setOnDragExited(e -> {
            isEditing = false;
            bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getArea().setOpacity(1);
            e.consume();
        });

        pane_Map.getTransforms().add(new Rotate(0, pane_Map.getWidth() / 2, pane_Map.getHeight() / 2));
    }

    /**
     * When a client is added, the shapes get listeners assigned to it
     * @param booking 
     */
    public void addBooking(Booking booking) {
        setClientListeners(booking.getArea());
        setClientListeners(booking.getText());
    }

    /**
     * This method neatens up the code and gives the mapPolygons its listeners
     *
     * @param mapCurrent
     */
    private void setMapListeners(Polygon mapCurrent) {
        mapCurrent.setOnMouseClicked(e -> {
            infoLabel.setText("Total Area: " + round(area(mapCurrent) * 100)
                    / 100.0 + " m\u00B2");
        });

        mapCurrent.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String nodeId = db.getString();
                success = true;
                infoLabel.setText("Client Placed.");
                updateFile();
            }
            e.setDropCompleted(success);
            e.consume();
        });

        mapCurrent.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                Polygon booking = bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getArea();
                Text label = bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getText();

                moveShow(mapCurrent,
                        booking,
                        label,
                        Double.parseDouble(e.getDragboard().getString().split(",")[3]),
                        Double.parseDouble(e.getDragboard().getString().split(",")[4]),
                        e.getX(),
                        e.getY());
            }
            e.consume();
        });

        mapCurrent.setOnDragEntered((DragEvent e) -> {
            if (e.getDragboard().hasString()) {
                Polygon booking = bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getArea();
                Text label = bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getText();
                booking.setVisible(true);

                if (!pane_Map.getChildren().contains(booking)) {
                    booking.getTransforms().addAll(mapPolygons[0].getTransforms());
                    label.getTransforms().addAll(mapPolygons[0].getTransforms());
                    pane_Map.getChildren().addAll(booking, label);
                    ((Text) clientList.getChildren().get(bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getId())).setFill(Color.web("#6E6E6E"));
                    bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).isPlaced(true);
                    infoLabel.setText("Client Booking in Progress.");
                }
            }
        });
    }

    /**
     * The method takes the polygons and texts from the clients and assigns listeners to them
     * @param clientCurrent 
     */
    private void setClientListeners(Shape clientCurrent) {
        clientCurrent.setOnMouseClicked(e -> {
            infoLabel.setText("Click and hold to drag this polygon around, drag off to the client list to remove it.");
            e.consume();
        });

        clientCurrent.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String nodeId = db.getString();
                success = true;
                infoLabel.setText("Client Placed.");
                updateFile();
            }
            e.setDropCompleted(success);
            e.consume();
        });

        clientCurrent.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString()) {

                e.acceptTransferModes(TransferMode.MOVE);

                if (e.getDragboard().getString().split(",")[1].equalsIgnoreCase("booking")) {
                    double[] center = getCenter(clientCurrent);
                    for (Polygon plane : mapPolygons) {
                        if (plane.contains(center[0], center[1])) {

                            moveShow(plane,
                                    bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getArea(),
                                    bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])).getText(),
                                    Double.parseDouble(e.getDragboard().getString().split(",")[3]),
                                    Double.parseDouble(e.getDragboard().getString().split(",")[4]),
                                    e.getX(),
                                    e.getY());
                            break;
                        }
                    }
                }
            }
            e.consume();
        });
    }

    /**
     * @Author FN Lombard
     * @Company: VASTech
     * @Description: The method finds the maximum and minimum x- and y-values of
     * the polygons in order to normalize them.
     */
    private void minMax() {
        xMin[0] = mapPolygons[0].getLayoutBounds().getMinX();
        yMin[0] = mapPolygons[0].getLayoutBounds().getMinY();
        xMax[0] = xMin[0];
        yMax[0] = yMin[0];
        for (Polygon current : mapPolygons) {
            if (current.getLayoutBounds().getMinX() < xMin[0]) {
                xMin[0] = current.getLayoutBounds().getMinX();
            } else if (current.getLayoutBounds().getMaxX() > xMax[0]) {
                xMax[0] = current.getLayoutBounds().getMaxX();
            }
            if (current.getLayoutBounds().getMinY() < yMin[0]) {
                yMin[0] = current.getLayoutBounds().getMinY();
            } else if (current.getLayoutBounds().getMaxY() > yMax[0]) {
                yMax[0] = current.getLayoutBounds().getMaxY();
            }
        }
    }

    /**
     * @Author: FN Lombard
     * @Company: VASTech
     * @Description: Project GPS coordinates to Cartesian. Assume working with a
     * small area, the latitude can be approximated as y and the longitude has
     * to stretched by a factor of cos(phi) where phi is the center latitude of
     * the map to prevent warping.
     */
    public void portMapTo() {
        //Find the smallest edge of map
        final double DEL_Y = Math.abs(yMax[0] - yMin[0]);
        final double DEL_X = Math.abs(xMax[0] - xMin[0]);
        final double BIGGEST_EDGE = DEL_X < DEL_Y ? DEL_Y : DEL_X;

        // Normalise the mapPolygons
        for (Polygon mapPolygon : mapPolygons) {
            ObservableList<Double> tmpPolygon = mapPolygon.getPoints();
            for (int coords = 0; coords < tmpPolygon.size(); coords += 2) {
                tmpPolygon.set(coords, (tmpPolygon.get(coords) - xMin[0]) / BIGGEST_EDGE * (pane_Map.getWidth() > pane_Map.getHeight() ? pane_Map.getWidth() : pane_Map.getHeight()));
                tmpPolygon.set(coords + 1, (tmpPolygon.get(coords + 1) - yMin[0]) / BIGGEST_EDGE * (pane_Map.getWidth() > pane_Map.getHeight() ? pane_Map.getWidth() : pane_Map.getHeight()));
            }
        }

        bookings.stream().map((booking) -> booking.getArea().getPoints()).forEachOrdered((tmpPolygon) -> {
            for (int coords = 0; coords < tmpPolygon.size(); coords += 2) {
                tmpPolygon.set(coords, (tmpPolygon.get(coords) - xMin[0]) / BIGGEST_EDGE * pane_Map.getWidth());
                tmpPolygon.set(coords + 1, (tmpPolygon.get(coords + 1) - yMin[0]) / BIGGEST_EDGE * pane_Map.getWidth());
            }
        });

        bookings.forEach((booking) -> {
            booking.getText().setX((booking.getText().getX() - xMin[0]) / BIGGEST_EDGE * pane_Map.getWidth() - booking.getText().getLayoutBounds().getWidth() / 2.0);
            booking.getText().setY((booking.getText().getY() - yMin[0]) / BIGGEST_EDGE * pane_Map.getWidth());
        });
    }

    /**
     * Takes map back to the GPS coordinates
     */
    public void portMapFrom() {
        //Find the smallest edge of map
        final double DEL_Y = Math.abs(yMax[0] - yMin[0]);
        final double DEL_X = Math.abs(xMax[0] - xMin[0]);
        final double BIGGEST_EDGE = DEL_X < DEL_Y ? DEL_Y : DEL_X;

        // Normalise the mapPolygons
        for (Polygon mapPolygon : mapPolygons) {
            ObservableList<Double> tmpPolygon = mapPolygon.getPoints();
            for (int coords = 0; coords < tmpPolygon.size(); coords += 2) {
                tmpPolygon.set(coords, tmpPolygon.get(coords) / pane_Map.getWidth() * BIGGEST_EDGE + xMin[0]);
                tmpPolygon.set(coords + 1, tmpPolygon.get(coords + 1) / pane_Map.getWidth() * BIGGEST_EDGE + yMin[0]);
            }
        }

        // Normalise the mapPolygons
        bookings.stream().map((booking) -> booking.getArea().getPoints()).forEachOrdered((tmpPolygon) -> {
            for (int coords = 0; coords < tmpPolygon.size(); coords += 2) {
                tmpPolygon.set(coords, tmpPolygon.get(coords) / pane_Map.getWidth() * BIGGEST_EDGE + xMin[0]);
                tmpPolygon.set(coords + 1, tmpPolygon.get(coords + 1) / pane_Map.getWidth() * BIGGEST_EDGE + yMin[0]);
            }
        });

        bookings.forEach((booking) -> {
            booking.getText().setX((booking.getText().getX() + booking.getText().getLayoutBounds().getWidth() / 2.0) / pane_Map.getWidth() * BIGGEST_EDGE + xMin[0]);
            booking.getText().setY(booking.getText().getY() / pane_Map.getWidth() * BIGGEST_EDGE + yMin[0]);
        });
    }

    /**
     * Function drags the polygons on the 5100x5100 grid
     * @param deltaX
     * @param deltaY
     */
    public void dragMap(double deltaX, double deltaY) {
        if (!isEditing) {
            pane_Map.getChildren().forEach((component) -> {
                component.getTransforms().add(new Translate(deltaX, deltaY));
            });
        }
    }

    /**
     * This method zooms the canvas - not changing the size of the mapPolygons
     *
     * @param m
     */
    public void zoomIn(ScrollEvent m) {
        double oldScale = 1;
        oldScale = pane_Map.getTransforms().stream()
                .filter((x) -> (x instanceof Scale))
                .map((x) -> x.getMxx())
                .reduce(oldScale, (accumulator, _item) -> accumulator * _item);

        Scale scale = new Scale();
        scale.setPivotX((GV.SCREEN_W / 5 + GV.SCREEN_W * 2 / 5));
        scale.setPivotY((GV.SCREEN_H * 0.9) / 2);
        scale.setX(GV.ZOOM_AMOUNT * oldScale);
        scale.setY(GV.ZOOM_AMOUNT * oldScale);

        for (int i = pane_Map.getTransforms().size() - 1; i >= 0; i--) {
            if (pane_Map.getTransforms().get(i) instanceof Scale) {
                pane_Map.getTransforms().remove(pane_Map.getTransforms().get(i));
            }
        }
        pane_Map.getTransforms().add(scale);
    }

    /**
     * This method zooms the canvas - not changing the size of the mapPolygons
     *
     * @param m
     */
    public void zoomOut(ScrollEvent m) {
        double oldScale = 1;
        oldScale = pane_Map.getTransforms().stream()
                .filter((x) -> (x instanceof Scale))
                .map((x) -> x.getMxx())
                .reduce(oldScale, (accumulator, _item) -> accumulator * _item);

        Scale scale = new Scale();
        scale.setPivotX((GV.SCREEN_W / 5 + GV.SCREEN_W * 2 / 5));
        scale.setPivotY((GV.SCREEN_H * 0.9) / 2);
        scale.setX(oldScale / GV.ZOOM_AMOUNT);
        scale.setY(oldScale / GV.ZOOM_AMOUNT);

        for (int i = pane_Map.getTransforms().size() - 1; i >= 0; i--) {
            if (pane_Map.getTransforms().get(i) instanceof Scale) {
                pane_Map.getTransforms().remove(pane_Map.getTransforms().get(i));
            }
        }
        pane_Map.getTransforms().add(scale);
    }

    /**
     * This method uses a standard formula to determine the area of a irregular
     * polygon.
     *
     * @param bound
     * @return
     */
    public double area(Polygon bound) {
        double area = 0;
        for (int data = 0; data < bound.getPoints().size() - 3; data += 2) {
            area -= (bound.getPoints().get(data) * bound.getPoints()
                    .get(data + 3));
            area += (bound.getPoints().get(data + 1) * bound.getPoints()
                    .get(data + 2));
        }
        area /= 2;
        area *= GV.METER_SQUARED_2_MAP_RATIO;
        return abs(area);
    }

    private double length(double x1, double y1, double x2, double y2) {
        return sqrt(pow(x1 - x2, 2) + pow(y1 - y2, 2));
    }

    private double gradient(double x1, double y1, double x2, double y2) {
        double dy = y2 - y1;
        double dx = x2 - x1;
        return round(dy / dx * 100) / 100.0;
    }

    private void moveShow(Polygon plane, Polygon booking, Text label, double faceLength, double area, double mouseX, double mouseY) {

        // Declare two points and use the first two as default
        double[] line_closest = new double[4];
        double x1, y1, x2, y2;

        // Connect the last point to the first one
        x1 = plane.getPoints().get(plane.getPoints().size() - 2);
        y1 = plane.getPoints().get(plane.getPoints().size() - 1);
        x2 = plane.getPoints().get(0);
        y2 = plane.getPoints().get(1);

        line_closest[0] = x1;
        line_closest[1] = y1;
        line_closest[2] = x2;
        line_closest[3] = y2;

        // take the center of the line to the mousepointer
        double temp_distance = length((x1 + x2) / 2.0, (y1 + y2) / 2.0, mouseX, mouseY);
        double shortest = temp_distance;

        // Find the line closest to the mouse pointer
        // Start at 2 because the first coordinates are already the default
        for (int x = 0; x < plane.getPoints().size(); x += 2) {
            // Length between two points of polygon
            double line_length = length(x1, y1, x2, y2);
            double gradient = gradient(x1, y1, x2, y2);

            for (int i = 0; i < line_length; i++) {
                temp_distance = length(x1 + signum(x2 - x1) * i * abs(cos(atan(gradient))), y1 + signum(y2 - y1) * i * abs(sin(atan(gradient))), mouseX, mouseY);

                if (temp_distance < shortest) {
                    line_closest[0] = x1;
                    line_closest[1] = y1;
                    line_closest[2] = x2;
                    line_closest[3] = y2;

                    shortest = temp_distance;
                }
            }

            // Have to include the last line as well, since the last point is connected to the first one
            if (x == plane.getPoints().size() - 2) {
                break;
            } else {
                x1 = plane.getPoints().get(x);
                y1 = plane.getPoints().get(x + 1);
                x2 = plane.getPoints().get(x + 2);
                y2 = plane.getPoints().get(x + 3);
            }
        }

        // Find the closest point to the mouse pointer
        double gradient = gradient(line_closest[0], line_closest[1], line_closest[2], line_closest[3]);
        double gradient_ort = -1 / gradient;
        double offset_1 = (line_closest[1] + line_closest[3]) / 2.0 - gradient * (line_closest[0] + line_closest[2]) / 2.0;
        double offset_ort_1 = mouseY - gradient_ort * mouseX;
        double xi;
        double yi;

        if (gradient > 1e10) {
            xi = (line_closest[0] + line_closest[2]) / 2.0;
            yi = mouseY;
        } else if (gradient == 0) {
            xi = mouseX;
            yi = (line_closest[1] + line_closest[3]) / 2.0;
        } else {
            xi = (offset_ort_1 - offset_1) / (gradient - gradient_ort);
            yi = gradient * xi + offset_1;
        }

        // The closest to the mouse pointer is the origin
        double prev_x = xi;
        double prev_y = yi; // Next point on the polygon.
        double next_x;
        double next_y;

        ObservableList<Double> oldPoints = booking.getPoints();
        Polygon test = new Polygon();

        test.getPoints().addAll(xi, yi);

        // Draw the block
        if (!plane.contains(xi + area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort)), yi + area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort)))) {
            // First Point
            next_x = prev_x + faceLength / 2.0 / GV.METER_2_MAP_RATIO * cos(atan(gradient));
            next_y = prev_y + faceLength / 2.0 / GV.METER_2_MAP_RATIO * sin(atan(gradient));
            test.getPoints().addAll(next_x, next_y);

            // Second Point
            next_x -= area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort));
            next_y -= area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort));
            test.getPoints().addAll(next_x, next_y);

            // Third Point
            next_x -= faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient));
            next_y -= faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient));
            test.getPoints().addAll(next_x, next_y);

            // Fourth Point
            next_x += area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort));
            next_y += area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort));
            test.getPoints().addAll(next_x, next_y, xi, yi);
        } else {
            // First Point
            next_x = prev_x + faceLength / 2.0 / GV.METER_2_MAP_RATIO * cos(atan(gradient));
            next_y = prev_y + faceLength / 2.0 / GV.METER_2_MAP_RATIO * sin(atan(gradient));
            test.getPoints().addAll(next_x, next_y);

            // Second Point
            next_x += area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort));
            next_y += area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort));
            test.getPoints().addAll(next_x, next_y);

            // Third Point
            next_x -= faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient));
            next_y -= faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient));
            test.getPoints().addAll(next_x, next_y);

            // Fourth Point
            next_x -= area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort));
            next_y -= area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort));
            test.getPoints().addAll(next_x, next_y, xi, yi);
        }

        double[] center = getCenter(test);

        if (plane.contains(center[0], center[1])) {
            booking.getPoints().removeAll(oldPoints);
            booking.getPoints().addAll(test.getPoints());
            label.setWrappingWidth(booking.getLayoutBounds().getWidth() / 2.0);
            label.setX((booking.getLayoutBounds().getMaxX() + booking.getLayoutBounds().getMinX()) / 2.0 - label.getLayoutBounds().getWidth() / 2.0);
            label.setY((booking.getLayoutBounds().getMaxY() + booking.getLayoutBounds().getMinY()) / 2.0);
        }
    }

    /**
     *
     * @param shape
     * @return
     */
    public double[] getCenter(Shape shape) {
        return new double[]{(shape.getLayoutBounds().getMaxX() + shape.getLayoutBounds().getMinX()) / 2.0, (shape.getLayoutBounds().getMaxY() + shape.getLayoutBounds().getMinY()) / 2.0};
    }

    /**
     * This method saves the current state of the program to the csv file
     */
    public void updateFile() {
        BufferedWriter writer = null;
        try {
            File output = new File("resources/" + csvData.getName());
            writer = new BufferedWriter(new FileWriter(output));

            for (Booking booking : bookings) {
                String points = booking.getArea().getPoints().toString().replaceAll(",", ";");
                String colour = booking.getArea().getFill().toString();
                String[] tmp = booking.toString().split(",");
                if (tmp[1].equalsIgnoreCase("booking")) {
                    writer.write(tmp[1] + "," + tmp[2] + "," + tmp[3] + "," + tmp[4] + "," + tmp[5] + "," + tmp[6] + "," + points + "," + colour + "\n");
                } else {
                    writer.write(tmp[1] + "," + tmp[2] + "," + tmp[3] + "," + tmp[4] + "," + tmp[5] + "," + points + "," + colour + "\n");
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                // Close the writer regardless of what happens
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(Controller.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public double[] getLayout() {
        return new double[]{xMin[1], yMin[1], xMax[1], yMax[1]};
    }

    public Polygon[] getMapPolygons() {
        return mapPolygons;
    }
}
