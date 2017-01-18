/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afrikaburn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.round;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 *
 * @author User
 */
public class SVGWriter {

    public SVGWriter(Polygon[] mapPolygons, ArrayList<Booking> bookings, double width, double height) {
        File output = new File("Resources/outputSVG.svg");
        try (FileWriter fw = new FileWriter(output)) {
            fw.write("<svg width=\"" + round(width) + "\" height=\"" + round(height) + "\">\n");
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

                String tmpBuild = "";
                for (int i = 0; i < mapPolygon.getPoints().size(); i += 2) {
                    tmpBuild += "" + round(mapPolygon.getPoints().get(i)) + "," + round(mapPolygon.getPoints().get(i + 1)) + " ";
                }
                fw.write("<polygon points=\"" + tmpBuild + "\" "
                        + "style=\"fill:" + fill_hex + ";"
                        + "stroke:" + stroke_hex + ";"
                        + "stroke-width:" + mapPolygon.getStrokeWidth() + "\" />\n");
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

                String tmpBuild = "";
                for (int i = 0; i < booking.getArea().getPoints().size(); i += 2) {
                    tmpBuild += "" + round(booking.getArea().getPoints().get(i)) + "," + round(booking.getArea().getPoints().get(i + 1)) + " ";
                }
                fw.write("<polygon points=\"" + tmpBuild + "\" "
                        + "style=\"fill:" + fill_hex + ";"
                        + "stroke:" + stroke_hex + ";"
                        + "stroke-width:" + booking.getArea().getStrokeWidth() + "\" />\n");
            }

            fw.write("</svg>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
