package afrikaburn;

import java.io.File;
import static java.lang.Double.NaN;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * @Author FN Lombard
 * @Company: VASTech
 * @Description:
 */
public final class MapController {

    // Class variables
    private final Polygon[] mapPolygons;// High Resolution Polygons
    private final Polygon[] clientPolygons;
    private final Booking[] clients;// All of the Clients
    private double yMin;    // Smallest Y-Value of map
    private double yMax;    // Largest Y-Value of map
    private double xMin;    // Smallest X-Value of map
    private double xMax;    // Largest X-Value of map
    private double delX;    // Total dragged X
    private double delY;    // Total dragged Y
    private final int totalPolygons;
    private final Label infoLabel;
    private final Pane canvas;
    private boolean editing = false;
    private final Line one;
    private final Line two;

    /**
     * @Description: Reads the map from the file (name must be
     * afrikaburnmap.json) and loads the coordinates.
     */
    MapController(Label infoLabel, Pane map, Booking[] clients, Polygon[] clientPolygons) {
        this.clientPolygons = clientPolygons;
        this.clients = clients;
        this.infoLabel = infoLabel;
        this.canvas = map;
        one = new Line();
        two = new Line();
        delX = 0;
        delY = 0;
        JSONReader reader
                = new JSONReader(new File("resources/afrikaburnmapv2.json"));
        mapPolygons = reader.polygons();
        totalPolygons = reader.getTotalPolygons();

        minMax();
        portMap();

        setupMap();
        // Read saved bookings
    }

    /**
     * @return @Author FN Lombard
     * @Company: VASTech
     * @Description: This method builds the map and returns it to the controller
     */
    public void setupMap() {

        one.setStrokeWidth(0.1);
        two.setStrokeWidth(0.1);

        for (Polygon current : mapPolygons) {
            current.setFill(Color.LIGHTGREY);
            current.setStroke(Color.BLACK);
            current.setStrokeWidth(0.05);
            current.setOpacity(0.8);
            setMapListeners(current);
            canvas.getChildren().add(current);
        }

        for (Polygon current : clientPolygons) {
            current.setStroke(Color.BLACK);
            current.setStrokeWidth(0.25);
            current.setFill(Color.DODGERBLUE);
            current.setOpacity(0.5);
            setClientListeners(current);
            canvas.getChildren().add(current);
        }

        // The origin is in the top left hand corner, changes it to bottom left
        canvas.getChildren().addAll(one, two);
        canvas.setOnDragExited(e -> {
            editing = false;
            clients[Integer.parseInt(e.getDragboard().getString().split(",")[0])].getArea().setOpacity(1);
            e.consume();
        });
        canvas.getTransforms().add(new Rotate(0, GV.MAP_WIDTH / 2,
                GV.MAP_HEIGHT / 2));
    }

