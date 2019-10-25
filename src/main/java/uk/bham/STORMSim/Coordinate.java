package uk.bham.STORMSim;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A class to encapsulate a coordinate system. Coordinates are assumed to be in nanometers
 *
 * @author Connor Wilkes
 * @version 05/07/2018
 */
public class Coordinate {

    private double x, y;

    /**
     * @param x
     * @param y
     */
    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isIn(double height, double width) {
        return (y <= height) && (x <= width);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(x).append(y).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Coordinate)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        Coordinate toCompare = (Coordinate) obj;
        return new EqualsBuilder().append(x, toCompare.getX()).append(y, toCompare.getY()).isEquals();
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
