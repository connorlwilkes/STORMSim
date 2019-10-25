package uk.bham.STORMSim.data_automation;

import io.scif.img.IO;
import net.imagej.table.GenericTable;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import uk.bham.STORMSim.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class moderated by a Master automator that produces STORM data given a set of parameters
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class DataAutomator implements Runnable {

    private int size = 32000;
    private boolean randomSize = false;
    private int molNumber = 100;
    private boolean randomMolNumber = false;
    private double onChance = 0.25;
    private boolean randomOnChance = false;
    private double offChance = 0.8;
    private boolean randomOffChance = false;
    private int numberOfFrames = 10;
    private boolean randomNumberOfFrames = false;
    private double tagChance = 0.95;
    private boolean randomTagChance = false;
    private double psfVariance = 200;
    private boolean randomPsfVariance = false;
    private int fluoroSize = 10;
    private boolean randomFluorosize = false;
    private int noise = 50;
    private boolean randomNoise = false;
    private int switchingCycle = 10;
    private boolean randomSwitchingCycle = false;
    private boolean mlData;
    private String directory;
    private MasterAutomator.Variable changedVariable;
    private String changedTo;
    private int id;

    public DataAutomator(boolean mlData, String dir, int id) {
        this.mlData = mlData;
        directory = dir;
        this.id = id;
    }

    public DataAutomator(String dir, MasterAutomator.Variable changedVariable) {
        this.directory = dir;
        this.changedVariable = changedVariable;
    }

    @Override
    public void run() {
        generateData();
    }

    private void generateData() {
        String endOfFile;
        if (mlData) {
            generateRandomValues();
            endOfFile = id + "" + "-train_data";
        } else {
            endOfFile = changedVariable.toString() + "-" + changedTo;
        }
        Fluorophore fluorophore = new Fluorophore(fluoroSize, switchingCycle, 300);
        MarkovPhotoswitchingModel photoswitchingModel = new MarkovPhotoswitchingModel(onChance, offChance);
        Settings settings = new Settings(size, size, molNumber, numberOfFrames, psfVariance, noise, photoswitchingModel, fluorophore, tagChance);
        MasterGenerator data = new MasterGenerator(null, settings);
        Img<UnsignedByteType> image = data.getFrames().getTotalImage();
        Img<UnsignedByteType> GTimage = data.getGroundTruthFrame().getImage();
        GenericTable table = data.getGroundTruth().getTable().getTable();
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMM");
        String dateString = date.format(formatter);
        try {
            Path dir = Paths.get(directory, "Image-" + endOfFile + "-" + dateString);
            Files.createDirectories(dir);
            File imageFile = new File(dir.toFile(), "Image-" + endOfFile + ".tif");
            String imgPath = imageFile.getAbsolutePath();
            IO.saveImg(imgPath, image);
            File GTImgFile = new File(dir.toFile(), "GTImage-" + endOfFile + ".tif");
            String path = GTImgFile.getAbsolutePath();
            IO.saveImg(path, GTimage);
            File tableFile = new File(dir.toFile(), "Table-" + endOfFile + "-STORMSim.csv");
            CSVExporter exporter = new CSVExporter(table, tableFile);
            exporter.export();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mlData) {
            System.out.println("Completed: " + id);
        } else {
            System.out.println("Completed: " + changedTo);
        }
    }

    private void generateRandomValues() {
        if (randomSize) {
            size = ThreadLocalRandom.current().nextInt(10000, 32001);
        }
        if (randomMolNumber) {
            molNumber = ThreadLocalRandom.current().nextInt(0, 1001);
        }
        if (randomNoise) {
            noise = ThreadLocalRandom.current().nextInt(0, 201);
        }
        if (randomOnChance) {
            onChance = ThreadLocalRandom.current().nextDouble(0, 1.0);
        }
        if (randomFluorosize) {
            fluoroSize = ThreadLocalRandom.current().nextInt(0, 50);
        }
        if (randomOffChance) {
            offChance = ThreadLocalRandom.current().nextDouble(0, 1.0);
        }
        if (randomNumberOfFrames) {
            numberOfFrames = ThreadLocalRandom.current().nextInt(1, 101);
        }
        if (randomPsfVariance) {
            psfVariance = ThreadLocalRandom.current().nextDouble(100, 201);
        }
        if (randomTagChance) {
            tagChance = ThreadLocalRandom.current().nextDouble(0, 1.0);
        }
        if (randomSwitchingCycle) {
            switchingCycle = ThreadLocalRandom.current().nextInt(1, 101);
        }
    }

    public void setSize(int size) {
        changedTo = String.valueOf(size);
        this.size = size;
    }

    public void setMolNumber(int molNumber) {
        changedTo = String.valueOf(molNumber);
        this.molNumber = molNumber;
    }

    public void setOnChance(double onChance) {
        changedTo = String.valueOf(onChance);
        this.onChance = onChance;
    }

    public void setOffChance(double offChance) {
        changedTo = String.valueOf(offChance);
        this.offChance = offChance;
    }

    public void setNumberOfFrames(int numberOfFrames) {
        changedTo = String.valueOf(numberOfFrames);
        this.numberOfFrames = numberOfFrames;
    }

    public void setTagChance(double tagChance) {
        changedTo = String.valueOf(tagChance);
        this.tagChance = tagChance;
    }

    public void setPsfVariance(double psfVariance) {
        changedTo = String.valueOf(psfVariance);
        this.psfVariance = psfVariance;
    }

    public void setFluoroSize(int fluoroSize) {
        changedTo = String.valueOf(fluoroSize);
        this.fluoroSize = fluoroSize;
    }

    public void setNoise(int noise) {
        changedTo = String.valueOf(noise);
        this.noise = noise;
    }

    public void setRandomSize(boolean randomSize) {
        this.randomSize = randomSize;
    }

    public void setRandomMolNumber(boolean randomMolNumber) {
        this.randomMolNumber = randomMolNumber;
    }

    public void setRandomOnChance(boolean randomOnChance) {
        this.randomOnChance = randomOnChance;
    }

    public void setRandomOffChance(boolean randomOffChance) {
        this.randomOffChance = randomOffChance;
    }

    public void setRandomNumberOfFrames(boolean randomNumberOfFrames) {
        this.randomNumberOfFrames = randomNumberOfFrames;
    }

    public void setRandomTagChance(boolean randomTagChance) {
        this.randomTagChance = randomTagChance;
    }

    public void setRandomPsfVariance(boolean randomPsfVariance) {
        this.randomPsfVariance = randomPsfVariance;
    }

    public void setRandomFluorosize(boolean randomFluorosize) {
        this.randomFluorosize = randomFluorosize;
    }

    public void setRandomNoise(boolean randomNoise) {
        this.randomNoise = randomNoise;
    }

    public void setSwitchingCycle(int switchingCycle) {
        this.switchingCycle = switchingCycle;
    }

    public void setRandomSwitchingCyle(boolean randomSwitchingCyle) {
        this.randomSwitchingCycle = randomSwitchingCyle;
    }


}
