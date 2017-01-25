package afrikaburn;

import java.awt.geom.Line2D;
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
import java.util.Arrays;
import java.util.Objects;
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
import javafx.scene.shape.Circle;
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
    private double delX, delY;
    private final Label infoLabel;
    private final Pane pane_Map;
    private boolean isEditing = false;
    private final VBox clientList;
    private final File csvData;
    private final Circle dot;

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
        delX = 0;
        delY = 0;
        dot = new Circle();

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
        pane_Map.getChildren().add(dot);
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
     *
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
                updateFile();
            }
            e.setDropCompleted(success);
            e.consume();
        });

        mapCurrent.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                Booking booking = bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0]));
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
     * The method takes the polygons and texts from the clients and assigns
     * listeners to them
     *
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
                                    bookings.get(Integer.parseInt(e.getDragboard().getString().split(",")[0])),
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
     *
     * @param deltaX
     * @param deltaY
     */
    public void dragMap(double deltaX, double deltaY) {
        if (!isEditing) {
            delX += deltaX;
            delY += deltaY;
            pane_Map.getChildren().forEach((current) -> {
                current.getTransforms().clear();
                current.getTransforms().add(new Translate(delX, delY));
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
        // Connect the first point to the last point
        // The formula used can be found at: http://www.mathopenref.com/coordpolygonarea.html
        area += bound.getPoints().get(bound.getPoints().size() - 2) * bound.getPoints().get(1);
        area -= bound.getPoints().get(bound.getPoints().size() - 1) * bound.getPoints().get(0);
        for (int point = 0; point < bound.getPoints().size() - 3; point += 2) {
            area += (bound.getPoints().get(point) * bound.getPoints()
                    .get(point + 3));
            area -= (bound.getPoints().get(point + 1) * bound.getPoints()
                    .get(point + 2));
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

    /**
     * This method finds the point on the polygon closest to the mouse and draws
     * a polygon inside the square
     *
     * @param plane
     * @param booking
     * @param label
     * @param faceLength
     * @param area
     * @param mouseX
     * @param mouseY
     */
    private void moveShow(Polygon plane, Booking clientBooking, Text label, double faceLength, double area, double mouseX, double mouseY) {
        // Declare two points and use the first two as default
        Polygon booking = clientBooking.getArea();
        double[] line_closest = new double[4];
        double x1, y1, x2, y2;
        int polygonIndexClosest = 0, polygonIndexClosestSave = 0;

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

            // Check each unit length of the line - increases the resolution
            for (int i = 0; i < line_length; i++) {
                temp_distance = length(x1 + signum(x2 - x1) * i * abs(cos(atan(gradient))),
                        y1 + signum(y2 - y1) * i * abs(sin(atan(gradient))),
                        mouseX,
                        mouseY);

                if (temp_distance < shortest) {
                    line_closest[0] = x1;
                    line_closest[1] = y1;
                    line_closest[2] = x2;
                    line_closest[3] = y2;
                    polygonIndexClosest = x == 0 ? plane.getPoints().size() - 2 : x - 2; // x was increased by 2
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
        polygonIndexClosestSave = polygonIndexClosest;

        // Find the closest point to the mouse pointer
        double gradient = gradient(line_closest[0], line_closest[1], line_closest[2], line_closest[3]);
        double gradient_ort = -1 / gradient;
        double offset_1 = (line_closest[1] + line_closest[3]) / 2.0 - gradient * (line_closest[0] + line_closest[2]) / 2.0;
        double offset_ort_1 = mouseY - gradient_ort * mouseX;
        double xi, yi;

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
        double prev_y = yi;
        double next_x;
        double next_y;

        clientBooking.setHeight(1);
        int[] indexHeight = new int[2];
        double[] gradientHeight = new double[2];
        int index_direction, sign; // Counts the total legs facing downwards, if two, then the booking must flip
        Polygon test = new Polygon();
        test.getPoints().addAll(xi, yi); // Add the point closest to the mouse

        double extrudedLength = 0, maxLength; // Total length the front has been drawn
        boolean placed = true;

        gradient = gradient(prev_x,
                prev_y,
                plane.getPoints().get(polygonIndexClosest),
                plane.getPoints().get(polygonIndexClosest + 1));

        // First Point
        next_x = prev_x + faceLength / 2.0 / GV.METER_2_MAP_RATIO * cos(atan(gradient));
        next_y = prev_y + faceLength / 2.0 / GV.METER_2_MAP_RATIO * sin(atan(gradient));

        double length_to_point_one, length_to_point_two;

        if (polygonIndexClosest == plane.getPoints().size() - 2) {
            length_to_point_one = length(next_x,
                    next_y,
                    plane.getPoints().get(polygonIndexClosest),
                    plane.getPoints().get(polygonIndexClosest + 1));
            length_to_point_two = length(next_x,
                    next_y,
                    plane.getPoints().get(0),
                    plane.getPoints().get(1));
        } else {
            length_to_point_one = length(next_x,
                    next_y,
                    plane.getPoints().get(polygonIndexClosest),
                    plane.getPoints().get(polygonIndexClosest + 1));
            length_to_point_two = length(next_x,
                    next_y,
                    plane.getPoints().get(polygonIndexClosest + 2),
                    plane.getPoints().get(polygonIndexClosest + 3));
        }

        if (length_to_point_one < length_to_point_two) {
            index_direction = 1;
        } else {
            index_direction = -1;
            polygonIndexClosest += 2;
            sign = (int) signum(plane.getPoints().get(polygonIndexClosest) - prev_x);
            sign = sign == 0 ? 1 : sign;
            next_x = prev_x + sign * faceLength / 2.0 / GV.METER_2_MAP_RATIO * (cos(atan(gradient)));
            next_y = prev_y + sign * faceLength / 2.0 / GV.METER_2_MAP_RATIO * (sin(atan(gradient)));
        }

        maxLength = length(xi, yi, plane.getPoints().get(polygonIndexClosest),
                plane.getPoints().get(polygonIndexClosest + 1));

        if (faceLength / 2.0 / GV.METER_2_MAP_RATIO < maxLength) {
            test.getPoints().addAll(next_x, next_y);
        } else {
            do {
                // Add the next point from the plane's polygon - follows it
                prev_x = plane.getPoints().get(polygonIndexClosest);
                prev_y = plane.getPoints().get(polygonIndexClosest + 1);
                test.getPoints().addAll(prev_x, prev_y);

                // Increase the extruded length
                extrudedLength += maxLength;

                // Roll over for when the end is reached
                if (index_direction > 0) {
                    polygonIndexClosest = (polygonIndexClosest == 0) ? plane.getPoints().size() - 2 : polygonIndexClosest - 2;
                } else {
                    polygonIndexClosest = (polygonIndexClosest == plane.getPoints().size() - 2) ? 0 : polygonIndexClosest + 2;
                }

                // Gradient to the next point
                gradient = gradient(prev_x,
                        prev_y,
                        plane.getPoints().get(polygonIndexClosest),
                        plane.getPoints().get(polygonIndexClosest + 1));

                // Available length to extrude on
                maxLength = length(prev_x,
                        prev_y,
                        plane.getPoints().get(polygonIndexClosest),
                        plane.getPoints().get(polygonIndexClosest + 1));

                sign = (int) signum(plane.getPoints().get(polygonIndexClosest) - prev_x);
                sign = sign == 0 ? 1 : sign;
                next_x = prev_x + sign * (faceLength / 2.0 / GV.METER_2_MAP_RATIO - extrudedLength) * (cos(atan(gradient)));
                next_y = prev_y + sign * (faceLength / 2.0 / GV.METER_2_MAP_RATIO - extrudedLength) * (sin(atan(gradient)));

                if (faceLength / 2.0 / GV.METER_2_MAP_RATIO - extrudedLength < maxLength) {
                    test.getPoints().addAll(next_x, next_y);
                }
            } while (faceLength / 2.0 / GV.METER_2_MAP_RATIO - extrudedLength > maxLength);
        }

        // Save frontage to object to export to gpx
        final double BIGGEST_EDGE = Math.abs(xMax[0] - xMin[0]) < Math.abs(yMax[0] - yMin[0]) ? Math.abs(yMax[0] - yMin[0]) : Math.abs(xMax[0] - xMin[0]);
        clientBooking.setFrontage(0, next_x / pane_Map.getWidth() * BIGGEST_EDGE + xMin[0]);
        clientBooking.setFrontage(1, next_y / pane_Map.getWidth() * BIGGEST_EDGE + yMin[0]);

        // Second Point
        gradient_ort = -1 / gradient;
        indexHeight[0] = test.getPoints().size() - 2;
        gradientHeight[0] = gradient_ort;

        next_x += clientBooking.getHeight() * cos(atan(gradient_ort));
        next_y += clientBooking.getHeight() * sin(atan(gradient_ort));

        // Extrude the other way
        if (!plane.contains(next_x, next_y)) {
            next_x -= 2 * clientBooking.getHeight() * cos(atan(gradient_ort));
            next_y -= 2 * clientBooking.getHeight() * sin(atan(gradient_ort));
        }
        test.getPoints().addAll(next_x, next_y);

        // Start again at the center
        // Third Point
        ArrayList<Double> pointsToAdd = new ArrayList<>();
        prev_x = xi;
        prev_y = yi;
        extrudedLength = 0;
        if (index_direction > 0) {
            polygonIndexClosest = polygonIndexClosestSave + 2;
        } else {
            polygonIndexClosest = polygonIndexClosestSave;
        }

        // Gradient to the next point
        gradient = gradient(prev_x,
                prev_y,
                plane.getPoints().get(polygonIndexClosest),
                plane.getPoints().get(polygonIndexClosest + 1));

        // The length to the next point
        maxLength = length(prev_x,
                prev_y,
                plane.getPoints().get(polygonIndexClosest),
                plane.getPoints().get(polygonIndexClosest + 1));

        // Subtracking - opposite direction
        sign = (int) signum(plane.getPoints().get(polygonIndexClosest) - prev_x);
        sign = sign == 0 ? 1 : sign;
        next_x = prev_x + sign * faceLength / 2.0 / GV.METER_2_MAP_RATIO * cos(atan(gradient));
        next_y = prev_y + sign * faceLength / 2.0 / GV.METER_2_MAP_RATIO * sin(atan(gradient));

        // Keep adding points until the desired extrusion length is met
        if (faceLength / 2.0 / GV.METER_2_MAP_RATIO < maxLength) {
            pointsToAdd.add(next_x);
            pointsToAdd.add(next_y);
        } else {
            do {
                prev_x = plane.getPoints().get(polygonIndexClosest);
                prev_y = plane.getPoints().get(polygonIndexClosest + 1);
                pointsToAdd.add(prev_x);
                pointsToAdd.add(prev_y);
                if (index_direction > 0) {
                    polygonIndexClosest = (polygonIndexClosest == plane.getPoints().size() - 2) ? 0 : polygonIndexClosest + 2;
                } else {
                    polygonIndexClosest = (polygonIndexClosest == 0) ? plane.getPoints().size() - 2 : polygonIndexClosest - 2;
                }
                extrudedLength += maxLength;

                gradient = gradient(prev_x,
                        prev_y,
                        plane.getPoints().get(polygonIndexClosest),
                        plane.getPoints().get(polygonIndexClosest + 1));

                maxLength = length(prev_x,
                        prev_y,
                        plane.getPoints().get(polygonIndexClosest),
                        plane.getPoints().get(polygonIndexClosest + 1));

                sign = (int) signum(plane.getPoints().get(polygonIndexClosest) - prev_x);
                sign = sign == 0 ? 1 : sign;
                next_x = prev_x + sign * (faceLength / 2.0 / GV.METER_2_MAP_RATIO - extrudedLength) * cos(atan(gradient));
                next_y = prev_y + sign * (faceLength / 2.0 / GV.METER_2_MAP_RATIO - extrudedLength) * sin(atan(gradient));

                if (faceLength / 2.0 / GV.METER_2_MAP_RATIO - extrudedLength < maxLength) {
                    pointsToAdd.add(next_x);
                    pointsToAdd.add(next_y);
                }
            } while (faceLength / 2.0 / GV.METER_2_MAP_RATIO - extrudedLength > maxLength);
        }

        clientBooking.setFrontage(2, next_x / pane_Map.getWidth() * BIGGEST_EDGE + xMin[0]);
        clientBooking.setFrontage(3, next_y / pane_Map.getWidth() * BIGGEST_EDGE + yMin[0]);

        // Third Point
        gradient_ort = -1 / gradient;
        indexHeight[1] = test.getPoints().size() + 2;
        gradientHeight[1] = gradient_ort;
        next_x += clientBooking.getHeight() * cos(atan(gradient_ort));
        next_y += clientBooking.getHeight() * sin(atan(gradient_ort));
        if (!plane.contains(next_x, next_y)) {
            next_x -= 2 * clientBooking.getHeight() * cos(atan(gradient_ort));
            next_y -= 2 * clientBooking.getHeight() * sin(atan(gradient_ort));
        }
        test.getPoints().addAll(next_x, next_y);

        // Adds the rest of the points
        for (int i = pointsToAdd.size() - 1; i >= 0; i -= 2) {
            test.getPoints().addAll(pointsToAdd.get(i - 1), pointsToAdd.get(i));
        }

        // Increases the length until the area is 99.9% accurate
        while (area(test) <= 0.999 * area) {
            clientBooking.setHeight(clientBooking.getHeight() + 1);
            double tempx = test.getPoints().get(indexHeight[0]) + clientBooking.getHeight() * cos(atan(gradientHeight[0]));
            double tempy = test.getPoints().get(indexHeight[0] + 1) + clientBooking.getHeight() * sin(atan(gradientHeight[0]));

            if (!plane.contains(tempx, tempy)) {
                tempx = test.getPoints().get(indexHeight[0]) - clientBooking.getHeight() * cos(atan(gradientHeight[0]));
                tempy = test.getPoints().get(indexHeight[0] + 1) - clientBooking.getHeight() * sin(atan(gradientHeight[0]));
            }

            test.getPoints().set(indexHeight[0] + 2, tempx);
            test.getPoints().set(indexHeight[0] + 3, tempy);

            tempx = test.getPoints().get(indexHeight[1]) + clientBooking.getHeight() * cos(atan(gradientHeight[1]));
            tempy = test.getPoints().get(indexHeight[1] + 1) + clientBooking.getHeight() * sin(atan(gradientHeight[1]));

            if (!plane.contains(tempx, tempy)) {
                tempx = test.getPoints().get(indexHeight[1]) - clientBooking.getHeight() * cos(atan(gradientHeight[1]));
                tempy = test.getPoints().get(indexHeight[1] + 1) - clientBooking.getHeight() * sin(atan(gradientHeight[1]));
            }

            test.getPoints().set(indexHeight[1] - 2, tempx);
            test.getPoints().set(indexHeight[1] - 1, tempy);

            Line2D line1 = new Line2D.Double(test.getPoints().get(indexHeight[0]),
                    test.getPoints().get(indexHeight[0] + 1),
                    test.getPoints().get(indexHeight[0] + 2),
                    test.getPoints().get(indexHeight[0] + 3));
            Line2D line2 = new Line2D.Double(test.getPoints().get(indexHeight[1]),
                    test.getPoints().get(indexHeight[1] + 1),
                    test.getPoints().get(indexHeight[1] - 2),
                    test.getPoints().get(indexHeight[1] - 1));

            if (line1.intersectsLine(line2)
                    || !plane.contains(test.getPoints().get(indexHeight[0] + 2),
                            test.getPoints().get(indexHeight[0] + 3))
                    || !plane.contains(test.getPoints().get(indexHeight[1] - 2),
                            test.getPoints().get(indexHeight[1] - 1))) {
                placed = false;
                break;
            }
        }

        double[] center = getCenter(test);

        // Test whether the booking can be placed where the user wants it
        if (placed && plane.contains(center[0], center[1])) {
            booking.getPoints().clear();
            booking.getPoints().addAll(test.getPoints());
            label.setWrappingWidth(booking.getLayoutBounds().getWidth() / 2.0);
            label.setX((booking.getLayoutBounds().getMaxX() + booking.getLayoutBounds().getMinX()) / 2.0 - label.getLayoutBounds().getWidth() / 2.0);
            label.setY((booking.getLayoutBounds().getMaxY() + booking.getLayoutBounds().getMinY()) / 2.0);
            clientBooking.setHeight(1);
            infoLabel.setText("Real Area: " + area(test) + " m\u00B2");
        } else {
            infoLabel.setText("Booking can't fit into this space.");
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
                String frontage = Arrays.toString(booking.getFrontage()).replaceAll(",", ";");
                String[] tmp = booking.toString().split(",");
                if (tmp[1].equalsIgnoreCase("booking")) {
                    writer.write(tmp[1] + "," + tmp[2] + "," + tmp[3] + "," + tmp[4] + "," + tmp[5] + "," + tmp[6] + "," + points + "," + colour + "," + frontage + "\n");
                } // For the future case where the programmer wants to implement different algorithms for different kind of bookings 
                else {
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

    public double[] getDrag() {
        return new double[]{delX, delY};
    }
}
