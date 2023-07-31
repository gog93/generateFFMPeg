package com.example.generateadobepremierescript.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ZipUploadService {
    @Value("${source.dir}")
    private  String source_dir;

    public boolean uploadAndExtractFiles(MultipartFile file) {
        String uploadPath=source_dir+"\\";
        if (file.isEmpty()) {
            // Handle empty file case
            return false;
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                String fileName = StringUtils.cleanPath(entry.getName());

                // If the entry is a file, transfer it to the static folder
                if (!entry.isDirectory()) {
                    Path outputPath = Paths.get(uploadPath, fileName);
                    File outputFile = outputPath.toFile();
                    outputFile.getParentFile().mkdirs();

                    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, len);
                        }
                    }
                } else {
                    // If the entry is a directory, create the corresponding directory in the static folder
                    Path dirPath = Paths.get(uploadPath, fileName);
                    Files.createDirectories(dirPath);
                }

                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }

            // File upload and extraction completed successfully
            return true;
        } catch (IOException e) {
            // Handle file read/write errors
            return false;
        }
    }
}
