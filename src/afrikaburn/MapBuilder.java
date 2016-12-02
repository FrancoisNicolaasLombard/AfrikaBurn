package afrikaburn;

import java.io.File;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
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
    private double latMin;
    private double latMax;
    private double lonMin;
    private double lonMax;
    private final int totalPolygons;

    /**
     * 
     */
    MapBuilder() {
        JSONReader reader = new JSONReader(new File("resources/afrikaburnmap.json"));
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
            current.getTransforms().add(new Rotate(-90, GV.MAP_WIDTH / 2, GV.MAP_HEIGHT / 2));
            current.setFill(Color.LIGHTGREY);
            current.setStroke(Color.BLACK);
            current.setStrokeWidth(1);
            current.setOnMouseEntered((MouseEvent mouseEvent) -> {
                System.out.println(area(current));
                current.setFill(Color.BLUE);
            });
            current.setOnMouseExited((MouseEvent mouseEvent) -> {
                current.setFill(Color.LIGHTGREY);
            });
            canvas.getChildren().add(current);
        }
        return canvas;
    }

    /**
     * @Author FN Lombard
     * @Company: VASTech
     * @Description:
     */
    private void minMax() {
        lonMin = polygons[0].getPoints().get(0);
        latMin = polygons[0].getPoints().get(1);
        lonMax = lonMin;
        latMax = latMin;
        System.out.println("Total Polygons: " + totalPolygons);
        for (int count = 0; count < totalPolygons; count++) {
            for (int coords = 0; coords < polygons[count].getPoints().size(); coords += 2) {
                if (polygons[count].getPoints().get(coords + 1) > latMax) {
                    latMax = polygons[count].getPoints().get(coords + 1);
                } else if (polygons[count].getPoints().get(coords + 1) < latMin) {
                    latMin = polygons[count].getPoints().get(coords + 1);
                }
                if (polygons[count].getPoints().get(coords) > lonMax) {
                    lonMax = polygons[count].getPoints().get(coords);
                } else if (polygons[count].getPoints().get(coords) < lonMin) {
                    lonMin = polygons[count].getPoints().get(coords);
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
                tmpPolygon.set(coords, (tmpPolygon.get(coords) - lonMin)
                        / Math.abs(lonMax - lonMin) * GV.MAP_HEIGHT);
                tmpPolygon.set(coords + 1, (tmpPolygon.get(coords + 1) - latMin)
                        / Math.abs(latMax - latMin) * GV.MAP_WIDTH);
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
            polygons[count].getTransforms().add(new Translate(-deltaY, deltaX));
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
    public double area(Polygon bound){
        double area = 0;
        System.out.println(bound.getPoints());
        for (int data = 0; data < bound.getPoints().size() - 2; data+=2){
            //area -= bound.getPoints().get(data) * bound.getPoints().get(data + );
           // area += bound.getPoints().get(data+1) * bound.getPoints().get(data + 2);
           // area /= 2;
        }
        System.out.println("awe");
        return Math.abs(area);
    }
}