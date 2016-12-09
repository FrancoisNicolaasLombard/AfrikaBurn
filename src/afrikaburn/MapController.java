package afrikaburn;

import java.io.File;
import static java.lang.Double.NaN;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
    final Polygon[] highRes;
    final Polygon[] lowRes;
    //final Polygon[] clients;
    private double yMin;
    private double yMax;
    private double xMin;
    private double xMax;
    private double delX;
    private double delY;
    private final int totalPolygons;
    private final Label infoLabel;
    private final Pane canvas;
    private boolean editing = false;

    /**
     * @Description: Reads the map from the file (name must be
     * afrikaburnmap.json) and loads the coordinates.
     */
    MapController(Label infoLabel, Pane map) {
        this.infoLabel = infoLabel;
        this.canvas = map;
        delX = 0;
        delY = 0;
        JSONReader reader
                = new JSONReader(new File("resources/afrikaburnmapv2.json"));
        highRes = reader.polygons();
        lowRes = reader.polygons();
        totalPolygons = reader.getTotalPolygons();
        //clients = new Polygons();
        minMax();
        portMap();

        //increaseResolution();
        getMap();
    }

    /**
     * @return @Author FN Lombard
     * @Company: VASTech
     * @Description: This method builds the map and returns it to the controller
     */
    public void getMap() {
        for (Polygon current : highRes) {
            current.setFill(Color.LIGHTGREY);
            current.setStroke(Color.BLACK);
            current.setStrokeWidth(0.25);
            setListeners(current);
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
    private void setListeners(Polygon current) {
        current.setOnMouseClicked(e -> {
            infoLabel.setText("Total Area: " + round(area(current) * 100)
                    / 100.0 + " m\u00B2");
            bookedArea(current, 10, 100, e.getX(), e.getY());
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
            if (e.getGestureSource() != current
                    && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.COPY);
                current.setFill(Color.BLUE);
            }
            System.out.println("TEST");
            e.consume();
        });

        current.setOnDragExited(e -> {
            current.setFill(Color.LIGHTGREY);
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

    /**
     * Find the line closest to the mouse.
     *
     * @param mouseX
     * @Author: FN Lombard
     * @Company: VASTech
     * @Description: This method draws a polygon with the correct face-length
     * and area closest to the cursor when clicked.
     *
     * @param faceLength
     * @param plane
     * @param area
     * @param mouseY
     */
    public void bookedArea(Polygon plane, double faceLength, double area, double mouseX, double mouseY) {
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
        double halfFace = faceLength / 2.0;

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

        // Next point on the polygon.
        double next_x, prev_x;
        double next_y, prev_y;
        Polygon booking = new Polygon();

        // The closest to the mouse pointer is the origin
        prev_x = xi;
        prev_y = yi;

        booking.getPoints().addAll(xi, yi);

        // Draw the next point following the polygon following the polygon line
        next_x = prev_x + halfFace / GV.METER_2_MAP_RATIO * cos(atan(m1));
        next_y = prev_y + halfFace / GV.METER_2_MAP_RATIO * sin(atan(m1));

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

        booking.getPoints().addAll(next_x, next_y);

        //dot.getTransforms().addAll(new Translate(delX, delY));
        booking.setStroke(Color.BLACK);
        booking.setStrokeWidth(0.25);
        booking.setFill(Color.DODGERBLUE);
        booking.getTransforms().addAll(new Translate(delX, delY));
        booking.setOnMousePressed(e -> {
            editing = true;
        });
        booking.setOnMouseDragged(e -> {
            double mouseX_new = e.getX();
            double mouseY_new = e.getY();
            booking.getPoints().removeAll(booking.getPoints());
            // Declare two points and use the first two as default
            GeoLine closest_new = new GeoLine(plane.getPoints().get(0), plane.getPoints().get(1),
                    plane.getPoints().get(2), plane.getPoints().get(3));
            double shortest_new = closest_new.cent2Point(mouseX_new, mouseY_new);

            // Find the line closest to the mouse pointer
            for (int x = 2; x < plane.getPoints().size() - 2; x += 2) {
                GeoLine tmp = new GeoLine(plane.getPoints().get(x), plane.getPoints().get(x + 1),
                        plane.getPoints().get(x + 2), plane.getPoints().get(x + 3));
                if (tmp.cent2Point(mouseX_new, mouseY_new) < shortest_new) {
                    closest_new = tmp;
                    shortest_new = tmp.cent2Point(mouseX_new, mouseY_new);
                }
            }

            // Find the closest point to the mouse pointer
            double m1_new = gradient(closest_new);
            double m2_new = -1 / m1_new;
            double c1_new = closest_new.getCent()[1] - m1_new * closest_new.getCent()[0];
            double c2_new = mouseY_new - m2_new * mouseX_new;

            double xi_new;
            double yi_new;

            if (m2_new == 0) {
                xi_new = closest_new.getCent()[0];
                yi_new = mouseY_new;
            } else if (m1_new == 0) {
                xi_new = mouseX_new;
                yi_new = closest_new.getCent()[1];
            } else {
                xi_new = (c2_new - c1_new) / (m1_new - m2_new);
                yi_new = m1_new * xi_new + c1_new;
            }

            // The closest to the mouse pointer is the origin
            double prev_x_new = xi_new;
            double prev_y_new = yi_new; // Next point on the polygon.
            double next_x_new;
            double next_y_new;

            booking.getPoints().addAll(xi_new, yi_new);

            // Draw the next point following the polygon following the polygon line
            next_x_new = prev_x_new + halfFace / GV.METER_2_MAP_RATIO * cos(atan(m1_new));
            next_y_new = prev_y_new + halfFace / GV.METER_2_MAP_RATIO * sin(atan(m1_new));

            booking.getPoints().addAll(next_x_new, next_y_new);

            // Normalise the length
            next_x_new += area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(m2_new));
            next_y_new += area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(m2_new));
            if (!plane.contains(next_x_new, next_y_new)) {
                next_x_new -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(m2_new));
                next_y_new -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(m2_new));
            }

            booking.getPoints().addAll(next_x_new, next_y_new);

            // Normalise the length
            next_x_new -= faceLength / GV.METER_2_MAP_RATIO * cos(atan(m1_new));
            next_y_new -= faceLength / GV.METER_2_MAP_RATIO * sin(atan(m1_new));
            if (!plane.contains(next_x_new, next_y_new)) {
                next_x_new += 2 * faceLength / GV.METER_2_MAP_RATIO * cos(atan(m1_new));
                next_y_new += 2 * faceLength / GV.METER_2_MAP_RATIO * sin(atan(m1_new));
            }

            booking.getPoints().addAll(next_x_new, next_y_new);

            // Normalise the length
            next_x_new += area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(m2_new));
            next_y_new += area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(m2_new));
            if (round((next_y_new - yi_new) / (next_x_new - xi_new) * 100) != round(m1_new * 100)) {
                next_x_new -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * cos(atan(m2_new));
                next_y_new -= 2 * area / faceLength / GV.METER_2_MAP_RATIO * sin(atan(m2_new));
            }

            booking.getPoints().addAll(next_x_new, next_y_new);
        });
        booking.setOnMouseReleased(e -> {
            editing = false;
        });
        canvas.getChildren().add(booking);
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
}
