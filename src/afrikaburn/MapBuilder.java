package afrikaburn;

import java.io.File;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
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
public class MapBuilder {

    final Polygon[] polygons;
    private double yMin;
    private double yMax;
    private double xMin;
    private double xMax;
    private final int totalPolygons;

    /**
     * @Description: Reads the map from the file 
     * (name must be afrikaburnmap.json) and loads the coordinates.
     */
    MapBuilder() {
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
            current.setOnMouseEntered((MouseEvent mouseEvent) -> {
                current.setFill(Color.BLUE);
            });
            current.setOnMouseExited((MouseEvent mouseEvent) -> {
                current.setFill(Color.LIGHTGREY);
            });
            canvas.getChildren().add(current);
        }
        
        // The origin is in the top left hand corner, changes it to bottom left
        canvas.getTransforms().add(new Rotate(180, GV.MAP_WIDTH / 2, 
                GV.MAP_HEIGHT / 2));
        return canvas;
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
        // Normalise the polygons
        for (int count = 0; count < totalPolygons; count++) {
            ObservableList<Double> tmpPolygon = polygons[count].getPoints();
            for (int coords = 0; coords < tmpPolygon.size(); coords += 2) {
                tmpPolygon.set(coords, (tmpPolygon.get(coords) - xMin)
                        / Math.abs(xMax - xMin) * GV.MAP_WIDTH);
                tmpPolygon.set(coords + 1, (tmpPolygon.get(coords + 1) - yMin)
                        / Math.abs(yMax - yMin) * GV.MAP_HEIGHT);
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
        System.out.println(bound.getPoints());
        for (int data = 0; data < bound.getPoints().size() - 5; data += 2) {
            area -= (bound.getPoints().get(data) * bound.getPoints()
                    .get(data + 3));
            area += (bound.getPoints().get(data + 1) * bound.getPoints()
                    .get(data + 2));
        }
        area /= 2;
        return Math.abs(area);
    }
}
