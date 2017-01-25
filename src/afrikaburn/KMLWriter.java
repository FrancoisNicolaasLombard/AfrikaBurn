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
 * This class parses the shapes into a KML file
 *
 * @author User
 */
public class KMLWriter {

    public KMLWriter(Polygon[] mapPolygons, ArrayList<Booking> bookings) {
        File output = new File("Resources/outputKML.kml");
        int mapPolygonCntr = 1;
        try (FileWriter fw = new FileWriter(output)) {
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                    + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\r\n"
                    + "<Document>\r\n"
                    + "\t<name>AfrikaBurn Map</name>\r\n");
            for (Polygon mapPolygon : mapPolygons) {
                // KML uses reverse order: ABGR
                Color fill = (Color) mapPolygon.getFill();
                String fill_hex = String.format("#dd%02X%02X%02X",
                        (int) (fill.getBlue() * 255),
                        (int) (fill.getGreen() * 255),
                        (int) (fill.getRed() * 255));

                Color stroke = (Color) mapPolygon.getStroke();
                String stroke_hex = String.format("#dd%02X%02X%02X",
                        (int) (stroke.getBlue() * 255),
                        (int) (stroke.getGreen() * 255),
                        (int) (stroke.getRed() * 255));

                String tmpBuild = "";
                for (int i = 0; i < mapPolygon.getPoints().size(); i += 2) {
                    tmpBuild += "\t\t\t\t\t" + mapPolygon.getPoints().get(i) + "," + -1.0 * mapPolygon.getPoints().get(i + 1) + ",0 \r\n";
                }

                fw.write("<Placemark>\r\n"
                        + "\t<name>Map Polygon " + (mapPolygonCntr++) + "</name>\r\n"
                        + "\t<Style>\r\n"
                        + "\t\t<LineStyle>\r\n"
                        + "\t\t\t<color>" + stroke_hex + "</color>\r\n"
                        + "\t\t\t<width>" + mapPolygon.getStrokeWidth() + "</width>\r\n"
                        + "\t\t</LineStyle>\r\n"
                        + "\t\t<PolyStyle>\r\n"
                        + "\t\t\t<color>" + fill_hex + "</color>\r\n"
                        + "\t\t</PolyStyle>\r\n"
                        + "\t</Style>\r\n"
                        + "\t<Polygon>\r\n"
                        + "\t\t<altitudeMode>clampToGround</altitudeMode>\r\n"
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
                if (booking.isPlaced()) {
                    Color fill = (Color) booking.getArea().getFill();
                    String fill_hex = String.format("#ff%02X%02X%02X",
                            (int) (fill.getBlue() * 255),
                            (int) (fill.getGreen() * 255),
                            (int) (fill.getRed() * 255));

                    Color stroke = (Color) booking.getArea().getStroke();
                    String stroke_hex = String.format("#ff%02X%02X%02X",
                            (int) (stroke.getBlue() * 255),
                            (int) (stroke.getGreen() * 255),
                            (int) (stroke.getRed() * 255));

                    String tmpBuild = "";
                    for (int i = 0; i < booking.getArea().getPoints().size(); i += 2) {
                        tmpBuild += "\t\t\t\t\t" + booking.getArea().getPoints().get(i) + "," + -1.0 * booking.getArea().getPoints().get(i + 1) + ",0 \r\n";
                    }

                    fw.write("<Placemark>\r\n"
                            + "\t<name>" + booking.getName() + "</name>\r\n"
                            + "\t<description>Size in squared meter: " + booking.getSize()
                            + "\r\n" + (booking.isSexy() ? "This camp contains explicit content" : "This camp is family safe")
                            + "\r\n" + (booking.isNoisy() ? "This camp is noisy" : "This camp is not noisy") + "</description>\r\n"
                            + "\t<Style>\r\n"
                            + "\t\t<LineStyle>\r\n"
                            + "\t\t\t<color>" + stroke_hex + "</color>\r\n"
                            + "\t\t\t<width>" + booking.getArea().getStrokeWidth() + "</width>\r\n"
                            + "\t\t</LineStyle>\r\n"
                            + "\t\t<PolyStyle>\r\n"
                            + "\t\t\t<color>" + fill_hex + "</color>\r\n"
                            + "\t\t</PolyStyle>\r\n"
                            + "\t</Style>\r\n"
                            + "\t<Polygon>\r\n"
                            + "\t\t<altitudeMode>clampToGround</altitudeMode>\r\n"
                            + "\t\t<outerBoundaryIs>\r\n"
                            + "\t\t\t<LinearRing>\r\n"
                            + "\t\t\t\t<coordinates>\r\n"
                            + tmpBuild
                            + "\t\t\t\t</coordinates>\r\n"
                            + "\t\t\t</LinearRing>\r\n"
                            + "\t\t</outerBoundaryIs>\r\n"
                            + "\t</Polygon>\r\n"
                            + "</Placemark>\r\n\r\n");

                    fw.write("<Placemark>\r\n"
                            + "\t<name>" + booking.getName() + "</name>\r\n"
                            + "\t<Style>\r\n"
                            + "\t\t<LabelStyle>\r\n"
                            + "\t\t\t<scale>0.85</scale>\r\n"
                            + "\t\t\t<color>#ffffffff</color>\r\n"
                            + "\t\t</LabelStyle>\r\n"
                            + "\t\t<IconStyle>\r\n"
                            + "\t\t\t<Icon></Icon>\r\n"
                            + "\t\t</IconStyle>\r\n"
                            + "\t</Style>\r\n"
                            + "\t<Point>\r\n"
                            + "\t\t<coordinates>" + booking.getText().getX() + "," + -1.0 * booking.getText().getY() + "</coordinates>\r\n"
                            + "\t</Point>"
                            + "</Placemark>\r\n\r\n");
                }
            }

            fw.write("</Document>\r\n"
                    + "</kml>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
