package uk.bham.STORMSim;

import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class describing a Fluorophore model
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class Fluorophore {

    private double size;
    private Coordinate molCoordinate;
    private Coordinate location;
    private State state;
    private PSFModel psfModel;
    private PhotoswitchingModel photoswitchingModel;
    private int switchingCycle;
    private boolean photoBleached;
    private int currentCycleNumber;
    private double intensity;

    public Fluorophore(double size, int switchingCycle, double intensity) {
        this.size = size;
        this.intensity = intensity;
        state = State.OFF;
        this.switchingCycle = switchingCycle;
    }

    public Fluorophore(Coordinate molCoordinate, PSFModel psfModel, PhotoswitchingModel photoswitchingModel, Fluorophore fluorophore) {
        state = State.OFF;
        this.molCoordinate = molCoordinate;
        this.psfModel = psfModel;
        this.photoswitchingModel = photoswitchingModel;
        switchingCycle = fluorophore.getSwitchingCycle();
        size = fluorophore.getSize();
        intensity = fluorophore.getIntensity();
        photoBleached = false;
        currentCycleNumber = 0;
    }

    public Fluorophore(Fluorophore fluorophore) {
        state = fluorophore.getState();
        molCoordinate = fluorophore.getMolCoordinate();
        psfModel = fluorophore.getPsfModel();
        size = fluorophore.getSize();
        intensity = fluorophore.getIntensity();
        photoBleached = false;
        currentCycleNumber = 0;
    }

    public State getState() {
        return state;
    }

    public PSFModel getPsfModel() {
        return psfModel;
    }

    public PhotoswitchingModel getPhotoswitchingModel() {
        return photoswitchingModel;
    }

    public int getSwitchingCycle() {
        return switchingCycle;
    }

    public boolean isPhotoBleached() {
        return photoBleached;
    }

    public double getSize() {
        return size;
    }

    public Coordinate getLocation() {
        return location;
    }

    public double getIntensity() {
        return intensity;
    }

    public Coordinate getMolCoordinate() {
        return molCoordinate;
    }

    /**
     * Generates a new location in coordinates for the fluorophore around its molecule dependant on its size
     */
    public void locationGen() {
        Random r = new Random();
        double x = r.nextGaussian() * size + molCoordinate.getX();
        double y = r.nextGaussian() * size + molCoordinate.getY();
        location = new Coordinate(x, y);
    }

    /**
     * Switches the state of the fluorophore between off, transition1, active, transition2 and off
     */
    public void switchState() {
        if (state == State.OFF) {
            state = State.TRANSITION1;
        } else if (state == State.TRANSITION1) {
            state = State.ACTIVE;
        } else if (state == State.ACTIVE) {
            state = State.TRANSITION2;
        } else if (state == State.TRANSITION2) {
            state = State.OFF;
            currentCycleNumber++;
            PoissonDistribution poissonDistribution = new PoissonDistribution(switchingCycle);
            double chance = poissonDistribution.cumulativeProbability(currentCycleNumber);
            double rand = ThreadLocalRandom.current().nextDouble(1);
            if (chance >= rand) {
                photoBleached = true;
            }
        }
    }

    public enum State {
        TRANSITION1, ACTIVE, TRANSITION2, OFF
    }
}
