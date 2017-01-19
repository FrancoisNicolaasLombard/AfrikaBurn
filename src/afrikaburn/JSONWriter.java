/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afrikaburn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * This class just parses the shapes into a JSON file
 * @author User
 */
public class JSONWriter {

    public JSONWriter(Polygon[] mapPolygons, ArrayList<Booking> bookings) {
        try {
            File output = new File("Resources/outputJSON.json");
            try (FileWriter fw = new FileWriter(output)) {
                fw.write("{\n"
                        + "    \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n");
                for (Polygon mapPolygon : mapPolygons) {
                    Color fill = (Color) mapPolygon.getFill();
                    String fill_hex = String.format("#%02X%02X%02X",
                            (int) (fill.getRed() * 255),
                            (int) (fill.getGreen() * 255),
                            (int) (fill.getBlue() * 255));

                    Color stroke = (Color) mapPolygon.getStroke();
                    String stroke_hex = String.format("#%02X%02X%02X",
                            (int) (stroke.getRed() * 255),
                            (int) (stroke.getGreen() * 255),
                            (int) (stroke.getBlue() * 255));

                    fw.write("        {\n"
                            + "            \"type\": \"Feature\",\n"
                            + "            \"geometry\": {\n"
                            + "                \"type\": \"Polygon\",\n"
                            + "                \"coordinates\": [\n"
                            + "                    [\n");
                    for (int i = 0; i < mapPolygon.getPoints().size(); i += 2) {
                        if (i != 0) {
                            fw.write(",\n");
                        }
                        fw.write("                         [\n"
                                + "                            " + mapPolygon.getPoints().get(i) + ",\n"
                                + "                            " + -1.0 * mapPolygon.getPoints().get(i + 1) + ",\n"
                                + "                            0\n"
                                + "                        ]");
                    }
                    fw.write("\n]\n"
                            + "                ]\n"
                            + "            },\n"
                            + "            \"properties\": {\n"
                            + "                \"label\": \"0\",\n"
                            + "                \"styleUrl\": \"#PolyStyle00\",\n"
                            + "                \"styleHash\": \"-2c7e1fd5\",\n"
                            + "                \"stroke\": \"" + stroke_hex + "\",\n"
                            + "                \"stroke-opacity\": 1,\n"
                            + "                \"stroke-width\": " + mapPolygon.getStrokeWidth() + ",\n"
                            + "                \"fill\": \"" + fill_hex + "\",\n"
                            + "                \"fill-opacity\": 1\n"
                            + "            },\n"
                            + "            \"id\": \"ID_00000\"\n"
                            + "        }");
                    fw.write(",\n");
                }
                for (Booking booking : bookings) {
                    Color fill = (Color) booking.getArea().getFill();
                    String fill_hex = String.format("#%02X%02X%02X",
                            (int) (fill.getRed() * 255),
                            (int) (fill.getGreen() * 255),
                            (int) (fill.getBlue() * 255));

                    Color stroke = (Color) booking.getArea().getStroke();
                    String stroke_hex = String.format("#%02X%02X%02X",
                            (int) (stroke.getRed() * 255),
                            (int) (stroke.getGreen() * 255),
                            (int) (stroke.getBlue() * 255));

                    fw.write("        {\n"
                            + "            \"type\": \"Feature\",\n"
                            + "            \"geometry\": {\n"
                            + "                \"type\": \"Polygon\",\n"
                            + "                \"coordinates\": [\n"
                            + "                    [\n");
                    for (int i = 0; i < booking.getArea().getPoints().size(); i += 2) {
                        if (i != 0) {
                            fw.write(",\n");
                        }
                        fw.write("                         [\n"
                                + "                            " + booking.getArea().getPoints().get(i) + ",\n"
                                + "                            " + -1.0 * booking.getArea().getPoints().get(i + 1) + ",\n"
                                + "                            0\n"
                                + "                        ]");
                    }
                    fw.write("\n]\n"
                            + "                ]\n"
                            + "            },\n"
                            + "            \"properties\": {\n"
                            + "                \"label\": \"0\",\n"
                            + "                \"styleUrl\": \"#PolyStyle00\",\n"
                            + "                \"styleHash\": \"-2c7e1fd5\",\n"
                            + "                \"stroke\": \"" + stroke_hex + "\",\n"
                            + "                \"stroke-opacity\": 1,\n"
                            + "                \"stroke-width\": " + booking.getArea().getStrokeWidth() + ",\n"
                            + "                \"fill\": \"" + fill_hex + "\",\n"
                            + "                \"fill-opacity\": 1\n"
                            + "            },\n"
                            + "            \"id\": \"ID_00000\"\n"
                            + "        }");
                    if (!booking.getArea().equals(bookings.get(bookings.size() - 1).getArea())) {
                        fw.write(",\n");
                    }
                }
                fw.write("\n"
                        + "    ]\n"
                        + "}");
                fw.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
