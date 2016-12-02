package afrikaburn;

/**
 *
 * @author User
 */
public class Booking {
    private String name;
    private double size;
    private boolean noiseSensitivite;
    private boolean eroticSensitive;

    public Booking(String name, double size, boolean noiseSensitivite, boolean eroticSensitive) {
        this.name = name;
        this.size = size;
        this.noiseSensitivite = noiseSensitivite;
        this.eroticSensitive = eroticSensitive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
