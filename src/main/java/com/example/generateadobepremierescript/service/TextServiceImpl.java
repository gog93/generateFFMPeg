package com.example.generateadobepremierescript.service;

import lombok.RequiredArgsConstructor;
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
    private static String filePath= "C:\\Users\\gohar\\Desktop\\generateAdobePremiereScript\\";

//    @Value("${source.dir}")
    private static String directoryPath="C:\\Users\\gohar\\Videos\\Captures";


    public void generateFFMpegCommand(String text, String videoPackageName, String outputVideoPath, String ffmpegCommand) {
        String videoFileName = videoPackageName + text;
        System.out.println("FFmpeg Command: " + ffmpegCommand);
        writeInVideoFileName(videoFileName, outputVideoPath);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCommand);
            processBuilder.inheritIO(); // Redirect input, output, and error streams to the console

            Process process = processBuilder.start();
            System.out.println("-y");

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
        int count = 1;
        boolean forAppend=false;
        int wordCount=0;

        try {

            Scanner scanner = new Scanner(new File(text));

            while (scanner.hasNext()) {
                String videoName = count + ".mp4";

                word = scanner.next();
                if (word.startsWith("<",0) && !word.startsWith("/",1) ) {
                    forAppend=false;
                    String ffmpegCommand = "ffmpeg -f lavfi -i color=c=black:s=1280x720:r=30:d=5 -vf drawtext=textfile=textfile.txt:fontfile=OpenSans-Semibold.ttf:fontsize=28:fontcolor=white:x=50:y=50 " + videoPackageName + videoName;

                    generateFFMpegCommand(videoFileName, videoPackageName, videoName, ffmpegCommand);
                    String b = word.replaceAll("<|>", "");
                    ++count;
                    videoName=count+ ".mp4";
                    checkVideoName(b, videoFileName, videoPackageName, videoName);

                }else if (word.startsWith("</")){
                    forAppend=false;
                   String c= videoName;
                    String tempOutput = "temp.mp4";

                    String a=" ffmpeg -y -i "+videoPackageName +videoName+" -vf \"drawtext=textfile=textfile.txt:fontfile=OpenSans-Semibold.ttf:fontsize=28:fontcolor=white:x=50:y=50\" "+videoPackageName+tempOutput;
                    String ffmpegCommand = "ffmpeg -f lavfi -i color=c=black:s=1280x720:r=30:d=5 -vf drawtext=textfile=textfile.txt:fontfile=OpenSans-Semibold.ttf:fontsize=28:fontcolor=white:x=50:y=50 " + videoPackageName + videoName;

                    generateFFMpegCommand(videoFileName, videoPackageName, videoName, a);

                    Files.move(Paths.get(videoPackageName + tempOutput), Paths.get(videoPackageName + videoName), StandardCopyOption.REPLACE_EXISTING);

                }else {
                    try {
                        FileWriter out = new FileWriter("textfile.txt", forAppend);
                        forAppend=true;

                        out.write(word);
                        if (wordCount>=10){
                            out.append("\n");
                            wordCount=0;
                        }
                        out.append(" ");
                        out.close();
                        ++wordCount;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }

                System.out.println(word);
            }

            scanner.close();

            if (isVideoFileLineCountGreaterThanTwo(videoPackageName, videoFileName)) {
                concatVideos(videoPackageName, videoFileName,++count);
            }

        } catch (IOException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }

    public void checkVideoName(String videoNameToCheck, String videoFileName, String videoPackageName, String name) throws IOException {

        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String videoName = file.getName();

                    if (videoName.equalsIgnoreCase(videoNameToCheck)) {
                        moveVideosDir(videoNameToCheck, directoryPath, videoPackageName);
                        Path sourcePath = Paths.get(videoPackageName+"\\"+videoNameToCheck+".mp4");
                        Path destinationPath = Paths.get(videoPackageName+"\\"+name+".mp4");

                        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

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
                                    Path sourcePath = Paths.get(videoPackageName+"\\"+videoNameToCheck);
                                    Path destinationPath = Paths.get(videoPackageName+"\\"+name);

                                    Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                                    // Set the file path
                                    String filePathWithVideoName = videoPackageName + videoFileName;
                                    writeInVideoFileName(filePathWithVideoName, name);
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

    public void concatVideos(String filePath, String videoFileName,int count) {
        String concatedVideoName = count + ".mp4";
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


