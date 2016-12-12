package afrikaburn;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 *
 * @Author: FN Lombard
 * @Company: VASTech
 * @Description
 */
public class Booking {

    private String name;
    private double front;
    private double size;
    private boolean noiseSensitivite;
    private boolean eroticSensitive;
    private final Polygon area;
    private final int id;

    public Booking(int id, String name,
            double front,
            double size,
            boolean noiseSensitivite,
            boolean eroticSensitive) {
        this.id = id;
        this.name = name;
        this.front = front;
        this.size = size;
        this.noiseSensitivite = noiseSensitivite;
        this.eroticSensitive = eroticSensitive;
        area = new Polygon();
        
    }

    @Override
    public String toString() {
        return id + "," + name + "," + front + "," + size + "," + noiseSensitivite + "," + eroticSensitive;
    }

    /**
     * @Author @return @Description: Getter and Setter methods
     */
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFront() {
        return front;
    }

    public void setFront(double front) {
        this.front = front;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public boolean isNoiseSensitivite() {
        return noiseSensitivite;
    }

    public void setNoiseSensitivite(boolean noiseSensitivite) {
        this.noiseSensitivite = noiseSensitivite;
    }

    public boolean isEroticSensitive() {
        return eroticSensitive;
    }

    public void setEroticSensitive(boolean eroticSensitive) {
        this.eroticSensitive = eroticSensitive;
    }

    public void setArea(Polygon saved) {
        area.getPoints().setAll(saved.getPoints());
    }

    public Polygon getArea() {
        return area;
    }
}
