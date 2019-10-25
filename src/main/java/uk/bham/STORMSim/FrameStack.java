package uk.bham.STORMSim;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

import java.util.LinkedList;
import java.util.List;

/**
 * A class that pulls together a number of frames in a sequence in a queue and then creates an overall video of the
 * frames put together
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class FrameStack {

    private List<STORMFrame> STORMFrameList;
    private ArrayImg<UnsignedByteType, ?> totalImage;
    private int[] dimensions;

    /**
     * Constructor for the FrameStack class
     *
     * @param settings settings of the simulation
     */
    public FrameStack(Settings settings) {
        STORMFrameList = new LinkedList<>();
        dimensions = settings.getPixelDimensionsTotal();
        totalImage = new ArrayImgFactory<>(new UnsignedByteType()).create(dimensions);
    }

    public synchronized void addFrame(STORMFrame STORMFrame) {
        STORMFrameList.add(STORMFrame);
    }

    public Img<UnsignedByteType> getTotalImage() {
        return totalImage;
    }

    /**
     * Inserts a frame into the overall image
     *
     * @param frameNumber the number of the frame in sequential order
     * @param STORMFrame  the frame to add
     */
    public void addFrameToTotalVideo(int frameNumber, STORMFrame STORMFrame) {
        FinalInterval interval = Intervals.createMinMax(0, 0, frameNumber, dimensions[0] - 1, dimensions[1] - 1, frameNumber);
        IntervalIterator location = new IntervalIterator(interval);
        ArrayRandomAccess<UnsignedByteType> iterator = totalImage.randomAccess();
        Img<UnsignedByteType> image = STORMFrame.getImage();
        Cursor<UnsignedByteType> frameIter = image.cursor();
        while (location.hasNext()) {
            location.fwd();
            iterator.setPosition(location);
            iterator.get().set(frameIter.next());
        }


    }
}