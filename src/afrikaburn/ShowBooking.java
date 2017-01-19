package afrikaburn;

/**
 * Child of booking: adds a front area
 * @author User
 */
public class ShowBooking extends Booking {

    private double front;

    public ShowBooking(int id, String name, double front, double size, boolean isNoisy, boolean isSexy, String points, String colour) {
        super(id, name, size, isNoisy, isSexy, points, colour);
        this.front = front;
    }

    public double front() {
        return front;
    }

    public void setFront(double front) {
        this.front = front;
    }

    @Override
    public String toString() {
        return super.getId() + ",booking" + "," + super.getName() + "," + front + "," + super.getSize() + "," + super.isNoisy() + "," + super.isSexy();
    }
}
