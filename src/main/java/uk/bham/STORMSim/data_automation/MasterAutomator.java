package uk.bham.STORMSim.data_automation;

import net.imagej.ImageJ;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class that moderates a group of DataAutomators to generate a collection of STORM simulations with different
 * parameters for testing or for experimental purposes
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class MasterAutomator {

    private boolean randomSize = false;
    private boolean randomMolNumber = false;
    private boolean randomOnChance = false;
    private boolean randomOffChance = false;
    private boolean randomNumberOfFrames = false;
    private boolean randomTagChance = false;
    private boolean randomPsfVariance = false;
    private boolean randomFluorosize = false;
    private boolean randomNoise = false;
    private boolean randomSwitchingCycle = false;
    private boolean mlData = false;
    private String directory;
    private ExecutorService threadPool;
    private int limit;
    private int increment;
    private int startingValue;
    private Variable changedVariable;

    public MasterAutomator(String directory, int numberOfThreads, int limit, int increment,
                           int startingValue, Variable changedVariable) {
        this.directory = directory;
        threadPool = Executors.newFixedThreadPool(numberOfThreads);
        this.limit = limit;
        this.increment = increment;
        this.startingValue = startingValue;
        this.changedVariable = changedVariable;

    }

    public MasterAutomator(List<Variable> randomVariables, int number, String directory, int numberOfThreads) {
        mlData = true;
        startingValue = 0;
        limit = number;
        increment = 1;
        this.directory = directory;
        randomVariables.forEach(this::processVariable);
        threadPool = Executors.newFixedThreadPool(numberOfThreads);
    }

    public MasterAutomator(boolean randomSize, boolean randomMolNumber, boolean randomOnChance, boolean randomOffChance,
                           boolean randomNumberOfFrames, boolean randomTagChance, boolean randomPsfVariance,
                           boolean randomFluorosize, boolean randomNoise, boolean randomSwitchingCycle,
                           boolean mlData, String directory, int numberOfThreads, int limit, int increment,
                           int startingValue, Variable changedVariable) {
        this.randomSize = randomSize;
        this.randomMolNumber = randomMolNumber;
        this.randomOnChance = randomOnChance;
        this.randomOffChance = randomOffChance;
        this.randomNumberOfFrames = randomNumberOfFrames;
        this.randomTagChance = randomTagChance;
        this.randomPsfVariance = randomPsfVariance;
        this.randomFluorosize = randomFluorosize;
        this.randomNoise = randomNoise;
        this.randomSwitchingCycle = randomSwitchingCycle;
        this.mlData = mlData;
        this.directory = directory;
        threadPool = Executors.newFixedThreadPool(numberOfThreads);
        this.limit = limit;
        this.increment = increment;
        this.startingValue = startingValue;
        this.changedVariable = changedVariable;
    }

    public static void main(String[] args) {
        final ImageJ ij = new ImageJ();
        ij.launch();
        MasterAutomator masterAutomator = new MasterAutomator("C:\\Users\\virtualreality\\IdeaProjects\\STORMSim\\python\\resources\\experiments\\PSF_variance",
                100, 300, 10, 100, Variable.PSF_VARIANCE);
        masterAutomator.generateData();
    }

    public void generateData() {
        for (int i = startingValue; i <= limit; i += increment) {
            if (mlData) {
                DataAutomator dataAutomator = new DataAutomator(mlData, directory, i);
                setRandomVariables(dataAutomator);
                threadPool.execute(new Thread(dataAutomator));
            } else {
                DataAutomator dataAutomator = new DataAutomator(directory, changedVariable);
                incrementVariable(dataAutomator, i);
                setRandomVariables(dataAutomator);
                threadPool.execute(new Thread(dataAutomator));
            }
        }
    }

    private void setRandomVariables(DataAutomator dataAutomator) {
        if (randomSize) {
            dataAutomator.setRandomSize(true);
        }
        if (randomFluorosize) {
            dataAutomator.setRandomFluorosize(true);
        }
        if (randomMolNumber) {
            dataAutomator.setRandomMolNumber(true);
        }
        if (randomNoise) {
            dataAutomator.setRandomNoise(true);
        }
        if (randomNumberOfFrames) {
            dataAutomator.setRandomNumberOfFrames(true);
        }
        if (randomOnChance) {
            dataAutomator.setRandomOnChance(true);
        }
        if (randomOffChance) {
            dataAutomator.setRandomOffChance(true);
        }
        if (randomPsfVariance) {
            dataAutomator.setRandomPsfVariance(true);
        }
        if (randomSwitchingCycle) {
            dataAutomator.setRandomSwitchingCyle(true);
        }
        if (randomTagChance) {
            dataAutomator.setRandomTagChance(true);
        }
    }

    private void incrementVariable(DataAutomator dataAutomator, int i) {
        switch (changedVariable) {
            case ON_CHANCE:
                dataAutomator.setOnChance(i);
                break;
            case FRAME_SIZE:
                dataAutomator.setSize(i);
                break;
            case MOL_NUMBER:
                dataAutomator.setMolNumber(i);
                break;
            case OFF_CHANCE:
                dataAutomator.setOffChance(i);
                break;
            case NUMBER_OF_FRAMES:
                dataAutomator.setNumberOfFrames(i);
                break;
            case TAG_CHANCE:
                dataAutomator.setTagChance(i);
                break;
            case NOISE_VALUE:
                dataAutomator.setNoise(i);
                break;
            case PSF_VARIANCE:
                dataAutomator.setPsfVariance(i);
                break;
            case SWITCHING_CYCLE:
                dataAutomator.setSwitchingCycle(i);
                break;
            case SIZE_OF_FLUOROPHORE:
                dataAutomator.setFluoroSize(i);
                break;
        }
    }

    private void processVariable(Variable variable) {
        switch (variable) {
            case SIZE_OF_FLUOROPHORE:
                randomFluorosize = true;
                break;
            case SWITCHING_CYCLE:
                randomSwitchingCycle = true;
                break;
            case PSF_VARIANCE:
                randomPsfVariance = true;
                break;
            case NOISE_VALUE:
                randomNoise = true;
                break;
            case TAG_CHANCE:
                randomTagChance = true;
                break;
            case OFF_CHANCE:
                randomOffChance = true;
                break;
            case MOL_NUMBER:
                randomMolNumber = true;
                break;
            case FRAME_SIZE:
                randomSize = true;
                break;
            case ON_CHANCE:
                randomOnChance = true;
                break;
            case NUMBER_OF_FRAMES:
                randomNumberOfFrames = true;
                break;
        }

    }

    public enum Variable {
        FRAME_SIZE, MOL_NUMBER, ON_CHANCE, OFF_CHANCE, NUMBER_OF_FRAMES, TAG_CHANCE,
        PSF_VARIANCE, SIZE_OF_FLUOROPHORE, NOISE_VALUE, SWITCHING_CYCLE
    }
}
