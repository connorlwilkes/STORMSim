package uk.bham.STORMSim;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


/**
 * A class that represents an individual 'video' frame. Each frame contains a list of molecules that are displayed in it
 *
 * @author Connor Wilkes
 * @version 5/07/2018
 */
public class STORMFrame extends Frame {

    private List<Fluorophore> fluorophores;
    private int id;
    private double meanNoise;
    private PoissonDistribution noiseDistribution;
    private double frameIntensity;
    private Set<Coordinate> fluorophoreCoordinates;

    /**
     * Constructor for the STORMFrame class
     *
     * @param groundTruth the groundTruth associated with the STORMFrame
     * @param id          the id of the STORMFrame
     */
    public STORMFrame(GroundTruth groundTruth, int id, Settings settings, List<Fluorophore> fluorophores) {
        super(groundTruth, settings);
        this.fluorophores = fluorophores;
        meanNoise = settings.getNoiseStdDev();
        if (meanNoise > 0) {
            noiseDistribution = new PoissonDistribution(meanNoise);
        }
        this.id = id;
        this.frameIntensity = settings.getFluorophore().getIntensity();
        fluorophoreCoordinates = new HashSet<>();
    }

    private static double normaliseWithState(double number, Fluorophore.State state, double factor) {
        if (state == Fluorophore.State.ACTIVE) {
            return number;
        } else if (state == Fluorophore.State.TRANSITION1 || state == Fluorophore.State.TRANSITION2) {
            return Math.ceil(number * factor);
        } else {
            return 0;
        }
    }

    /**
     * Adds molecules to the frame. Run at initialisation of the frame
     */
    public void addFluorophores() {
        for (Fluorophore fluorophore : fluorophores) {
            Fluorophore.State fluorophoreState = fluorophore.getState();
            if (fluorophoreState != Fluorophore.State.OFF) {
                double factor = ThreadLocalRandom.current().nextDouble(0.25, 1);
                fluorophore.locationGen();
                if (fluorophoreCoordinates.contains(fluorophore.getLocation())) {
                    fluorophore.locationGen();
                }
                Coordinate fluoroCoordinates = fluorophore.getLocation();
                IntervalIterator iterator = new IntervalIterator(calculateInterval(fluorophore));
                ArrayRandomAccess<UnsignedByteType> iter = getImage().randomAccess();
                PSFModel model = fluorophore.getPsfModel();
                while (iterator.hasNext()) {
                    iterator.fwd();
                    iter.setPosition(iterator);
                    UnsignedByteType current = iter.get();
                    double toAdd = generatePixelIntensity(fluorophoreState, factor, fluoroCoordinates, iter, model, current);
                    if (current.getInteger() <= 255) {
                        if (toAdd >= 255) {
                            current.set(255);
                        } else {
                            current.set((int) toAdd);
                        }
                    }
                }
            }
        }
        if (meanNoise > 0) {
            addNoise();
        }
    }

    private double generatePixelIntensity(Fluorophore.State fluorophoreState, double factor,
                                          Coordinate fluoroCoordinates, ArrayRandomAccess<UnsignedByteType> iter,
                                          PSFModel model, UnsignedByteType current) {
        double convertedX = convertFromPixelToNm(iter.getIntPosition(0), getGroundTruth().getWidth(),
                getDimensions()[1]);
        double convertedY = convertFromPixelToNm(iter.getIntPosition(1), getGroundTruth().getHeight(),
                getDimensions()[0]);
        Coordinate currentPosition = new Coordinate(convertedX, convertedY);
        double sample = model.getIntensityFactor(fluoroCoordinates, currentPosition);
        double pixelIntensity = calculatePixelIntensity(sample);
        return normaliseWithState(pixelIntensity,
                fluorophoreState, factor) + current.getInteger();
    }

    private double calculatePixelIntensity(double sample) {
        double expected = frameIntensity * sample;
        if (expected > 0) {
            PoissonDistribution poissonDistribution = new PoissonDistribution(expected);
            int poissonSample = poissonDistribution.sample();
            return convertFromIntensityToPixelValue(frameIntensity, poissonSample);

        } else {
            return 0;
        }
    }

    private void addNoise() {
        Cursor<UnsignedByteType> iter = getImage().cursor();
        double underLyingPhoton = convertFromIntensityToPixelValue(frameIntensity, 5.0);
        while (iter.hasNext()) {
            UnsignedByteType current = iter.next();
            double noise = convertFromIntensityToPixelValue(frameIntensity, noiseDistribution.sample()) + current.getInteger() + underLyingPhoton;
            if (current.getInteger() <= 255) {
                if (noise >= 255) {
                    current.set(255);
                } else {
                    current.set((int) noise);
                }
            }
        }
    }

    private FinalInterval calculateInterval(Fluorophore fluorophore) {
        Coordinate fluoroCoordinates = fluorophore.getLocation();
        double pixelLocationX = convertFromNmToPixel(fluoroCoordinates.getX(), getGroundTruth().getWidth(), getDimensions()[1]);
        double pixelLocationY = convertFromNmToPixel(fluoroCoordinates.getY(), getGroundTruth().getHeight(), getDimensions()[0]);
        long sizeWidth = (long) (convertFromNmToPixel(fluorophore.getPsfModel().getSize() * 2, getGroundTruth().getWidth(), getDimensions()[1]));
        long sizeHeight = (long) (convertFromNmToPixel(fluorophore.getPsfModel().getSize() * 2, getGroundTruth().getHeight(), getDimensions()[0]));
        long startingX = (long) (pixelLocationX - sizeWidth * 2);
        long startingY = (long) (pixelLocationY - sizeHeight * 2);
        long intervalWidth = (long) 4 * sizeWidth + 1;
        long intervalHeight = (long) 4 * sizeHeight + 1;
        long toX = normalise(startingX + intervalWidth, false) - 1;
        long toY = normalise(startingY + intervalHeight, true) - 1;
        return Intervals.createMinMax(normalise(startingX, false), normalise(startingY, true), toX, toY);

    }

    public int getId() {
        return id;
    }
}

