package uk.bham.STORMSim;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * A class describing an individual frame
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class Frame {

    private GroundTruth groundTruth;
    private ArrayImg<UnsignedByteType, ?> image;
    private int[] dimensions;

    public Frame(GroundTruth groundTruth, Settings settings) {
        this.groundTruth = groundTruth;
        this.dimensions = settings.getPixelDimensionsFrame();
        this.image = new ArrayImgFactory<>(new UnsignedByteType()).create(dimensions);
    }

    /**
     * Converts a measurement from nanometers to pixels
     *
     * @param nanometer         nm to convert
     * @param heightOrWidthOfGT the dimension (height or width) of the ground truth (in nm) model
     * @param size              the dimension (height or width) of the image (in pixels)
     * @return converted number
     */
    public static double convertFromNmToPixel(double nanometer, double heightOrWidthOfGT, int size) {
        return nanometer * size / heightOrWidthOfGT;
    }

    /**
     * Converts a measurements from pixels to nanometers
     *
     * @param pixel             pixel to convert
     * @param heightOrWidthOfGT the dimension (height or width) of the ground truth (in nm) model
     * @param size              the dimension (height or width) of the image (in pixels)
     * @return converted number
     */
    public static double convertFromPixelToNm(double pixel, double heightOrWidthOfGT, int size) {
        return pixel * heightOrWidthOfGT / size;
    }

    /**
     * Converts an intensity value to a pixel value between 0 and 255. This is dependant on the max intensity of the
     * frame which is defined in the Settings class
     *
     * @param intensity the intensity
     * @param value     the value to convert
     * @return the pixel value
     */
    public static double convertFromIntensityToPixelValue(double intensity, double value) {
        if (value > intensity) {
            return 255;
        } else {
            return (value / intensity) * 255;
        }
    }

    public ArrayImg<UnsignedByteType, ?> getImage() {
        return image;
    }

    public GroundTruth getGroundTruth() {
        return groundTruth;
    }

    public int[] getDimensions() {
        return dimensions;
    }

    /**
     * Normalises a number such that it does not extend past the size of the frame
     *
     * @param i      long to normalise
     * @param height whether the normalisation is based on the height of the frame (if false width is used)
     * @return the normalised number
     */
    public long normalise(long i, boolean height) {
        if (i < 0) {
            return 0;
        } else {
            if (height) {
                if (i > getDimensions()[0]) {
                    return getDimensions()[0];
                } else {
                    return i;
                }
            } else {
                if (i > getDimensions()[1]) {
                    return getDimensions()[1];
                } else {
                    return i;
                }
            }
        }
    }
}
