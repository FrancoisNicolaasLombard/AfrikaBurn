package afrikaburn;

import java.io.File;
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
public class MapController {

    final Polygon[] polygons;
    private double yMin;
    private double yMax;
    private double xMin;
    private double xMax;
    private final int totalPolygons;
    private final Label infoLabel;

    /**
     * @Description: Reads the map from the file (name must be
     * afrikaburnmap.json) and loads the coordinates.
     */
    MapController(Label infoLabel) {
        this.infoLabel = infoLabel;
        JSONReader reader
                = new JSONReader(new File("resources/afrikaburnmap.json"));
        polygons = reader.polygons();
        totalPolygons = reader.getTotalPolygons();

        minMax();
        portMap();
    }

    /**
     * @return @Author FN Lombard
     * @Company: VASTech
     * @Description:
     */
    public Pane getGroup() {
        Pane canvas = new Pane();

        for (Polygon current : polygons) {
            current.setFill(Color.LIGHTGREY);
            current.setStroke(Color.BLACK);
            current.setStrokeWidth(1);
            setListeners(current);
            canvas.getChildren().add(current);
        }

        // The origin is in the top left hand corner, changes it to bottom left
        canvas.getTransforms().add(new Rotate(-90, GV.MAP_WIDTH / 2,
                GV.MAP_HEIGHT / 2));
        return canvas;
    }

    private void setListeners(Polygon current) {
        current.setOnMouseClicked(e -> {
            infoLabel.setText("Total Area: " + Math.round(area(current) * 100) / 100.0 + " m\u00B2");
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
     * @Description:
     */
    private void minMax() {
        xMin = polygons[0].getPoints().get(0);
        yMin = polygons[0].getPoints().get(1);
        xMax = xMin;
        yMax = yMin;
        System.out.println("Total Polygons: " + totalPolygons);
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
        for (int count = 0; count < totalPolygons; count++) {
            polygons[count].getTransforms().add(new Translate(deltaX, deltaY));
        }

    }

    /**
     *
     */
    public void zoomIn() {
        for (int count = 0; count < totalPolygons; count++) {
            polygons[count].setScaleX(polygons[count].getScaleX() + 0.05);
            polygons[count].setScaleY(polygons[count].getScaleY() + 0.05);
        }
    }

    /**
     *
     */
    public void zoomOut() {
        for (int count = 0; count < totalPolygons; count++) {
            polygons[count].setScaleX(polygons[count].getScaleX() - 0.05);
            polygons[count].setScaleY(polygons[count].getScaleY() - 0.05);
        }
    }

    /**
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
        area *= GV.METER2MAP_RATIO;
        return Math.abs(area);
    }

    /**
     * Find the line closest to the mouse.
     *
     * @param plane
     * @param area
     * @param mouseX
     * @param mouseY
     * @return
     */
    public Line bookedArea(Polygon plane, double area, double mouseX, double mouseY) {
        // Declare two points and use the first two as default
        GeoLine closest = new GeoLine(plane.getPoints().get(0), plane.getPoints().get(1),
                plane.getPoints().get(2), plane.getPoints().get(3));
        double shortest = closest.cent2Point(mouseX, mouseY);
        
        // Find the line closest to the mouse pointer
        for (int x = 2; x < plane.getPoints().size() - 2; x += 2) {
            GeoLine tmp = new GeoLine(plane.getPoints().get(x), plane.getPoints().get(x + 1),
                plane.getPoints().get(x + 2), plane.getPoints().get(x + 3));
            if (tmp.cent2Point(mouseX, mouseY) < shortest){
                closest = tmp;
                shortest = tmp.cent2Point(mouseX, mouseY);
            }
        }
        return new Line(closest.getX1(), closest.getY1(), closest.getX2(), closest.getY2());
    }
}
