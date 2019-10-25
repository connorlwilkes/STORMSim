package uk.bham.STORMSim;

/**
 * An interface describing a photoswitching model
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public interface PhotoswitchingModel {

    double getOnChance();

    double getOffChance();

    double getStayOnChance();
}
