package org.example;

import javax.swing.*;
import java.awt.Component; // Added import
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BatFileGenerator {

    public static void generate(Component parentComponent, List<String> links, String prefix, String suffix, String outputPath, String type) {
        if (links.isEmpty()) {
            JOptionPane.showMessageDialog(parentComponent, "No " + type + " links captured to generate .bat file.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("@echo off");
            writer.newLine();
            writer.write("chcp 65001");
            writer.newLine();

            for (String link : links) {
                String fullCommand = prefix + " \"" + link.trim() + "\" " + suffix;
                writer.write(fullCommand);
                writer.newLine();
            }
            writer.write("pause");
            writer.newLine();

            JOptionPane.showMessageDialog(parentComponent, "Generated " + type + " .bat file saved to: " + outputPath, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent, "Error writing " + type + " .bat file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error writing " + type + " .bat file: " + e.getMessage());
        }
    }
}
