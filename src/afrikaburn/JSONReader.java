package afrikaburn;

import javafx.scene.shape.Polygon;
import java.io.File;
import java.io.FileNotFoundException;
import static java.lang.Double.NaN;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author FN Lombard
 * @company VASTech
 * @description This class reads in a map in JSON formal and assigns polygons
 * for each piece of land.
 */
public class JSONReader {

    // Class variables
    private final File map;
    private final String decimalPattern = "([0-9]*)\\.([0-9]*)";
    private int totalPolygons;
    private Polygon[] polygons;

    JSONReader(File map) {
        this.map = map;
    }

    /**
     * Builds all of the polygons with the JSON file
     *
     * @return
     */
    public Polygon[] polygons() {
        countPolygons();
        int currentPolygon = -1;
        int points = 0;
        polygons = new Polygon[totalPolygons];
        try {
            try (Scanner input = new Scanner(map)) {
                while (input.hasNext() && totalPolygons != currentPolygon) {
                    String token = input.next();
                    if (token.contains("Polygon")) {
                        currentPolygon++;
                        polygons[currentPolygon] = new Polygon();
                        System.out.println(points);
                        points = 0;
                    } else if (Pattern.matches(decimalPattern,
                            token.replace(",", ""))
                            && !token.replace(",", "").equals("0")
                            && !token.replace(",", "").equals("0.0")) {
                        token = token.replace(",", "");

                        /*
                        This method of mapping does not work.. rather using
                        the 1:1 approximation when dealing with small areas 
                        that x = longitude and y = latitude
                         */
                        double lat = Double.parseDouble(token);
                        double lon = Double.parseDouble(input.next());
                        double x = cos(toRadians(lat)) * cos(toRadians(lon));
                        double y = cos(toRadians(lat)) * sin(toRadians(lon));
                        points++;
                        polygons[currentPolygon].getPoints().addAll(lon, lat);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONReader.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return polygons;
    }

    /**
     * Counts the total entries in the JSON file
     */
    private void countPolygons() {
        int entries = 0;
        try {
            try (Scanner input = new Scanner(map)) {
                while (input.hasNext()) {
                    if (input.next().contains("Polygon")) {
                        entries++;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONReader.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        totalPolygons = entries;
    }

    /**
     * This method returns the number of land
     *
     * @return
     */
    public int getTotalPolygons() {
        return totalPolygons;
    }

}
