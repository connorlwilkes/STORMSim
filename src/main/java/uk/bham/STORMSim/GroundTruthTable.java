package uk.bham.STORMSim;

import net.imagej.table.DefaultGenericTable;
import net.imagej.table.DoubleColumn;
import net.imagej.table.GenericTable;
import net.imagej.table.IntColumn;

import java.text.DecimalFormat;

/**
 * A class that generates a table of the locations of all the molecules present in the simulation
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class GroundTruthTable {

    private GenericTable table;
    private DoubleColumn xLocation;
    private DoubleColumn yLocation;
    private IntColumn id;
    private GroundTruth groundTruth;

    public GroundTruthTable(GroundTruth groundTruth) {
        xLocation = new DoubleColumn("x Location (nm)");
        yLocation = new DoubleColumn("y Location (nm)");
        id = new IntColumn("Molecule id");
        this.groundTruth = groundTruth;
    }

    /**
     * Adds a molecule to the table
     *
     * @param molecule molecule to add
     */
    public void addMoleculeToTable(Molecule molecule) {
//        double nmHeight = groundTruth.getHeight();
//        double x = nmHeight - molecule.getCoordinates().getX();
        double x = molecule.getCoordinates().getX();
        double y = molecule.getCoordinates().getY();
        DecimalFormat df = new DecimalFormat("#.##");
        x = Double.valueOf(df.format(x));
        y = Double.valueOf(df.format(y));
        xLocation.add(x);
        yLocation.add(y);
        id.add(molecule.getId());
    }

    /**
     * Compiles the table
     */
    public void makeTable() {
        table = new DefaultGenericTable();
        table.add(id);
        table.add(xLocation);
        table.add(yLocation);
    }

    public GenericTable getTable() {
        return table;
    }
}
