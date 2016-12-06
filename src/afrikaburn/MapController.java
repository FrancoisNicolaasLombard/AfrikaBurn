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
                = new JSONReader(new File("resources/afrikaburnmap.json"));
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
            current.setStrokeWidth(0.4);
            setListeners(current);
            canvas.getChildren().add(current);
        }

        // The origin is in the top left hand corner, changes it to bottom left
        canvas.getTransforms().add(new Rotate(-90, GV.MAP_WIDTH / 2,
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
                + Math.round(closest.getLength() * GV.METER_2_MAP_RATIO * 100)
                / 100.0 + " m");

        // Find the closest point to the mouse pointer
        double m1 = gradient(closest);
        double m2 = -1 / m1;

        //<-- Better code for looking for absolute closest line -->
        // Case exception for a horizontal line
        double xi, yi;
        if (m2 == 0) {
            xi = closest.getCent()[0];
            yi = mouseY;
        } else if (m1 == 0) {
            xi = mouseX;
            yi = closest.getCent()[1];
        } else {
            double cc = closest.getCent()[1] - m1 * closest.getCent()[0];
            double cm = mouseY - m2 * mouseX;
            xi = (cm - cc) / (m1 - m2);
            yi = m1 * xi + cc;
        }

        Circle dot = new Circle();
        dot.setCenterX(xi);
        dot.setCenterY(yi);
        dot.setRadius(1.1);
        dot.setFill(Color.GREEN);

        // <-- INSERT CODE FOR DRAWING POLYGON -->
        // <-- INSERT CODE FOR POLYGON LISTENERS -->
        Line draw = new Line(closest.getX1(),
                closest.getY1(),
                closest.getX2(),
                closest.getY2());
        draw.setStroke(Color.RED);
        draw.setStrokeWidth(0.5);

        // Add all drawn components
        draw.getTransforms().addAll(new Translate(delX, delY));
        dot.getTransforms().addAll(new Translate(delX, delY));
        canvas.getChildren().addAll(draw, dot);
    }

    private double gradient(GeoLine line) {
        double dy = line.getY2() - line.getY1();
        double dx = line.getX2() - line.getX1();
        return dy / dx;
    }

    /**
     * Add resolution
     */
    public void increaseResolution() {
        for (Polygon polygon : polygons) {
            Polygon tmp = new Polygon();
            double x1, x2, y1, y2, length;
            int originalPoints = polygon.getPoints().size(), tracker = 0;

            for (int i = 0; i < originalPoints - 3; i += 2) {
                x1 = polygon.getPoints().get(i + tracker);
                y1 = polygon.getPoints().get(i + 1 + tracker);
                x2 = polygon.getPoints().get(i + 2 + tracker);
                y2 = polygon.getPoints().get(i + 3 + tracker);

                tmp.getPoints().addAll(x1, y1);

                // Add one point for each meter on the line - exlude first and last points.
                length = x2 - x1;
                double m = (y2 - y1) / length;
                double c = y2 - m * x2;
                int sign = (int) (length / abs(length));
                Circle dot = new Circle();
                dot.setCenterX(x1);
                dot.setCenterY(y1);
                dot.setRadius(1.1);
                dot.setFill(Color.RED);
                canvas.getChildren().add(dot);
                for (int j = 1; j < abs(length) - 1; j++) {
                    if (m == 0) {
                        tmp.getPoints().addAll(x1, y1 + j);
                    } else if (m == NaN) {
                        tmp.getPoints().addAll(j + x1, y1);
                    } else {
                        tmp.getPoints().addAll(j * sign + x1, m * (j * sign + x1) + c);

                        Circle dots = new Circle();
                        dots.setCenterX(j * sign + x1);
                        dots.setCenterY(m * (j * sign + x1) + c);
                        dots.setRadius(1.1);
                        dots.setFill(Color.AQUA);
                        canvas.getChildren().add(dots);
                    }
                }
            }
            //tmp.setStroke(Color.DARKGOLDENROD);
            // canvas.getChildren().add(tmp);
            polygon.getPoints().setAll(tmp.getPoints());
            polygon = tmp;
        }
    }
}
