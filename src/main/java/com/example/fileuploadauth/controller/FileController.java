package com.example.fileuploadauth.controller;

import com.example.fileuploadauth.dto.FileResponse;
import com.example.fileuploadauth.model.StoredFile;
import com.example.fileuploadauth.service.FileStorageService;
import java.security.Principal;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileResponse upload(@RequestParam("file") MultipartFile file, Principal principal) {
        return fileStorageService.store(file, principal.getName());
    }

    @GetMapping
    public List<FileResponse> list(Principal principal) {
        return fileStorageService.listFiles(principal.getName());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id, Principal principal) {
        StoredFile file = fileStorageService.getFileMetadata(id, principal.getName());
        Resource resource = fileStorageService.loadAsResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType()
                ))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getOriginalFilename())
                        .build()
                        .toString())
                .body(resource);
    }
}