    /**
     * This method neatens up the code and gives the mapPolygons its listeners
     *
     * @param current
     */
    private void setMapListeners(Polygon current) {
        current.setOnMouseClicked(e -> {

            infoLabel.setText("Total Area: " + round(area(current) * 100)
                    / 100.0 + " m\u00B2");
        });

        current.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String nodeId = db.getString();
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });

        current.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString()) {

                e.acceptTransferModes(TransferMode.MOVE);

                Polygon booking = clients[Integer.parseInt(e.getDragboard().getString().split(",")[0])].getArea();

                moveBooking(current,
                        booking,
                        Double.parseDouble(e.getDragboard().getString().split(",")[2]),
                        Double.parseDouble(e.getDragboard().getString().split(",")[3]),
                        e.getX(),
                        e.getY());
            }
            e.consume();
        });

    }

    private void setClientListeners(Polygon current) {
        current.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString() && clients[Integer.parseInt(e.getDragboard().getString().split(",")[0])].getArea() == current) {

                e.acceptTransferModes(TransferMode.MOVE);

                for (Polygon plane : mapPolygons) {
                    if (plane.contains(getCenter(current)[0], getCenter(current)[1])) {
                        moveBooking(plane,
                                current,
                                Double.parseDouble(e.getDragboard().getString().split(",")[2]),
                                Double.parseDouble(e.getDragboard().getString().split(",")[3]),
                                e.getX(),
                                e.getY());
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
        xMin = mapPolygons[0].getPoints().get(0);
        yMin = mapPolygons[0].getPoints().get(1);
        xMax = xMin;
        yMax = yMin;
        for (int count = 0; count < totalPolygons; count++) {
            for (int coords = 0; coords < mapPolygons[count].getPoints().size(); coords += 2) {
                if (mapPolygons[count].getPoints().get(coords + 1) > yMax) {
                    yMax = mapPolygons[count].getPoints().get(coords + 1);
                } else if (mapPolygons[count].getPoints().get(coords + 1) < yMin) {
                    yMin = mapPolygons[count].getPoints().get(coords + 1);
                }
                if (mapPolygons[count].getPoints().get(coords) > xMax) {
                    xMax = mapPolygons[count].getPoints().get(coords);
                } else if (mapPolygons[count].getPoints().get(coords) < xMin) {
                    xMin = mapPolygons[count].getPoints().get(coords);
                }
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
    private void portMap() {
        //Find the smallest edge of map
        final double DEL_Y = Math.abs(yMax - yMin);
        final double DEL_X = Math.abs(xMax - xMin);
        final double BIGGEST_EDGE = DEL_X < DEL_Y ? DEL_Y : DEL_X;

        // Normalise the mapPolygons
        for (int count = 0; count < totalPolygons; count++) {
            ObservableList<Double> tmpPolygon = mapPolygons[count].getPoints();
            for (int coords = 0; coords < tmpPolygon.size(); coords += 2) {
                tmpPolygon.set(coords, (tmpPolygon.get(coords) - xMin)
                        / BIGGEST_EDGE * GV.MAP_WIDTH);
                tmpPolygon.set(coords + 1, (tmpPolygon.get(coords + 1) - yMin)
                        / BIGGEST_EDGE * GV.MAP_HEIGHT);
            }
        }
    }

    /**
     *
     * @param deltaX
     * @param deltaY
     */
    public void dragMap(double deltaX, double deltaY) {
        if (!editing) {
            canvas.getChildren().forEach((component) -> {
                component.getTransforms().add(new Translate(deltaX, deltaY));
            });
            delX += deltaX;
            delY += deltaY;
        }
    }

    /**
     * This method zooms the canvas - not changing the size of the mapPolygons
     */
    public void zoomIn() {
        canvas.setScaleX(canvas.getScaleX() * GV.ZOOM_AMOUNT);
        canvas.setScaleY(canvas.getScaleY() * GV.ZOOM_AMOUNT);
    }

    /**
     * This method zooms the canvas - not changing the size of the mapPolygons
     */
    public void zoomOut() {
        canvas.setScaleX(canvas.getScaleX() / GV.ZOOM_AMOUNT);
        canvas.setScaleY(canvas.getScaleY() / GV.ZOOM_AMOUNT);
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

    private double gradient(GeoLine line) {
        double dy = line.getY2() - line.getY1();
        double dx = line.getX2() - line.getX1();
        return dy / dx;
    }

    private double length(double x1, double y1, double x2, double y2) {
        return sqrt(pow(x1 - x2, 2) + pow(y1 - y2, 2));
    }

    private double gradient(double x1, double y1, double x2, double y2) {
        double dy = y2 - y1;
        double dx = x2 - x1;
        return round(dy / dx * 100) / 100.0;
    }

    private void moveBooking(Polygon plane, Polygon booking, double faceLength, double area, double mouseX, double mouseY) {
        // Declare two points and use the first two as default
        double[] line_01 = new double[4];
        double[] line_02 = 
        double x1, y1, x2, y2;
        double shortest = 0;
        double sec_shortest = shortest;

        // Find the line closest to the mouse pointer
        for (int x = 2; x < plane.getPoints().size() - 2; x += 2) {
            x1 = plane.getPoints().get(x);
            y1 = plane.getPoints().get(x + 1);
            x2 = plane.getPoints().get(x + 2);
            y2 = plane.getPoints().get(x + 3);
            double temp_distance = length((x1 + x2) / 2.0, (y1 + y2) / 2.0, mouseX, mouseY);
            
            if (temp_distance < shortest) {
                line_01[0] = x1;
                line_01[1] = y1;
                line_01[2] = x2;
                line_01[3] = y2;
                        
                shortest = temp_distance;
            } else if (temp_distance < sec_shortest) {
                sec_closest = tmp;
                sec_shortest = temp_distance;
            }
        }

        // Find the closest point to the mouse pointer
        double gradient_1 = gradient(closest);
        double gradient_ort_1 = -1 / gradient_1;
        double offset_1 = closest.getCent()[1] - gradient_1 * closest.getCent()[0];
        double offset_ort_1 = mouseY - gradient_ort_1 * mouseX;

        double gradient_2 = gradient(sec_closest);
        double gradient_ort_2 = -1 / gradient_2;
        double offset_2 = sec_closest.getCent()[1] - gradient_2 * sec_closest.getCent()[0];
        double offset_ort_2 = mouseY - gradient_ort_2 * mouseX;

        double xi_1;
        double yi_1;
        double xi_2;
        double yi_2;
        double xi;
        double yi;
        double gradient;
        double gradient_ort;

        if (gradient_ort_1 == 0) {
            xi_1 = closest.getCent()[0];
            yi_1 = mouseY;
        } else if (gradient_1 == 0) {
            xi_1 = mouseX;
            yi_1 = closest.getCent()[1];
        } else {
            xi_1 = (offset_ort_1 - offset_1) / (gradient_1 - gradient_ort_1);
            yi_1 = gradient_1 * xi_1 + offset_1;
        }

        if (gradient_ort_2 == 0) {
            xi_2 = sec_closest.getCent()[0];
            yi_2 = mouseY;
        } else if (gradient_2 == 0) {
            xi_2 = mouseX;
            yi_2 = sec_closest.getCent()[1];
        } else {
            xi_2 = (offset_ort_2 - offset_2) / (gradient_2 - gradient_ort_2);
            yi_2 = gradient_2 * xi_2 + offset_2;
        }

        if (length(xi_1, yi_1, mouseX, mouseY) < length(xi_2, yi_2, mouseX, mouseY)) {
            xi = xi_1;
            yi = yi_1;
            gradient = gradient_1;
            gradient_ort = gradient_ort_1;
            one.setStroke(Color.RED);
            two.setStroke(Color.BLACK);
        } else {
            xi = xi_2;
            yi = yi_2;
            gradient = gradient_2;
            gradient_ort = gradient_ort_2;
            two.setStroke(Color.RED);
            one.setStroke(Color.BLACK);
        }

        one.setStartX(mouseX);
        one.setStartY(mouseY);
        one.setEndX(xi_1);
        one.setEndY(yi_1);

        two.setStartX(mouseX);
        two.setStartY(mouseY);
        two.setEndX(xi_2);
        two.setEndY(yi_2);

        // The closest to the mouse pointer is the origin
        double prev_x = xi;
        double prev_y = yi; // Next point on the polygon.
        double next_x;
        double next_y;

        ObservableList<Double> oldPoints = booking.getPoints();
        Polygon test = new Polygon();
        booking.getPoints().removeAll(oldPoints);

        test.getPoints().addAll(xi, yi);
        // Draw the next point following the polygon following the polygon line

        next_x = prev_x + faceLength / 2.0 / GV.METER_2_MAP_RATIO * cos(atan(gradient));
        next_y = prev_y + faceLength / 2.0 / GV.METER_2_MAP_RATIO * sin(atan(gradient));

        test.getPoints().addAll(next_x, next_y);

        // Normalise the length
        next_x += area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort));
        next_y += area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort));
        if (!plane.contains(next_x, next_y)) {
            next_x -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort));
            next_y -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort));
        }

        test.getPoints().addAll(next_x, next_y);

        // Normalise the length
        next_x -= faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient));
        next_y -= faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient));
        if (!plane.contains(next_x, next_y)) {
            next_x += 2 * faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient));
            next_y += 2 * faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient));
        }

        test.getPoints().addAll(next_x, next_y);

        // Normalise the length
        next_x -= area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort));
        next_y -= area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort));
        if (round((next_y - yi) / (next_x - xi) * 100) != round(gradient * 100)) {
            next_x += 2 * area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(gradient_ort));
            next_y += 2 * area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(gradient_ort));
        }
        test.getPoints().addAll(next_x, next_y, xi, yi);

        double[] center = getCenter(test);
        if (test.contains(center[0], center[1])) {
            booking.getPoints().addAll(test.getPoints());
        } else {
            booking.getPoints().addAll(oldPoints);
            System.out.println("Work");
        }
    }

    /**
     *
     */
    public void removeTrans() {
        canvas.getChildren().forEach(e -> {
            e.getTransforms().removeAll(e.getTransforms());
        });
        delX = 0;
        delY = 0;
    }

    /**
     *
     * @param pol
     * @return
     */
    private double[] getCenter(Polygon pol) {
        double[] center = new double[2];
        center[0] = 0;
        center[1] = 0;
        int points = 0;
        for (int i = 0; i < pol.getPoints().size(); i += 2) {
            center[0] += pol.getPoints().get(points++);
            center[1] += pol.getPoints().get(points++);
        }
        center[0] /= points / 2.0;
        center[1] /= points / 2.0;
        return center;
    }
}
