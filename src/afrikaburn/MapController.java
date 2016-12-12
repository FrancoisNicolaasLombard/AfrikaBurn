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
    private final Polygon[] highRes;// High Resolution Polygons
    private final Polygon[] lowRes; // Low Resolution Polygons
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

    /**
     * @Description: Reads the map from the file (name must be
     * afrikaburnmap.json) and loads the coordinates.
     */
    MapController(Label infoLabel, Pane map, Booking[] clients, Polygon[] clientPolygons) {
        this.clientPolygons = clientPolygons;
        this.clients = clients;
        this.infoLabel = infoLabel;
        this.canvas = map;
        delX = 0;
        delY = 0;
        JSONReader reader
                = new JSONReader(new File("resources/afrikaburnmapv2.json"));
        highRes = reader.polygons();
        lowRes = reader.polygons();
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
        for (Polygon current : highRes) {
            current.setFill(Color.LIGHTGREY);
            current.setStroke(Color.BLACK);
            current.setStrokeWidth(0.25);
            setMapListeners(current);
            canvas.getChildren().add(current);
        }

        for (Polygon current : clientPolygons) {
            current.setStroke(Color.BLACK);
            current.setStrokeWidth(0.25);
            current.setFill(Color.DODGERBLUE);

            setClientListeners(current);
            canvas.getChildren().add(current);
        }

        // The origin is in the top left hand corner, changes it to bottom left
        canvas.getTransforms().add(new Rotate(0, GV.MAP_WIDTH / 2,
                GV.MAP_HEIGHT / 2));
    }

    /**
     * This method neatens up the code and gives the highRes its listeners
     *
     * @param current
     */
    private void setMapListeners(Polygon current) {
        current.setOnMouseClicked(e -> {

            infoLabel.setText("Total Area: " + round(area(current) * 100)
                    / 100.0 + " m\u00B2");

            //bookedArea(current, 10, 100, e.getX(), e.getY());
        });

        current.setOnMousePressed(e -> {
            editing = true;
            for (Polygon booking : clientPolygons) {
                if (booking.contains(e.getX(), e.getY())) {
                    System.out.println("WORKING");
                }
            }
        });
        current.setOnMouseDragged(e -> {
            // moveBooking(current, booking, faceLength, area, e.getX(), e.getY());
        });
        current.setOnMouseReleased(e -> {
            editing = false;
        });

        current.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                String nodeId = db.getString();
                System.out.println(nodeId);
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });

        current.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString()) {

                e.acceptTransferModes(TransferMode.COPY);

                Polygon booking = clients[Integer.parseInt(e.getDragboard().getString().split(",")[0])].getArea();
                booking.getPoints().removeAll(booking.getPoints());

                moveBooking(current,
                        booking,
                        Double.parseDouble(e.getDragboard().getString().split(",")[2]),
                        Double.parseDouble(e.getDragboard().getString().split(",")[3]),
                        e.getX(),
                        e.getY());
            }
            e.consume();
        });

        current.setOnDragExited(e -> {
            current.setFill(Color.LIGHTGREY);
        });
    }

    private void setClientListeners(Polygon current) {
        current.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString()) {

                e.acceptTransferModes(TransferMode.COPY);

                for (Polygon plane : highRes) {
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
        xMin = highRes[0].getPoints().get(0);
        yMin = highRes[0].getPoints().get(1);
        xMax = xMin;
        yMax = yMin;
        for (int count = 0; count < totalPolygons; count++) {
            for (int coords = 0; coords < highRes[count].getPoints().size(); coords += 2) {
                if (highRes[count].getPoints().get(coords + 1) > yMax) {
                    yMax = highRes[count].getPoints().get(coords + 1);
                } else if (highRes[count].getPoints().get(coords + 1) < yMin) {
                    yMin = highRes[count].getPoints().get(coords + 1);
                }
                if (highRes[count].getPoints().get(coords) > xMax) {
                    xMax = highRes[count].getPoints().get(coords);
                } else if (highRes[count].getPoints().get(coords) < xMin) {
                    xMin = highRes[count].getPoints().get(coords);
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

        // Normalise the highRes
        for (int count = 0; count < totalPolygons; count++) {
            ObservableList<Double> tmpPolygon = highRes[count].getPoints();
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
     * This method zooms the canvas - not changing the size of the highRes
     */
    public void zoomIn() {
        canvas.setScaleX(canvas.getScaleX() * GV.ZOOM_AMOUNT);
        canvas.setScaleY(canvas.getScaleY() * GV.ZOOM_AMOUNT);
    }

    /**
     * This method zooms the canvas - not changing the size of the highRes
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

    /**
     *
     * @Author: FN Lombard
     * @Company: VASTech
     * @Description: This method increases the resolution of the polygons in
     * order to increase the accuracy of the bookedArea method.
     */
    public void increaseResolution() {
        for (Polygon polygon : highRes) {
            Polygon tmp = new Polygon();
            double x1, x2, y1, y2, x_len, y_len;
            int originalPoints = polygon.getPoints().size(), tracker = 0;

            for (int i = 0; i < originalPoints - 3; i += 2) {
                x1 = polygon.getPoints().get(i + tracker);
                y1 = polygon.getPoints().get(i + 1 + tracker);
                x2 = polygon.getPoints().get(i + 2 + tracker);
                y2 = polygon.getPoints().get(i + 3 + tracker);

                x_len = x2 - x1;
                y_len = y2 - y1;

                double m = y_len / x_len;
                double c = y2 - m * x2;
                int x_sign = (int) (x_len / abs(x_len));

                // X-Dimension Resolution
                for (int j = 0; j < abs(x_len); j += 2) {
                    if (m == 0) {
                        tmp.getPoints().addAll(j * x_sign + x1, y1);
                    } else if (m == NaN) {
                        tmp.getPoints().addAll(x1, y1 + j * x_sign);
                    } else {
                        tmp.getPoints().addAll(j * x_sign + x1, m * (j * x_sign + x1) + c);
                    }
                }
            }
            polygon.getPoints().removeAll(polygon.getPoints());
            polygon.getPoints().setAll(tmp.getPoints());
        }
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
        GeoLine closest = new GeoLine(plane.getPoints().get(0), plane.getPoints().get(1),
                plane.getPoints().get(2), plane.getPoints().get(3));
        double shortest = closest.cent2Point(mouseX, mouseY);

        // Find the line closest to the mouse pointer
        for (int x = 2; x < plane.getPoints().size() - 2; x += 2) {
            GeoLine tmp = new GeoLine(plane.getPoints().get(x), plane.getPoints().get(x + 1),
                    plane.getPoints().get(x + 2), plane.getPoints().get(x + 3));
            if (tmp.cent2Point(mouseX, mouseY) < shortest) {
                closest = tmp;
                shortest = tmp.cent2Point(mouseX, mouseY);
            }
        }

        // Find the closest point to the mouse pointer
        double m1 = gradient(closest);
        double m2 = -1 / m1;
        double c1 = closest.getCent()[1] - m1 * closest.getCent()[0];
        double c2 = mouseY - m2 * mouseX;

        double xi;
        double yi;

        if (m2 == 0) {
            xi = closest.getCent()[0];
            yi = mouseY;
        } else if (m1 == 0) {
            xi = mouseX;
            yi = closest.getCent()[1];
        } else {
            xi = (c2 - c1) / (m1 - m2);
            yi = m1 * xi + c1;
        }

        // The closest to the mouse pointer is the origin
        double prev_x = xi;
        double prev_y = yi; // Next point on the polygon.
        double next_x;
        double next_y;

        ObservableList<Double> oldPoints = booking.getPoints();
        booking.getPoints().removeAll(oldPoints);

        booking.getPoints().addAll(xi, yi);
        // Draw the next point following the polygon following the polygon line

        next_x = prev_x + faceLength / 2.0 / GV.METER_2_MAP_RATIO * cos(atan(m1));
        next_y = prev_y + faceLength / 2.0 / GV.METER_2_MAP_RATIO * sin(atan(m1));

        booking.getPoints().addAll(next_x, next_y);

        // Normalise the length
        next_x += area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(m2));
        next_y += area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(m2));
        if (!plane.contains(next_x, next_y)) {
            next_x -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(m2));
            next_y -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(m2));
        }

        booking.getPoints().addAll(next_x, next_y);

        // Normalise the length
        next_x -= faceLength / GV.METER_2_MAP_RATIO * cos(atan(m1));
        next_y -= faceLength / GV.METER_2_MAP_RATIO * sin(atan(m1));
        if (!plane.contains(next_x, next_y)) {
            next_x += 2 * faceLength / GV.METER_2_MAP_RATIO * cos(atan(m1));
            next_y += 2 * faceLength / GV.METER_2_MAP_RATIO * sin(atan(m1));
        }

        booking.getPoints().addAll(next_x, next_y);

        // Normalise the length
        next_x += area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(m2));
        next_y += area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(m2));
        if (round((next_y - yi) / (next_x - xi) * 100) != round(m1 * 100)) {
            next_x -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(m2));
            next_y -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(m2));
        }
        booking.getPoints().addAll(next_x, next_y, xi, yi);
        
    }

    public void removeTrans() {
        canvas.getChildren().forEach(e -> {
            e.getTransforms().removeAll(e.getTransforms());
        });
        delX = 0;
        delY = 0;
    }

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
