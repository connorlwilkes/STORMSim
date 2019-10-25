package uk.bham.STORMSim;

import net.imglib2.FinalInterval;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

import java.util.List;

/**
 * A class that encapsulates an image that shows the locations of the individual molecules in the ground truth model
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class GroundTruthFrame extends Frame {

    private List<Molecule> molecules;

    public GroundTruthFrame(GroundTruth groundTruth, Settings settings) {
        super(groundTruth, settings);
        molecules = groundTruth.getAllMolecules();
        addMolecules();
    }

    /**
     * Adds molecules to the image
     */
    public void addMolecules() {
        for (Molecule mol : molecules) {
            Coordinate coordinate = mol.getCoordinates();
            ArrayRandomAccess<UnsignedByteType> iter = getImage().randomAccess();
            IntervalIterator iterator = new IntervalIterator(calculateInterval(coordinate));
            while (iterator.hasNext()) {
                iterator.fwd();
                iter.setPosition(iterator);
                UnsignedByteType current = iter.get();
                current.set(255);
            }
        }
    }

    private FinalInterval calculateInterval(Coordinate coordinate) {
        double pixelLocationX = convertFromNmToPixel(coordinate.getX(), getGroundTruth().getWidth(), getDimensions()[1]);
        double pixelLocationY = convertFromNmToPixel(coordinate.getY(), getGroundTruth().getHeight(), getDimensions()[0]);
        long startingX = (long) pixelLocationX;
        long startingY = (long) pixelLocationY;
        long toY = (long) pixelLocationY;
        long toX = (long) pixelLocationX;
        if (pixelLocationX % 1 != 0) {
            startingX = (long) Math.floor(startingX);
            toX = (long) Math.ceil(toX);
        }
        if (pixelLocationY % 1 != 0) {
            startingY = (long) Math.floor(startingY);
            toY = (long) Math.ceil(toY);
        }
        return Intervals.createMinMax(normalise(startingX, false), normalise(startingY, true), toX, toY);
    }
}
