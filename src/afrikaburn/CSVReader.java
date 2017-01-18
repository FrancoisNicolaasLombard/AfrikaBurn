package afrikaburn;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @Author: FN Lombard
 * @Company: VASTech
 * @Description: This class reads in the clients from a CSV file and creates
 * booking entries for each.
 */
public class CSVReader {

    private ArrayList<Booking> bookings;

    CSVReader(File file) {
        bookings = new ArrayList<>();
        try {
            try (Scanner input = new Scanner(file)) {
                int count = 0;
                while (input.hasNextLine()) {
                    String[] elements = input.nextLine().split(",");
                    if (elements[0].equalsIgnoreCase("booking")) {
                        if (!elements[6].equals("[]") || elements.length != 8) {
                            bookings.add(new ShowBooking(count++, elements[1],
                                    Double.parseDouble(elements[2]),
                                    Double.parseDouble(elements[3]),
                                    elements[4].equalsIgnoreCase("true"),
                                    elements[5].equalsIgnoreCase("true"),
                                    elements[6], elements[7]));
                        } else {
                            bookings.add(new ShowBooking(count++, elements[1],
                                    Double.parseDouble(elements[2]),
                                    Double.parseDouble(elements[3]),
                                    elements[4].equalsIgnoreCase("true"),
                                    elements[5].equalsIgnoreCase("true"),
                                    "[-1.0; -1.0]", "0xffffffff"));
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CSVReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<Booking> getClients() {
        return bookings;
    }
}
