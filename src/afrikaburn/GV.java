package afrikaburn;

import java.awt.Toolkit;

/**
 *
 * @author: FN Lombard
 * @Company: VASTech
 * @Description:
 * @Notes: The METER2MAP_RATIO variable should be changed each time a new map is
 * loaded. This was calculated by taking the ratio between the square meter area
 * of the polygons in GeoJSON.io and comparing it to the area of the polygons in
 * the AfrikaBurn Desktop Application.
 */
public class GV {

    public static final int MAP_HEIGHT = 5500;
    public static final int MAP_WIDTH = 5500;
    public static final double ZOOM_AMOUNT = 1.15;
    public static final double METER_SQUARED_2_MAP_RATIO = 7.198666276306972e-2;
    public static final double METER_2_MAP_RATIO = Math.sqrt(METER_SQUARED_2_MAP_RATIO);
    public static final double SCREEN_W = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static final double SCREEN_H = Toolkit.getDefaultToolkit().getScreenSize().height;
}
