package uk.bham.STORMSim;

/**
 * A class that models a Gaussian noise Point Spread Function (PSF)
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class GaussianPSFModel implements PSFModel {

    private double standardDeviation;

    /**
     * Constructor for the GaussianNoisePSFModel. Takes standardDeviation as its single parameter - this varies per microscope
     *
     * @param variance standardDeviation that described the Gaussian function
     */
    public GaussianPSFModel(double variance) {
        this.standardDeviation = variance;
    }

    /**
     * Samples from a Gaussian distribution, described by standardDeviation and a set of coordinates, given a set of x,
     * y coordinates
     *
     * @param fluorophorePosition the coordinate of the molecule in question
     * @param point            the point that is being looked at
     * @return a double that represents the intensity of the noise at the input coordinate
     */
    @Override
    public double getIntensityFactor(Coordinate fluorophorePosition, Coordinate point) {
        double variance = standardDeviation * standardDeviation;
        double xBracket = (point.getX() - fluorophorePosition.getX()) * (point.getX() - fluorophorePosition.getX());
        double yBracket = (point.getY() - fluorophorePosition.getY()) * (point.getY() - fluorophorePosition.getY());
        double toExp = (xBracket + yBracket) / (2 * variance);
        return Math.exp(-(toExp));
    }

    public double getSize() {
        return standardDeviation;
    }
}

