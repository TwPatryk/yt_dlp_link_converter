package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends JFrame {

    // MP3 Components and Variables
    private JButton startMp3Button;
    private JButton stopMp3Button;
    private JButton runMp3BatButton;
    private List<String> capturedMp3Links;
    private AtomicBoolean isCapturingMp3;
    private Thread clipboardMonitorMp3Thread;
    private final String commandPrefixMp3 = "yt-dlp -f bestaudio --extract-audio --audio-format mp3";
    private final String commandSuffixMp3 = "--ffmpeg-location \"C:\\Program Files\\ffmpeg-master-latest-win64-gpl\\bin\"";
    private final String outputFilePathMp3 = "C://soft//notes//run_youtube_dlp_mp3.bat";

    // Video Components and Variables
    private JButton startVideoButton;
    private JButton stopVideoButton;
    private JButton runVideoBatButton;
    private List<String> capturedVideoLinks;
    private AtomicBoolean isCapturingVideo;
    private Thread clipboardMonitorVideoThread;
    private final String commandPrefixVideo = "yt-dlp -f \"bestvideo+bestaudio\" --merge-output-format mp4";
    private final String commandSuffixVideo = "--ffmpeg-location \"C:\\Program Files\\ffmpeg-master-latest-win64-gpl\\bin\"";
    private final String outputFilePathVideo = "C://soft//notes//run_youtube_dlp_video.bat";

    // Common Components
    private JTextArea linkDisplayArea;

    public Main() {
        super("YouTube DLP Link Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Increased size to accommodate more buttons
        setLocationRelativeTo(null); // Center the window

        capturedMp3Links = new ArrayList<>();
        isCapturingMp3 = new AtomicBoolean(false);

        capturedVideoLinks = new ArrayList<>();
        isCapturingVideo = new AtomicBoolean(false);

        initComponents();
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        // MP3 Buttons
        startMp3Button = new JButton("Start Capturing MP3 Links");
        stopMp3Button = new JButton("Stop Capturing & Generate MP3 BAT");
        runMp3BatButton = new JButton("Run MP3 BAT File");

        // Video Buttons
        startVideoButton = new JButton("Start Capturing Video Links");
        stopVideoButton = new JButton("Stop Capturing & Generate Video BAT");
        runVideoBatButton = new JButton("Run Video BAT File");

        linkDisplayArea = new JTextArea(15, 60); // Adjusted size
        linkDisplayArea.setEditable(false);
        linkDisplayArea.setLineWrap(true);
        linkDisplayArea.setWrapStyleWord(true);

        // Initial states
        stopMp3Button.setEnabled(false);
        runMp3BatButton.setEnabled(false);
        stopVideoButton.setEnabled(false);
        runVideoBatButton.setEnabled(false);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10)); // Add some spacing

        // MP3 Panel
        JPanel mp3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mp3Panel.setBorder(BorderFactory.createTitledBorder("MP3 Download Options"));
        mp3Panel.add(startMp3Button);
        mp3Panel.add(stopMp3Button);
        mp3Panel.add(runMp3BatButton);

        // Video Panel
        JPanel videoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        videoPanel.setBorder(BorderFactory.createTitledBorder("Video Download Options"));
        videoPanel.add(startVideoButton);
        videoPanel.add(stopVideoButton);
        videoPanel.add(runVideoBatButton);

        // Combine button panels
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(mp3Panel);
        controlPanel.add(videoPanel);

        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(linkDisplayArea), BorderLayout.CENTER);
    }

    private void addListeners() {
        // MP3 Listeners
        startMp3Button.addActionListener(e -> startCapturingMp3());
        stopMp3Button.addActionListener(e -> stopCapturingAndGenerateMp3Bat());
        runMp3BatButton.addActionListener(e -> {
            BatFileRunner.run(this, outputFilePathMp3, "MP3");
            linkDisplayArea.append("Attempting to run MP3 BAT file: " + outputFilePathMp3 + "\n");
        });

        // Video Listeners
        startVideoButton.addActionListener(e -> startCapturingVideo());
        stopVideoButton.addActionListener(e -> stopCapturingAndGenerateVideoBat());
        runVideoBatButton.addActionListener(e -> {
            BatFileRunner.run(this, outputFilePathVideo, "Video");
            linkDisplayArea.append("Attempting to run Video BAT file: " + outputFilePathVideo + "\n");
        });
    }

    // --- MP3 Specific Methods ---
    private void startCapturingMp3() {
        if (isCapturingMp3.compareAndSet(false, true)) {
            linkDisplayArea.append("MP3 Capturing started...\n");
            startMp3Button.setEnabled(false);
            stopMp3Button.setEnabled(true);
            runMp3BatButton.setEnabled(false);
            // Disable video buttons while MP3 capturing is active
            startVideoButton.setEnabled(false);
            stopVideoButton.setEnabled(false);
            runVideoBatButton.setEnabled(false);


            clipboardMonitorMp3Thread = new Thread(new ClipboardMonitor(capturedMp3Links, isCapturingMp3, linkDisplayArea, "MP3"));
            clipboardMonitorMp3Thread.setDaemon(true);
            clipboardMonitorMp3Thread.start();
        }
    }

    private void stopCapturingAndGenerateMp3Bat() {
        if (isCapturingMp3.compareAndSet(true, false)) {
            if (clipboardMonitorMp3Thread != null) {
                clipboardMonitorMp3Thread.interrupt();
            }

            startMp3Button.setEnabled(true);
            stopMp3Button.setEnabled(false);
            linkDisplayArea.append("\nMP3 Capturing stopped. Generating MP3 .bat file...\n");
            BatFileGenerator.generate(this, capturedMp3Links, commandPrefixMp3, commandSuffixMp3, outputFilePathMp3, "MP3");
            capturedMp3Links.clear();
            runMp3BatButton.setEnabled(true);
            // Re-enable video buttons
            startVideoButton.setEnabled(true);
            // stopVideoButton and runVideoBatButton state depends on their own logic
        }
    }

    // --- Video Specific Methods ---
    private void startCapturingVideo() {
        if (isCapturingVideo.compareAndSet(false, true)) {
            linkDisplayArea.append("Video Capturing started...\n");
            startVideoButton.setEnabled(false);
            stopVideoButton.setEnabled(true);
            runVideoBatButton.setEnabled(false);
            // Disable MP3 buttons while Video capturing is active
            startMp3Button.setEnabled(false);
            stopMp3Button.setEnabled(false);
            runMp3BatButton.setEnabled(false);

            clipboardMonitorVideoThread = new Thread(new ClipboardMonitor(capturedVideoLinks, isCapturingVideo, linkDisplayArea, "Video"));
            clipboardMonitorVideoThread.setDaemon(true);
            clipboardMonitorVideoThread.start();
        }
    }

    private void stopCapturingAndGenerateVideoBat() {
        if (isCapturingVideo.compareAndSet(true, false)) {
            if (clipboardMonitorVideoThread != null) {
                clipboardMonitorVideoThread.interrupt();
            }

            startVideoButton.setEnabled(true);
            stopVideoButton.setEnabled(false);
            linkDisplayArea.append("\nVideo Capturing stopped. Generating Video .bat file...\n");
            BatFileGenerator.generate(this, capturedVideoLinks, commandPrefixVideo, commandSuffixVideo, outputFilePathVideo, "Video");
            capturedVideoLinks.clear();
            runVideoBatButton.setEnabled(true);
            // Re-enable MP3 buttons
            startMp3Button.setEnabled(true);
            // stopMp3Button and runMp3BatButton state depends on their own logic
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
