/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package uk.bham.STORMSim;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imagej.table.GenericTable;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.scijava.ItemVisibility;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.convert.ConvertService;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * The main class of the STORMSim plug in. This plug in generates realistic STORMSim simulated data given a set of user
 * defined parameters
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
@Plugin(type = Command.class, menuPath = "Plugins>STORMSim")
public class STORMSim implements Command {

    @Parameter(visibility = ItemVisibility.MESSAGE)
    private final String fluorophoreSectionHeader = "Fluorophore Settings";
    @Parameter(visibility = ItemVisibility.MESSAGE)
    private final String frameHeader = "STORMFrame Settings";
    @Parameter(visibility = ItemVisibility.MESSAGE)
    private final String psfHeader = "PSF Settings";
    @Parameter(visibility = ItemVisibility.MESSAGE)
    private final String photoswitchingModelHeader = "Photoswitching Model Settings";
    @Parameter(visibility = ItemVisibility.MESSAGE)
    private final String save = "Saving";
    @Parameter
    private StatusService statusService;
    @Parameter
    private UIService uiService;
    @Parameter
    private IOService ioService;
    @Parameter
    private ConvertService convertService;
    @Parameter(label = "Size of fluorophore (nm)", min = "0")
    private double fluorosize;
    @Parameter(label = "Switching Cycle", min = "0")
    private int switchingCycle;
    @Parameter(label = "Intensity (Photon count)", min = "0")
    private double intensity;
    @Parameter(label = "Height & Width  of frame (nm)", min = "400")
    private int heightAndWidth;
    @Parameter(label = "Number of molecules", min = "1")
    private int moleculeNumber;
    @Parameter(label = "Number of frames", min = "1")
    private int numberOfFrames;
    @Parameter(label = "Noise value (mean)", min = "0")
    private double noise;
    @Parameter(label = "Type of PSF model to use", choices = {"Gaussian"})
    private String PSFmodel;
    @Parameter(label = "PSF standard deviation", min = "0")
    private double psfStDev;
    @Parameter(label = "Type of photoswitching model to use", choices = {"Markov"})
    private String photoswitchingModel;
    @Parameter(label = "On chance", min = "0", max = "1.0")
    private double onChance;
    @Parameter(label = "Off chance", min = "0", max = "1.0")
    private double offChance;
    @Parameter(label = "Tag chance", min = "0", max = "1.0")
    private double tagChance;
    @Parameter(label = "Save STORMImage?")
    private boolean imageSaveEnabled;

    @Parameter(label = "Save results?")
    private boolean resultsSaveEnabled;

    @Parameter(label = "Save location")
    private String saveLocation = "./";

    private Img<UnsignedByteType> STORMImage;
    private Img<UnsignedByteType> groundTruthImage;
    private GenericTable table;


    public static void main(String[] args) {
        final ImageJ ij = new ImageJ();
        ij.launch();
    }


    @Override
    public void run() {
        Fluorophore fluorophore = new Fluorophore(fluorosize, switchingCycle, intensity);
        if (photoswitchingModel.equals("Markov")) {
            MarkovPhotoswitchingModel photoSwitchingModel = new MarkovPhotoswitchingModel(onChance, offChance);
            Settings settings = new Settings(heightAndWidth, heightAndWidth, moleculeNumber, numberOfFrames, psfStDev, noise, photoSwitchingModel, fluorophore, tagChance);
            MasterGenerator data = new MasterGenerator(statusService, settings);
            display(data);
        }
    }

    private void display(MasterGenerator data) {
        STORMImage = data.getFrames().getTotalImage();
        groundTruthImage = data.getGroundTruthFrame().getImage();
        table = data.getGroundTruth().getTable().getTable();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateAndTime = dtf.format(now);
        String imageTitle = "Image:" + dateAndTime;
        String tableTitle = "Table:" + dateAndTime;
        String groundTruthTitle = "Ground Truth Image:" + dateAndTime;
        uiService.show(imageTitle, STORMImage);
        uiService.show(tableTitle, table);
        uiService.show(groundTruthTitle, groundTruthImage);
        if (imageSaveEnabled || resultsSaveEnabled) {
            save(dateAndTime, tableTitle, imageTitle, groundTruthTitle, imageSaveEnabled, resultsSaveEnabled);
        }
    }

    private void save(String dateAndTime, String tableTitle, String imageTitle, String groundTruthTitle, boolean imageSave, boolean tableSave) {
        try {
            if ("".equals(saveLocation)) {
                saveLocation = "./";
            } else if (!(saveLocation.endsWith("/"))) {
                saveLocation = saveLocation.concat("/");
            }
            Path dir = Paths.get(saveLocation + "STORMSim", dateAndTime);
            Files.createDirectories(dir);
            if (imageSave) {
                File imageFile = new File(dir.toFile(), imageTitle + ".tif");
                String path = imageFile.getAbsolutePath();
                ImagePlus img = convertService.convert(STORMImage, ImagePlus.class);
                IJ.save(img, path);
            }
            if (tableSave) {
                File tableFile = new File(dir.toFile(), tableTitle + ".csv");
                CSVExporter exporter = new CSVExporter(table, tableFile);
                exporter.export();
                File imageFile = new File(dir.toFile(), groundTruthTitle + ".tif");
                String path = imageFile.getAbsolutePath();
                ImagePlus img = convertService.convert(STORMImage, ImagePlus.class);
                IJ.save(img, path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
