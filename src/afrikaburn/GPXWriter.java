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

/**
 *
 * @author: FN Lombard
 * @Company: VASTech
 *
 * @Description: The class writes the layout to a gpx file which can be used in 
 * Garmin BaseCamp
 */
public class GPXWriter {

    public GPXWriter(ArrayList<Booking> bookings) {
        File output = new File("Resources/outputGPX.gpx");
        try (FileWriter fw = new FileWriter(output)) {
            fw.write("<?xml version=\"1.0\" standalone=\"yes\"?>\r\n"
                    + "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" "
                    + "creator=\"KML2GPX.COM\" "
                    + "version=\"1.1\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                    + "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 "
                    + "http://www.topografix.com/GPX/1/1/gpx.xsd\">\r\n"
                    + "\t<metadata>\r\n"
                    + "\t\t<name><![CDATA[AfrikaBurn Map]]></name>\r\n"
                    + "\t</metadata>\r\n\r\n");

            for (Booking booking : bookings) {
                if (booking.isPlaced()) {
                    fw.write("<wpt lat=\"" + (-1 * booking.getFrontage()[1]) + "\" lon=\"" + booking.getFrontage()[0] + "\">\r\n"
                            + "\t<ele>0</ele>\r\n"
                            + "\t<name><![CDATA[" + booking.getName() + "]]></name>\r\n"
                            + "\t<cmt><![CDATA[Side One]]></cmt>\r\n"
                            + "</wpt>\r\n\r\n");

                    fw.write("<wpt lat=\"" + (-1 * booking.getFrontage()[3]) + "\" lon=\"" + booking.getFrontage()[2] + "\">\r\n"
                            + "\t<ele>0</ele>\r\n"
                            + "\t<name><![CDATA[" + booking.getName() + "]]></name>\r\n"
                            + "\t<cmt><![CDATA[Side Two]]></cmt>\r\n"
                            + "</wpt>\r\n\r\n");
                }
            }

            fw.write("</gpx>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(JSONWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
