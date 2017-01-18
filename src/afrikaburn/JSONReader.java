package afrikaburn;

import javafx.scene.shape.Polygon;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        if (map.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(map);
                String jsontxt = IOUtils.toString(is);
                JSONObject json = new JSONObject(jsontxt);
                JSONArray objects = json.getJSONArray("features");
                polygons = new Polygon[objects.length()];

                for (int i = 0; i < polygons.length; i++) {
                    polygons[i] = new Polygon();
                    JSONObject objectGeo = objects.getJSONObject(i).getJSONObject("geometry");
                    JSONObject objectProperties = objects.getJSONObject(i).getJSONObject("properties");

                    JSONArray objectCoords = objectGeo.getJSONArray("coordinates").getJSONArray(0);
                    try {
                        polygons[i].setStrokeWidth(objectProperties.getDouble("stroke-width"));
                    } catch (JSONException e) {
                        polygons[i].setStrokeWidth(1); // Default Value
                        System.out.println(e.getMessage());
                    }
                    try {
                        polygons[i].setFill(Paint.valueOf(objectProperties.getString("fill")));
                    } catch (JSONException e) {
                        polygons[i].setFill(Color.GRAY);
                        System.out.println(e.getMessage());
                    }

                    // Add the points to the polygon
                    for (int j = 0; j < objectCoords.length(); j++) {
                        double lat = objectCoords.getJSONArray(j).getDouble(0);
                        double lon = objectCoords.getJSONArray(j).getDouble(1);
                        // Ignore the latitude;
                        /* This method of mapping does not work.. rather using
                        the 1:1 approximation when dealing with small areas 
                        that x = longitude and y = latitude. */
                        polygons[i].getPoints().addAll(lat, -lon);
                    }
                }
                is.close();
            } catch (IOException | JSONException ex) {
                Logger.getLogger(JSONReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Map does not exist.");
        }
        return polygons;
    }
}
