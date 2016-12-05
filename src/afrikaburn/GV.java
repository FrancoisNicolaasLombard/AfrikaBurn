package afrikaburn;

/**
 *
 * @author User
 * @Description
 * @Notes: The METER2MAP_RATIO variable should be changed each time a new map
 * is loaded. This was calculated by taking the ratio between the square meter
 * area of the polygons in GeoJSON.io and comparing it to the area of the
 * polygons in the AfrikaBurn Desktop Application.
 */
public class GV {
    public static final int MAP_HEIGHT = 550;
    public static final int MAP_WIDTH = 550;
    public static final double ZOOM_AMOUNT = 1.15;
    public static final double METER2MAP_RATIO = 7.198633233;
}
