package com.example.generateadobepremierescript.controller;

import com.example.generateadobepremierescript.repository.TextRepository;
import com.example.generateadobepremierescript.model.Text;
import com.example.generateadobepremierescript.service.DownloadZipServiceImpl;
import com.example.generateadobepremierescript.service.TextServiceImpl;
import com.example.generateadobepremierescript.service.ZipUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/adobe")
public class TextController {
    private final TextServiceImpl textService;
    private final TextRepository textRepository;
    private final ZipUploadService zipUploadService;

    @GetMapping()
    public String adobe(ModelMap model) {
        model.addAttribute("text", new Text());
        return "index";
    }

    @GetMapping("/download/{id}")
    public String download(ModelMap model, @PathVariable("id") Long id) {
        Text byId = textRepository.findById(id).get();
        model.addAttribute("text", byId);
        return "downloadPage";
    }

    @PostMapping()
    public String sendText(Model modelMap, @ModelAttribute(value = "text") Text text,
                           @RequestParam(value = "zip") MultipartFile zip) {
        modelMap.addAttribute("text1", "barev");
        zipUploadService.uploadAndExtractFiles(zip);

        modelMap.addAttribute("model", text);
        String packageName = textService.write("text.txt", text.getDescription());
        DownloadZipServiceImpl.zip(packageName);
        text.setPackageName(packageName);
        textRepository.save(text);
        modelMap.addAttribute("text", text);
        return "redirect:/adobe/download/" + text.getId();
    }

    @GetMapping("/downloadPackage/{id}")
    public ResponseEntity<FileSystemResource> downloadFiles(@PathVariable("id") Long id,
                                                            @RequestParam("package") String packageName) throws IOException {
//        String filePath = packageName + ".zip";
        String filePath = packageName + ".zip";

        // Create a FileSystemResource with the file path
        FileSystemResource fileResource = new FileSystemResource(filePath);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileResource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);

    }

    @GetMapping("/downloadFile/{id}")
    public ResponseEntity<FileSystemResource> downloadFile(@PathVariable("id") Long id,
                                                           @RequestParam("package") String packageName) throws IOException {
        String filePath = "";
        File directory = new File(packageName);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Error: The specified directory does not exist or is not a directory.");
        }

        File[] files = directory.listFiles();
        if (files == null) {
            System.out.println("Error: Failed to retrieve files from the directory.");
        }

        for (File file : files) {
            if (file.isFile()) {
                String videoName = file.getName();

                if (videoName.startsWith("concat")) {
                    filePath += packageName + "/" + videoName;

                    FileSystemResource fileResource = new FileSystemResource(filePath);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath + "\"");
                    return ResponseEntity.ok()
                            .headers(headers)
                            .contentLength(fileResource.contentLength())
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(fileResource);
                }
            }
        }
     return (ResponseEntity<FileSystemResource>) ResponseEntity.notFound();


    }
}
