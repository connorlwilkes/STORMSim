package uk.bham.STORMSim;

import net.imagej.table.GenericTable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class that exports results to CSV
 *
 * @author Connor Wilkes
 * @version 23/07/2018
 */
public class CSVExporter {

    private GenericTable table;
    private char separator;
    private File file;

    public CSVExporter(GenericTable table, File file) {
        this.table = table;
        separator = ',';
        this.file = file;
    }

    /**
     * Exports the table to csv format
     *
     * @throws IOException if there is a problem with file creation/writing
     */
    public void export() throws IOException {
        if (file.createNewFile()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.flush();
            writer.append("\"Molecule ID\",\"x Location (nm)\",\"y Location (nm)\"\n");
            for (int i = 0; i < table.getRowCount(); i++) {
                for (int j = 0; j < table.getColumnCount(); j++) {
                    writer.append('"');
                    if (j == table.getColumnCount() - 1) {
                        writer.append(String.valueOf(table.get(j, i)));
                        writer.append('"');
                    } else {
                        writer.append(String.valueOf(table.get(j, i)));
                        writer.append('"');
                        writer.append(separator);
                    }
                }
                writer.append('\n');
                writer.flush();
            }
            writer.close();
        }
    }
}
