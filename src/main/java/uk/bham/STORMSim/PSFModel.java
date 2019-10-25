package uk.bham.STORMSim;

/**
 * An interface describing a point spread function
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public interface PSFModel {

    /**
     * Gets the intensity of a pixel at a location as a function of the PSF given the fluorophore position
     *
     * @param fluorophorePosition the position of the fluorophore
     * @param point               the point at which the evaluate the PSF
     * @return the intensity factor between 0 and 1
     */
    double getIntensityFactor(Coordinate fluorophorePosition, Coordinate point);

    double getSize();
}
