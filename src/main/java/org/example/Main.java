package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String inputFilePath = "C://soft//notes//youtube.txt"; // Ścieżka do pliku wejściowego z linkami
        String outputFilePath = "C://soft//notes//run_youtube_dlp.bat"; // Ścieżka do pliku wyjściowego .bat

        // Podziel szablon komendy na dwie części, aby wstawić link w odpowiednie miejsce
        String commandPrefix = "yt-dlp -f bestaudio --extract-audio --audio-format mp3";
        String commandSuffix = "--ffmpeg-location \"C:\\Program Files\\ffmpeg-master-latest-win64-gpl\\bin\"";

        List<String> generatedCommands = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Upewnij się, że linia nie jest pusta i jest faktycznie linkiem
                if (!line.trim().isEmpty()) {
                    String fullCommand = commandPrefix + " \"" + line.trim() + "\" " + commandSuffix;
                    generatedCommands.add(fullCommand);
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd odczytu pliku: " + inputFilePath + " - " + e.getMessage());
            return; // Zakończ program w przypadku błędu odczytu
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write("@echo off"); // Nagłówek dla pliku .bat
            writer.newLine();
            writer.write("chcp 65001"); // Ustawienie kodowania dla polskich znaków
            writer.newLine();

            for (String command : generatedCommands) {
                writer.write(command);
                writer.newLine(); // Dodaj nową linię po każdej komendzie
            }
            writer.write("pause"); // Zatrzymanie okna konsoli po wykonaniu komend
            writer.newLine();

            System.out.println("Wygenerowany plik .bat został zapisany do: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Błąd zapisu pliku: " + outputFilePath + " - " + e.getMessage());
        }
    }
}
