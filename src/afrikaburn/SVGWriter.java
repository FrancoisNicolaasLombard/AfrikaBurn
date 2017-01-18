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
        File output = new File("Resources/outputKML.kml");
        try (FileWriter fw = new FileWriter(output)) {
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                    + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\r\n"
                    + "<Document>\r\n"
                    + "\t<name>pm1.kml</name>\r\n");
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
                    tmpBuild += "\t\t\t\t\t" + mapPolygon.getPoints().get(i) + "," + -1.0 * mapPolygon.getPoints().get(i + 1) + ",0 \r\n";
                }

                fw.write("<Placemark>\r\n"
                        + "\t<Polygon>\r\n"
                        + "\t\t<altitudeMode>relativeToGround</altitudeMode>\r\n"
                        + "\t\t<outerBoundaryIs>\r\n"
                        + "\t\t\t<LinearRing>\r\n"
                        + "\t\t\t\t<coordinates>\r\n"
                        + tmpBuild
                        + "\t\t\t\t</coordinates>\r\n"
                        + "\t\t\t</LinearRing>\r\n"
                        + "\t\t</outerBoundaryIs>\r\n"
                        + "\t</Polygon>\r\n"
                        + "</Placemark>\r\n\r\n");
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
                    tmpBuild += "\t\t\t\t\t" + booking.getArea().getPoints().get(i) + "," + -1.0 * booking.getArea().getPoints().get(i + 1) + ",0 \r\n";
                }

                fw.write("<Placemark>\r\n"
                        + "\t<Polygon>\r\n"
                        + "\t\t<altitudeMode>relativeToGround</altitudeMode>\r\n"
                        + "\t\t<outerBoundaryIs>\r\n"
                        + "\t\t\t<LinearRing>\r\n"
                        + "\t\t\t\t<coordinates>\r\n"
                        + tmpBuild
                        + "\t\t\t\t</coordinates>\r\n"
                        + "\t\t\t</LinearRing>\r\n"
                        + "\t\t</outerBoundaryIs>\r\n"
                        + "\t</Polygon>\r\n"
                        + "</Placemark>\r\n\r\n");
            }

            fw.write("</Document>\r\n"
                    + "</kml>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
