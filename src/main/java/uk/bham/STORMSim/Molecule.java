package uk.bham.STORMSim;

import java.util.List;

/**
 * A class representing a molecule
 *
 * @author Connor Wilkes
 * @version 28/06/2018
 */
public class Molecule {

    private Coordinate coordinates;
    private int id;
    private List<Fluorophore> fluorophores;
    private boolean isTagged;

    /**
     * Constructor for the molecule class
     *
     * @param coordinates location of the molecule in the GroundTruth
     * @param id          the id of the molecule
     */
    public Molecule(Coordinate coordinates, int id) {
        this.coordinates = coordinates;
        this.id = id;
    }

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean tagged) {
        isTagged = tagged;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public int getId() {
        return id;
    }

    public List<Fluorophore> getFluorophores() {
        return fluorophores;
    }

    public void setFluorophores(List<Fluorophore> fluorophores) {
        this.fluorophores = fluorophores;
    }
}


