package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends JFrame {

    private JButton startButton;
    private JButton stopButton;
    private JTextArea linkDisplayArea;
    private List<String> capturedLinks;
    private AtomicBoolean isCapturing;
    private Thread clipboardMonitorThread;

    // Existing command parts
    private final String commandPrefix = "yt-dlp -f bestaudio --extract-audio --audio-format mp3";
    private final String commandSuffix = "--ffmpeg-location \"C:\\Program Files\\ffmpeg-master-latest-win64-gpl\\bin\"";
    private final String outputFilePath = "C://soft//notes//run_youtube_dlp.bat";

    public Main() {
        super("YouTube DLP Link Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null); // Center the window

        capturedLinks = new ArrayList<>();
        isCapturing = new AtomicBoolean(false);

        initComponents();
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        startButton = new JButton("Start Capturing Links");
        stopButton = new JButton("Stop Capturing & Generate BAT");
        linkDisplayArea = new JTextArea(10, 40);
        linkDisplayArea.setEditable(false);
        linkDisplayArea.setLineWrap(true);
        linkDisplayArea.setWrapStyleWord(true);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(linkDisplayArea), BorderLayout.CENTER);
    }

    private void addListeners() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startCapturing();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopCapturingAndGenerateBat();
            }
        });
    }

    private void startCapturing() {
        if (isCapturing.compareAndSet(false, true)) {
            linkDisplayArea.setText("Capturing started...\n");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);

            clipboardMonitorThread = new Thread(new ClipboardMonitor());
            clipboardMonitorThread.setDaemon(true); // Allow application to exit even if this thread is running
            clipboardMonitorThread.start();
        }
    }

    private void stopCapturingAndGenerateBat() {
        if (isCapturing.compareAndSet(true, false)) {
            // Interrupt the clipboard monitoring thread
            if (clipboardMonitorThread != null) {
                clipboardMonitorThread.interrupt();
            }

            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            linkDisplayArea.append("\nCapturing stopped. Generating .bat file...\n");
            generateBatFile();
            capturedLinks.clear(); // Clear links after generating the file
        }
    }

    private void generateBatFile() {
        if (capturedLinks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No links captured to generate .bat file.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write("@echo off");
            writer.newLine();
            writer.write("chcp 65001");
            writer.newLine();

            for (String link : capturedLinks) {
                String fullCommand = commandPrefix + " \"" + link.trim() + "\" " + commandSuffix;
                writer.write(fullCommand);
                writer.newLine();
            }
            writer.write("pause");
            writer.newLine();

            linkDisplayArea.append("Generated .bat file saved to: " + outputFilePath + "\n");
            JOptionPane.showMessageDialog(this, "Generated .bat file saved to: " + outputFilePath, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            linkDisplayArea.append("Error writing .bat file: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Error writing .bat file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error writing .bat file: " + e.getMessage());
        }
    }

    private class ClipboardMonitor implements Runnable {
        private String lastClipboardContent = "";

        @Override
        public void run() {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            while (isCapturing.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    Transferable contents = clipboard.getContents(null);
                    if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        String currentClipboardContent = (String) contents.getTransferData(DataFlavor.stringFlavor);

                        if (!currentClipboardContent.equals(lastClipboardContent) && !currentClipboardContent.trim().isEmpty()) {
                            // Basic check for a URL pattern (can be improved)
                            if (currentClipboardContent.startsWith("http://") || currentClipboardContent.startsWith("https://")) {
                                final String link = currentClipboardContent.trim();
                                capturedLinks.add(link);
                                SwingUtilities.invokeLater(() -> {
                                    linkDisplayArea.append("Captured: " + link + "\n");
                                });
                            }
                            lastClipboardContent = currentClipboardContent;
                        }
                    }
                    Thread.sleep(500); // Check clipboard every 500ms
                } catch (UnsupportedFlavorException | IOException e) {
                    System.err.println("Error reading clipboard: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                    System.out.println("Clipboard monitor interrupted.");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
}
