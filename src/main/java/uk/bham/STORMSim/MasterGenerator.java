package uk.bham.STORMSim;

import org.scijava.app.StatusService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A wrapper class that generates a FrameStack given a Ground Truth model. Gives abstraction from dealing with each of
 * the other classes individually.
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class MasterGenerator {

    private GroundTruth groundTruth;
    private GroundTruthFrame groundTruthFrame;
    private FrameStack frames;
    private Settings params;
    private ExecutorService threadPool;
    private StatusService stormSimInstance;

    public MasterGenerator(StatusService stormSim, Settings settings) {
        stormSimInstance = stormSim;
        groundTruth = new GroundTruth(settings);
        params = settings;
        threadPool = Executors.newFixedThreadPool(10);
        createFrames(settings.getNumberOfFrames());
    }

    /**
     * Given a parameter describing the total number of frames to create generates a FrameStack object
     *
     * @param numberOfFrames number of frames to create
     */
    private void createFrames(int numberOfFrames) {
        frames = new FrameStack(params);
        groundTruthFrame = new GroundTruthFrame(groundTruth, params);
        List<List<Fluorophore>> individualFrameFluorophores = groundTruth.getIndividualFrameFluorophores();
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < numberOfFrames; i++) {
            STORMFrame newSTORMFrame = new STORMFrame(groundTruth, i, params, individualFrameFluorophores.get(i));
            threadPool.execute(() -> {
                newSTORMFrame.addFluorophores();
                frames.addFrame(newSTORMFrame);
                frames.addFrameToTotalVideo(newSTORMFrame.getId(), newSTORMFrame);
                counter.incrementAndGet();
                if (stormSimInstance != null) {
                    stormSimInstance.showStatus(counter.get(), numberOfFrames, "Completed processing of frame: " + newSTORMFrame.getId());
                }
            });
        }
        boolean done = false;
        while (!(done)) {
            if (counter.get() == numberOfFrames) {
                if (stormSimInstance != null) {
                    stormSimInstance.showStatus("Finishing processing of frames");
                }
                done = true;
            }
        }
    }

    public FrameStack getFrames() {
        return frames;
    }

    public GroundTruth getGroundTruth() {
        return groundTruth;
    }

    public GroundTruthFrame getGroundTruthFrame() {
        return groundTruthFrame;
    }
}
