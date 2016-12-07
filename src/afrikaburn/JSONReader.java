package afrikaburn;

import javafx.scene.shape.Polygon;
import java.io.File;
import java.io.FileNotFoundException;
import static java.lang.Double.NaN;
import static java.lang.Math.cos;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
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
        boolean foundNext = false;
        polygons = new Polygon[totalPolygons];
        try {
            try (Scanner input = new Scanner(map)) {
                while (input.hasNext() && totalPolygons != currentPolygon) {
                    String token = input.next();
                    if (token.toLowerCase().contains("polygon")) {
                        foundNext = true;
                        currentPolygon++;
                        polygons[currentPolygon] = new Polygon();
                    } else if (token.toLowerCase().contains("properties")) {
                        foundNext = false;
                    } else if (Pattern.matches(decimalPattern,
                            token.replace(",", ""))
                            && !token.replace(",", "").equals("0")
                            && !token.replace(",", "").equals("0.0")
                            && foundNext) {

                        token = token.replace(",", "");

                        /*
                        This method of mapping does not work.. rather using
                        the 1:1 approximation when dealing with small areas 
                        that x = longitude and y = latitude
                         
                        x = (longitude+180)*(mapWidth/360)

                        // convert from degrees to radians
                        latRad = latitude*PI/180;

                        // get y value
                        mercN = log(tan((PI/4)+(latRad/2)));
                        y     = (mapHeight/2)-(mapWidth*mercN/(2*PI));
                         */
                        double lat = Double.parseDouble(token);
                        token = input.next().replace(",", "");
                        double lon = Double.parseDouble(token);
                        double x = (lon + 180)*200/360;
                        double mercN = log(tan((Math.PI/4) + (toRadians(lat)/2)));
                        
                        double y = (100/2) - (200*mercN/(Math.PI*2));
                        polygons[currentPolygon].getPoints().addAll(-y, -x);
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
        System.out.println(entries);
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
