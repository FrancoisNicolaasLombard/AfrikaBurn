package afrikaburn;

import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 *
 * @Author: FN Lombard
 * @Company: VASTech
 * @Description: This is the parent to any type of booking that can be made
 * @TODO: Can make a child that (for example) can have a different kind of
 * polygon algorithm.
 */
public class Booking {

    private String name;
    private double size;
    private final Polygon area;
    private final Text onDescription;
    private final String offDescrption;
    private int id;
    private boolean isNoisy;
    private boolean isSexy;
    private boolean isPlaced;
    private double height;
    private double front;
    private final double[] frontage;

    public Booking(int id, String name, double front, double size, boolean isNoisy, boolean isSexy, String points, String colour, String frontageString) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.isNoisy = isNoisy;
        this.isSexy = isSexy;
        this.front = front;

        frontage = new double[4];
        String substring = frontageString.substring(1, frontageString.length() - 1);
        for (int x = 0; x < 4; x++) {
            frontage[x] = Double.parseDouble(substring.split(";")[x]);
        }

        area = new Polygon();

        onDescription = new Text();
        onDescription.setText(name
                + "\n" + size + " m\u00B2"
                + "\n" + (isNoisy ? "Loud" : "Not Loud")
                + "\n" + (isSexy ? "Erotic" : "Safe"));

        onDescription.setFontSmoothingType(FontSmoothingType.LCD);
        onDescription.setStyle("-fx-font-size: 5; -fx-font-weight: normal; -fx-alignment:center;");
        onDescription.setTextAlignment(TextAlignment.CENTER);
        onDescription.setTextOrigin(VPos.CENTER);

        area.setStroke(Color.BLACK);
        area.setStrokeWidth(0.25);
        area.setFill(Paint.valueOf(colour));

        String newPoints = points.substring(1, points.length() - 1);
        String[] pointTokens = newPoints.split(";");
        for (int i = 0; i < pointTokens.length; i += 2) {
            area.getPoints().addAll(Double.parseDouble(pointTokens[i]), Double.parseDouble(pointTokens[i + 1]));
        }
        if (!points.equals("[-1.0; -1.0]")) {
            onDescription.setWrappingWidth((area.getLayoutBounds().getWidth()) / 2.0);
            onDescription.setX((area.getLayoutBounds().getMaxX() + area.getLayoutBounds().getMinX()) / 2.0 - onDescription.getLayoutBounds().getWidth() / 2.0);
            onDescription.setY((area.getLayoutBounds().getMaxY() + area.getLayoutBounds().getMinY()) / 2.0);
            isPlaced = true;
        } else {
            isPlaced = false;
        }

        height = size / front;
        offDescrption = "";
    }

    public void setFrontage(int index, double value) {
        frontage[index] = value;
    }

    public double[] getFrontage() {
        return frontage;
    }

    public double front() {
        return front;
    }

    public void setFront(double front) {
        this.front = front;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public void isPlaced(boolean isPlaced) {
        this.isPlaced = isPlaced;
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

    public void setArea(Polygon saved) {
        area.getPoints().setAll(saved.getPoints());
    }

    public Polygon getArea() {
        return area;
    }

    public Text getText() {
        return onDescription;
    }

    public void updateText(double[] coords) {
        onDescription.setX(coords[0] - 1);
        onDescription.setY(coords[1]);
    }

    public boolean isNoisy() {
        return isNoisy;
    }

    public void setNoisy(boolean isNoisy) {
        this.isNoisy = isNoisy;
    }

    public boolean isSexy() {
        return isSexy;
    }

    public void setSexy(boolean isSexy) {
        this.isSexy = isSexy;
    }

    public void clearShape() {
        area.getPoints().removeAll(area.getPoints());
        area.getPoints().addAll(-1.0, -1.0);
        onDescription.setX(-1);
        onDescription.setY(-1);
        isPlaced = false;
        setDescription();
    }

    public void setDescription() {
        onDescription.setText(name
                + "\n" + size + " m\u00B2"
                + "\n" + (isNoisy ? "Loud" : "Not Loud")
                + "\n" + (isSexy ? "Erotic" : "Safe"));
    }

    @Override
    public String toString() {
        return id + ",booking" + "," + name + "," + front + "," + size + "," + isNoisy + "," + isSexy;
    }
}
