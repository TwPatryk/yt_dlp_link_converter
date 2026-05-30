package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClipboardMonitor implements Runnable {
    private final List<String> targetLinksList;
    private final AtomicBoolean isCapturingFlag;
    private final JTextArea displayArea;
    private final String type; // "MP3" or "Video"
    private String lastClipboardContent = "";

    public ClipboardMonitor(List<String> targetLinksList, AtomicBoolean isCapturingFlag, JTextArea displayArea, String type) {
        this.targetLinksList = targetLinksList;
        this.isCapturingFlag = isCapturingFlag;
        this.displayArea = displayArea;
        this.type = type;
    }

    @Override
    public void run() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        while (isCapturingFlag.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Transferable contents = clipboard.getContents(null);
                if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String currentClipboardContent = (String) contents.getTransferData(DataFlavor.stringFlavor);

                    if (!currentClipboardContent.equals(lastClipboardContent) && !currentClipboardContent.trim().isEmpty()) {
                        // Basic check for a URL pattern
                        if (currentClipboardContent.startsWith("http://") || currentClipboardContent.startsWith("https://")) {
                            final String link = currentClipboardContent.trim();
                            targetLinksList.add(link);
                            SwingUtilities.invokeLater(() -> {
                                displayArea.append("Captured (" + type + "): " + link + "\n");
                            });
                        }
                        lastClipboardContent = currentClipboardContent;
                    }
                }
                Thread.sleep(500); // Check clipboard every 500ms
            } catch (UnsupportedFlavorException | IOException e) {
                System.err.println("Error reading clipboard for " + type + ": " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(type + " Clipboard monitor interrupted.");
            }
        }
    }
}
