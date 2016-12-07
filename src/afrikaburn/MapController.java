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
import static java.lang.Math.toDegrees;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
    final Polygon[] polygons;
    private double yMin;
    private double yMax;
    private double xMin;
    private double xMax;
    private double delX;
    private double delY;
    private final int totalPolygons;
    private final Label infoLabel;
    private final Pane canvas;

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
        polygons = reader.polygons();
        totalPolygons = reader.getTotalPolygons();

        minMax();
        portMap();

        increaseResolution();
        getMap();
    }

    /**
     * @return @Author FN Lombard
     * @Company: VASTech
     * @Description: This method builds the map and returns it to the controller
     */
    public void getMap() {
        for (Polygon current : polygons) {
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
     * This method neatens up the code and gives the polygons its listeners
     *
     * @param current
     */
    private void setListeners(Polygon current) {
        current.setOnMouseClicked(e -> {
            infoLabel.setText("Total Area: " + Math.round(area(current) * 100)
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
        xMin = polygons[0].getPoints().get(0);
        yMin = polygons[0].getPoints().get(1);
        xMax = xMin;
        yMax = yMin;
        for (int count = 0; count < totalPolygons; count++) {
            for (int coords = 0; coords < polygons[count].getPoints().size(); coords += 2) {
                if (polygons[count].getPoints().get(coords + 1) > yMax) {
                    yMax = polygons[count].getPoints().get(coords + 1);
                } else if (polygons[count].getPoints().get(coords + 1) < yMin) {
                    yMin = polygons[count].getPoints().get(coords + 1);
                }
                if (polygons[count].getPoints().get(coords) > xMax) {
                    xMax = polygons[count].getPoints().get(coords);
                } else if (polygons[count].getPoints().get(coords) < xMin) {
                    xMin = polygons[count].getPoints().get(coords);
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

        // Normalise the polygons
        for (int count = 0; count < totalPolygons; count++) {
            ObservableList<Double> tmpPolygon = polygons[count].getPoints();
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
        canvas.getChildren().forEach((component) -> {
            component.getTransforms().add(new Translate(deltaX, deltaY));
        });
        delX += deltaX;
        delY += deltaY;
    }

    /**
     * This method zooms the canvas - not changing the size of the polygons
     */
    public void zoomIn() {
        canvas.setScaleX(canvas.getScaleX() * GV.ZOOM_AMOUNT);
        canvas.setScaleY(canvas.getScaleY() * GV.ZOOM_AMOUNT);
    }

    /**
     * This method zooms the canvas - not changing the size of the polygons
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
        return Math.abs(area);
    }

    /**
     * Find the line closest to the mouse.
     *
     * @Author: FN Lombard
     * @Company: VASTech
     * @Description: This method draws a polygon with the correct face-length
     * and area closest to the cursor when clicked.
     *
     * @param faceLength
     * @param plane
     * @param area
     * @param mouseX
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

        infoLabel.setText(infoLabel.getText() + " " + "Line length: "
                + faceLength + " m");

        // Find the closest point to the mouse pointer
        double m1 = gradient(closest);
        double m2 = -1 / m1;
        double sign_m1 = m1 / abs(m1);
        double sign_m2 = m2 / abs(m2);
        double cc = closest.getCent()[1] - m1 * closest.getCent()[0];
        double cm = mouseY - m2 * mouseX;

        //<-- Better code for looking for absolute closest line -->
        // Case exception for a horizontal line
        double xi;  // X-Coord on polygon orthogonal to cursor
        double yi;  // Y-Coord on polygon orthogonal to cursor
        double dely = 0;
        double extrudeLength = faceLength / 2.0;

        if (m2 == 0) {
            xi = closest.getCent()[0];
            yi = mouseY;
        } else if (m1 == 0) {
            xi = mouseX;
            yi = closest.getCent()[1];
        } else {
            xi = (cm - cc) / (m1 - m2);
            yi = m1 * xi + cc;
        }

        double next_x = 0;
        double next_y = 0;
        Polygon book = new Polygon();

        // Normalise the length
        next_x = xi + extrudeLength / GV.METER_2_MAP_RATIO * cos(atan(m1));
        next_y = yi + extrudeLength / GV.METER_2_MAP_RATIO * sin(atan(m1));

        Circle outline1 = new Circle();
        outline1.setCenterX(next_x);
        outline1.setCenterY(next_y);
        outline1.setRadius(0.4);
        outline1.setFill(Color.BLUEVIOLET);

        // Normalise the length
        next_x += area / extrudeLength / 2 / GV.METER_2_MAP_RATIO * cos(atan(m2));
        next_y += area / extrudeLength / 2 / GV.METER_2_MAP_RATIO * sin(atan(m2));
        if (!plane.contains(next_x, next_y)) {
            next_x -= 2 * area / extrudeLength / 2 / GV.METER_2_MAP_RATIO * cos(atan(m2));
            next_y -= 2 * area / extrudeLength / 2 / GV.METER_2_MAP_RATIO * sin(atan(m2));
        }

        Circle outline2 = new Circle();
        outline2.setCenterX(next_x);
        outline2.setCenterY(next_y);
        outline2.setRadius(0.4);
        outline2.setFill(Color.BLUEVIOLET);

        // Normalise the length
        next_x -= faceLength / GV.METER_2_MAP_RATIO * cos(atan(m1));
        next_y -= faceLength / GV.METER_2_MAP_RATIO * sin(atan(m1));
        if (!plane.contains(next_x, next_y)) {
            next_x += 2 * faceLength / GV.METER_2_MAP_RATIO * cos(atan(m1));
            next_y += 2 * faceLength / GV.METER_2_MAP_RATIO * sin(atan(m1));
        }

        Circle outline3 = new Circle();
        outline3.setCenterX(next_x);
        outline3.setCenterY(next_y);
        outline3.setRadius(0.4);
        outline3.setFill(Color.BLUEVIOLET);

        // Normalise the length
        next_x += sign_m1 * area / extrudeLength / 2 / GV.METER_2_MAP_RATIO * cos(atan(m2));
        next_y += sign_m1 * area / extrudeLength / 2 / GV.METER_2_MAP_RATIO * sin(atan(m2));
        System.out.println((next_y - yi) / (next_x - xi) + " " + m1);
        if (round((next_y - yi) / (next_x - xi) * 100) != round(m1 * 100)) {
            next_x -= sign_m1 * area / extrudeLength / GV.METER_2_MAP_RATIO * cos(atan(m2));
            next_y -= sign_m1 * area / extrudeLength / GV.METER_2_MAP_RATIO * sin(atan(m2));
            System.out.println((next_y - yi) / (next_x - xi) + " " + m1);
        }

        Circle outline4 = new Circle();
        outline4.setCenterX(next_x);
        outline4.setCenterY(next_y);
        outline4.setRadius(0.4);
        outline4.setFill(Color.BLUEVIOLET);

        Circle dot = new Circle();
        dot.setCenterX(xi);
        dot.setCenterY(yi);
        dot.setRadius(0.45);
        dot.setFill(Color.GREEN);

        // <-- INSERT CODE FOR DRAWING POLYGON -->
        // <-- INSERT CODE FOR POLYGON LISTENERS -->
        /*Line draw = new Line(closest.getX1(),
                closest.getY1(),
                closest.getX2(),
                closest.getY2());
        draw.setStroke(Color.RED);
        draw.setStrokeWidth(0.15);*/
        // Add all drawn components
        // draw.getTransforms().addAll(new Translate(delX, delY));
        dot.getTransforms().addAll(new Translate(delX, delY));
        outline1.getTransforms().addAll(new Translate(delX, delY));
        outline2.getTransforms().addAll(new Translate(delX, delY));
        outline3.getTransforms().addAll(new Translate(delX, delY));
        outline4.getTransforms().addAll(new Translate(delX, delY));
        canvas.getChildren().addAll(dot, outline1, outline2, outline3, outline4);
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
        for (Polygon polygon : polygons) {
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
}
