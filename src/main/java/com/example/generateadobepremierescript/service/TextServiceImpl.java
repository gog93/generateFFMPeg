package com.example.generateadobepremierescript.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TextServiceImpl {
//    @Value("${upload.path}")
    private static String filePath= "C:\\myProjects\\generateAdobePremiereScript\\";

//    @Value("${source.dir}")
    private static String directoryPath="C:\\Users\\gohar\\Videos\\Captures";


    public void generateFFMpegCommand(String word, String text, String videoPackageName) {
        String outputVideoPath = UUID.randomUUID() + ".mp4"; // Replace with the desired path for the output video
        String videoFileName = videoPackageName + text;
        String ffmpegCommand = "ffmpeg -f lavfi -i color=c=black:s=1280x720:r=30:d=5 -vf drawtext=text='" + word + "':fontfile=OpenSans-Semibold.ttf:fontsize=28:fontcolor=white:x=50:y=50 " + videoPackageName + outputVideoPath;
        System.out.println("FFmpeg Command: " + ffmpegCommand);
        writeInVideoFileName(videoFileName, outputVideoPath);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCommand);
            processBuilder.inheritIO(); // Redirect input, output, and error streams to the console
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("FFmpeg command executed successfully.");
            } else {
                System.out.println("FFmpeg command failed. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void read(String text, String videoPackageName) {
        String videoFileName = UUID.randomUUID() + ".txt";
        String word = "";

        try {

            Scanner scanner = new Scanner(new File(text));

            while (scanner.hasNext()) {
                word = scanner.next();
                if (word.startsWith("<") && word.endsWith(">")) {
                    String b = word.replaceAll("<|>", "");
                    checkVideoName(b, videoFileName, videoPackageName);

                } else {
                    generateFFMpegCommand(word, videoFileName, videoPackageName);

                }

                System.out.println(word);
            }

            scanner.close();

            if (isVideoFileLineCountGreaterThanTwo(videoPackageName, videoFileName)) {
                concatVideos(videoPackageName, videoFileName);
            }

        } catch (IOException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }

    public void checkVideoName(String videoNameToCheck, String videoFileName, String videoPackageName) throws IOException {

        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String videoName = file.getName();

                    if (videoName.equalsIgnoreCase(videoNameToCheck)) {
                        moveVideosDir(videoNameToCheck, directoryPath, videoPackageName);

                        // Set the file path
                        String filePathWithVideoName = videoPackageName + videoFileName;
                        writeInVideoFileName(filePathWithVideoName, file.getName());
                    }
                } else if (file.isDirectory()) {
                    // Check the files in the subdirectory
                    String subdirectoryPath = file.getAbsolutePath();
                    File subdirectory = new File(subdirectoryPath);
                    File[] subdirectoryFiles = subdirectory.listFiles();

                    if (subdirectoryFiles != null) {
                        for (File subdirectoryFile : subdirectoryFiles) {
                            if (subdirectoryFile.isFile()) {
                                String videoName = subdirectoryFile.getName();

                                if (videoName.equalsIgnoreCase(videoNameToCheck)) {
                                    moveVideosDir(videoNameToCheck, subdirectoryPath, videoPackageName);

                                    // Set the file path
                                    String filePathWithVideoName = videoPackageName + videoFileName;
                                    writeInVideoFileName(filePathWithVideoName, subdirectoryFile.getName());
                                }
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("Directory does not exist or an I/O error occurred");

        }

    }

    public void concatVideos(String filePath, String videoFileName) {
        String concatedVideoName = UUID.randomUUID().toString() + ".mp4";
        String concatCommand = "ffmpeg -f concat -safe 0 -i " + videoFileName + " -c copy " + filePath + "concated-" + concatedVideoName;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", concatCommand);
            processBuilder.directory(new File(filePath));

            Process process = processBuilder.inheritIO().start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("FFmpeg command executed successfully in directory: " + filePath);
            } else {
                System.err.println("Failed to execute the FFmpeg command. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

        }
    }

    public String write(String fileText, String text) {
        String uu = UUID.randomUUID().toString();
        String videoPackageName = filePath + uu + "\\";
        File mk = new File(videoPackageName);
        if (!mk.isDirectory()) {
            mk.mkdir();
        }

        try {
            File file = new File(fileText);
            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();
            System.out.println("Text written to text.txt successfully.");
            if (fileText.equals("text.txt")) {
                read("text.txt", videoPackageName);
            }
        } catch (IOException e) {
            System.out.println("Error occurred while writing to the file: " + e.getMessage());
        }
        return uu;
    }

    public void writeInVideoFileName(String filePath, String text) {

        try {
            FileWriter fileWriter = new FileWriter(filePath, true);

            fileWriter.write("file " + text + "\n");

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isVideoFileLineCountGreaterThanTwo(String dirPath, String videoFileName) {
        String filePath = dirPath + "/" + videoFileName;
        int lineCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read the file line by line and count the lines
            while (br.readLine() != null) {
                lineCount++;
                // You can also perform additional logic to process the video file's content
            }
        } catch (IOException e) {
            System.out.println("Error while reading the file: " + e.getMessage());
        }

        // Return true if the line count is greater than 2, otherwise return false
        return lineCount > 1;
    }

    private void moveVideosDir(String videoName, String sourceDir, String videoPackageName) {
        try {
            Path sourcePath = Paths.get(sourceDir);
            Path destinationPath = Paths.get(videoPackageName);

            // Ensure that destination directory exists
            if (!Files.exists(destinationPath)) {
                Files.createDirectories(destinationPath);
            }

            // Use walk method to get a stream of paths in the directory
            // and its subdirectories
            try (Stream<Path> paths = Files.walk(sourcePath)) {
                paths.forEach(path -> {
                    try {
                        // Check if it's a regular file
                        if (Files.isRegularFile(path)) {
                            // If it's the video we are interested in, move it
                            if (path.getFileName().toString().equals(videoName)) {
                                // Resolve the destination path
                                Path dest = destinationPath.resolve(sourcePath.relativize(path));
                                Files.move(path, dest, StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("File '" + videoName + "' moved successfully.");
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error while moving the file: " + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.out.println("Error while moving the files: " + e.getMessage());
        }
    }


}


