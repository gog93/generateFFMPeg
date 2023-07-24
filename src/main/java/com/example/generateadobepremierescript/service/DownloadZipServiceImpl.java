package com.example.generateadobepremierescript.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
@Service
public class DownloadZipServiceImpl {
        public static void zip(String packageName) {
            String sourceDir =packageName;
            String zipFile =packageName+ ".zip";

            try {
                zipDirectory(sourceDir, zipFile);
                System.out.println("ZIP file created successfully.");
            } catch (IOException e) {
                System.out.println("Error creating ZIP file: " + e.getMessage());
            }
        }

        private static void zipDirectory(String sourceDir, String zipFile) throws IOException {
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            File directory = new File(sourceDir);
            zipFiles(directory, directory.getName(), zos);

            zos.close();
            fos.close();
        }

        private static void zipFiles(File directory, String baseName, ZipOutputStream zos) throws IOException {
            File[] files = directory.listFiles();
            byte[] buffer = new byte[1024];
            int bytesRead;

            for (File file : files) {
                if (file.isDirectory()) {
                    zipFiles(file, baseName + File.separator + file.getName(), zos);
                } else {
                    FileInputStream fis = new FileInputStream(file);
                    zos.putNextEntry(new ZipEntry(baseName + File.separator + file.getName()));

                    while ((bytesRead = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, bytesRead);
                    }

                    fis.close();
                }
            }
        }
}