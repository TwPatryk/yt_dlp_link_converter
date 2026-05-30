package org.example;

import javax.swing.*;
import java.awt.Component; // Added import
import java.io.File;
import java.io.IOException;

public class BatFileRunner {

    public static void run(Component parentComponent, String outputPath, String type) {
        File batFile = new File(outputPath);
        if (!batFile.exists()) {
            JOptionPane.showMessageDialog(parentComponent, type + " BAT file not found at: " + outputPath + "\nPlease generate it first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", batFile.getAbsolutePath());
            pb.start();
            JOptionPane.showMessageDialog(parentComponent, type + " BAT file launched successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent, "Error running " + type + " BAT file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error running " + type + " BAT file: " + e.getMessage());
        }
    }
}
