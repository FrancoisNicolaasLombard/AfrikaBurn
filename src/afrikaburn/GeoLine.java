/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afrikaburn;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 *
 * @author User
 */
public class GeoLine {

    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    public GeoLine(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public double getLength() {
        return sqrt(pow(x1 - x2, 2) + pow(y2 - y1, 2));
    }
    
    public double[] getCent(){
        double[] tmp = new double[2];
        tmp[0] = (x1+x2)/2.0;
        tmp[1] = (y1+y2)/2.0;
        return tmp;
    }
    
    public double cent2Point(double x, double y){
        double[] center = getCent();
        return sqrt(pow(center[0] - x, 2) + pow(center[1] - y, 2));
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }
}
