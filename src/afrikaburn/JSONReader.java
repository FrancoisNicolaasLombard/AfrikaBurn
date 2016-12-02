package afrikaburn;

import javafx.scene.shape.Polygon;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author User Read dem files
 */
public class JSONReader {

    // Class variables
    private final File map;
    private final String decimalPattern = "([0-9]*)\\.([0-9]*)"; 
    private int totalPolygons;

    JSONReader(File map) {
        this.map = map;
    }

    /**
     *
     * @return
     */
    public Polygon[] polygons() {
        countPolygons();
        int currentPolygon = -1;
        Polygon[] polygons = new Polygon[totalPolygons];
        try {
            try (Scanner input = new Scanner(map)) {
                while (input.hasNext() && totalPolygons != currentPolygon) {
                    String token = input.next();
                    if (token.contains("Polygon")) {
                        currentPolygon++;
                        polygons[currentPolygon] = new Polygon();
                    }else if(Pattern.matches(decimalPattern, token.replace(",", "")) && !token.replace(",", "").equals("0") && !token.replace(",", "").equals("0.0")){
                        token = token.replace(",", "");
                        double lat = Double.parseDouble(token);
                        double lon = Double.parseDouble(input.next());
                        polygons[currentPolygon].getPoints().addAll(lon, lat);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return polygons;
    }
    
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
            Logger.getLogger(JSONReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        totalPolygons = entries;
    }

    public int getTotalPolygons() {
        return totalPolygons;
    }
}