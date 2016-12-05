package afrikaburn;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @Author: FN Lombard
 * @Company: VASTech
 * @Description:
 */
public class CSVReader {
    private Booking[] client;
    private int nrClients;

    CSVReader() {
        client = new Booking[nrClients()];
        try {
            try (Scanner input = new Scanner(new File("resources/campers.csv"))) {
                int count = 0;
                while (input.hasNextLine()) {
                    String[] elements = input.nextLine().split(",");
                    client[count++] = new Booking(elements[0],
                            Double.parseDouble(elements[1]),
                            Double.parseDouble(elements[2]),
                            elements[3].equalsIgnoreCase("yes"),
                            elements[4].equalsIgnoreCase("yes"));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CSVReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int nrClients(){
        nrClients = 0;
        try {
            try (Scanner input = new Scanner(new File("resources/campers.csv"))) {
                while(input.hasNextLine()){
                    input.nextLine();
                    nrClients++;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CSVReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nrClients;
    }
    
    public String[] getNames(){
        String[] names = new String[client.length];
        for (int x = 0; x < client.length; x++){
            names[x] = client[x].getName();
        }
        return names;
    }

    public Booking[] getClients() {
        return client;
    }
    
    public int getNrClients(){
        return nrClients;
    }
}
