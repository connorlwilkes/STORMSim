package uk.bham.STORMSim;

import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that represents the Ground Truth of the model, i.e the actual location of each molecule along an (x, y)
 * coordinate system described in terms of nanometers
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class GroundTruth {

    private List<Molecule> allMolecules;
    private List<List<Fluorophore>> individualFrameFluorophores;
    private ConcurrentHashMap<Coordinate, Molecule> molLocations;
    private GroundTruthTable table;
    private int height, width;


    public GroundTruth(Settings params) {
        molLocations = new ConcurrentHashMap<>();
        height = params.getNmDimensions()[0];
        width = params.getNmDimensions()[1];
        table = new GroundTruthTable(this);
        generateMoleculeLocations(params);
        generateIndividualFramedata(params);
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public List<List<Fluorophore>> getIndividualFrameFluorophores() {
        return individualFrameFluorophores;
    }

    public List<Molecule> getAllMolecules() {
        return allMolecules;
    }

    public GroundTruthTable getTable() {
        return table;
    }

    /**
     * Generates allMolecules for the Ground Truth
     *
     * @param settings settings to use to create the allMolecules
     */
    private void generateMoleculeLocations(Settings settings) {
        List<Molecule> moleculeList = new ArrayList<>();
        UniformRealDistribution distX = new UniformRealDistribution(0, width + 1);
        UniformRealDistribution distY = new UniformRealDistribution(0, height + 1);
        Coordinate coordinate = new Coordinate(distX.sample(), distY.sample());
        for (int i = 0; i < settings.getNumberOfMolecules(); i++) {
            Molecule mol = new Molecule(coordinate, i + 1);
            double chance = ThreadLocalRandom.current().nextDouble(1);
            if (chance >= settings.getTagChance()) {
                mol.setTagged(false);
                molLocations.put(coordinate, mol);
            } else {
                List<Fluorophore> fluorophores = new LinkedList<>();
                PhotoswitchingModel photoswitchingModel = settings.getPhotoswitchingModel();
                fluorophores.add(new Fluorophore(coordinate, settings.getPSFModel(), photoswitchingModel, settings.getFluorophore()));
                mol.setTagged(true);
                mol.setFluorophores(fluorophores);
                molLocations.put(coordinate, mol);

            }
            table.addMoleculeToTable(mol);
            moleculeList.add(mol);
            while (molLocations.containsKey(coordinate)) {
                double randomX = distX.sample();
                double randomY = distY.sample();
                coordinate = new Coordinate(randomX, randomY);
            }

        }
        table.makeTable();
        this.allMolecules = moleculeList;
    }

    private void generateIndividualFramedata(Settings settings) {
        individualFrameFluorophores = new LinkedList<>();
        for (int i = 0; i < settings.getNumberOfFrames(); i++) {
            List<Fluorophore> currentFrameList = new ArrayList<>();
            for (Molecule molecule : allMolecules) {
                if (molecule.isTagged()) {
                    for (Fluorophore fluorophore : molecule.getFluorophores()) {
                        if (!(fluorophore.isPhotoBleached())) {
                            if (fluorophore.getPhotoswitchingModel() instanceof MarkovPhotoswitchingModel) {
                                markovChoose(fluorophore, currentFrameList);
                            }
                        }
                    }
                }
            }
            individualFrameFluorophores.add(currentFrameList);
        }
    }

    private void markovChoose(Fluorophore fluorophore, List<Fluorophore> fluorophores) {
        MarkovPhotoswitchingModel model = (MarkovPhotoswitchingModel) fluorophore.getPhotoswitchingModel();
        Fluorophore.State fluorophoreState = fluorophore.getState();
        if (fluorophoreState == Fluorophore.State.ACTIVE) {
            double rand = ThreadLocalRandom.current().nextDouble(1);
            if (model.getOffChance() >= rand) {
                fluorophore.switchState();
            }
            fluorophores.add(new Fluorophore(fluorophore));
        } else if (fluorophoreState == Fluorophore.State.OFF) {
            double rand = ThreadLocalRandom.current().nextDouble(1);
            if (model.getOnChance() >= rand) {
                fluorophore.switchState();
                fluorophores.add(new Fluorophore(fluorophore));
            }
        } else {
            fluorophore.switchState();
            if (fluorophore.getState() == Fluorophore.State.ACTIVE) {
                fluorophores.add(new Fluorophore(fluorophore));
            }
        }
    }
}
