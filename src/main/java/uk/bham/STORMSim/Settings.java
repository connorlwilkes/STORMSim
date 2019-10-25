package uk.bham.STORMSim;

/**
 * A class that encapsulates the parameters entered by the user of the plug in
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class Settings {

    private int[] nmDimensions;
    private Fluorophore fluorophore;
    private int numberOfMolecules;
    private int numberOfFrames;
    private int[] pixelDimensionsTotal;
    private int[] pixelDimensionsFrame;
    private double noiseStdDev;
    private PSFModel PSFModel;
    private PhotoswitchingModel photoswitchingModel;
    private double tagChance;

    public Settings(int nmHeight, int nmWidth, int numberOfMolecules, int numberOfFrames, double PSFStandardDeviation,
                    double noiseValue, PhotoswitchingModel photoswitchingModel, Fluorophore fluorophore, double tagChance) {
        this.numberOfMolecules = numberOfMolecules;
        this.numberOfFrames = numberOfFrames;
        pixelDimensionsTotal = new int[]{400, 400, numberOfFrames};
        pixelDimensionsFrame = new int[]{400, 400};
        nmDimensions = new int[]{nmHeight, nmWidth};
        PSFModel = new GaussianPSFModel(PSFStandardDeviation);
        this.fluorophore = fluorophore;
        this.noiseStdDev = noiseValue;
        this.photoswitchingModel = photoswitchingModel;
        this.tagChance = tagChance;
    }

    public Settings(int eyepiece, int objective, int fieldSize, int numberOfFrames, int numberOfMolecules, int noiseValue,
                    int imageHeight, int imageWidth, PSFModel PSFModel) {
        this.numberOfFrames = numberOfFrames;
        this.numberOfMolecules = numberOfMolecules;
        this.noiseStdDev = noiseStdDev;
        pixelDimensionsTotal = new int[]{imageHeight, imageWidth, numberOfFrames};
        pixelDimensionsFrame = new int[]{imageHeight, imageWidth};
        this.PSFModel = PSFModel;
    }

    public int[] getNmDimensions() {
        return nmDimensions;
    }

    public Fluorophore getFluorophore() {
        return fluorophore;
    }

    public int getNumberOfMolecules() {
        return numberOfMolecules;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public int[] getPixelDimensionsTotal() {
        return pixelDimensionsTotal;
    }

    public int[] getPixelDimensionsFrame() {
        return pixelDimensionsFrame;
    }

    public double getNoiseStdDev() {
        return noiseStdDev;
    }

    public PSFModel getPSFModel() {
        return PSFModel;
    }

    public PhotoswitchingModel getPhotoswitchingModel() {
        return photoswitchingModel;
    }

    public double getTagChance() {
        return tagChance;
    }
}
