package uk.bham.STORMSim;

/**
 * A class that encapsulates a markov model for photoswitching
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class MarkovPhotoswitchingModel implements PhotoswitchingModel {

    private double onChance;
    private double offChance;

    public MarkovPhotoswitchingModel(double onChance, double offChance) {
        this.onChance = onChance;
        this.offChance = offChance;
    }

    @Override
    public double getOnChance() {
        return onChance;
    }

    @Override
    public double getOffChance() {
        return offChance;
    }

    @Override
    public double getStayOnChance() {
        return onChance;
    }
}
