package com.example.fileuploadauth.service;

import com.example.fileuploadauth.config.StorageProperties;
import com.example.fileuploadauth.dto.FileResponse;
import com.example.fileuploadauth.model.AppUser;
import com.example.fileuploadauth.model.StoredFile;
import com.example.fileuploadauth.repository.StoredFileRepository;
import com.example.fileuploadauth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadRoot;
    private final UserRepository userRepository;
    private final StoredFileRepository storedFileRepository;

    public FileStorageService(
            StorageProperties storageProperties,
            UserRepository userRepository,
            StoredFileRepository storedFileRepository
    ) {
        this.uploadRoot = Path.of(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        this.userRepository = userRepository;
        this.storedFileRepository = storedFileRepository;
    }

    @PostConstruct
    void createUploadRoot() throws IOException {
        Files.createDirectories(uploadRoot);
    }

    @Transactional
    public FileResponse store(MultipartFile file, String username) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()
        );
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String storedFilename = UUID.randomUUID() + "-" + originalFilename;
        Path target = uploadRoot.resolve(storedFilename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file", ex);
        }

        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        StoredFile storedFile = new StoredFile(
                originalFilename,
                storedFilename,
                file.getContentType(),
                file.getSize(),
                Instant.now(),
                owner
        );
        return FileResponse.from(storedFileRepository.save(storedFile));
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listFiles(String username) {
        return storedFileRepository.findByOwnerUsernameOrderByUploadedAtDesc(username)
                .stream()
                .map(FileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoredFile getFileMetadata(Long id, String username) {
        return storedFileRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    public Resource loadAsResource(StoredFile file) {
        try {
            Path target = uploadRoot.resolve(file.getStoredFilename()).normalize();
            Resource resource = new UrlResource(target.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new IllegalStateException("File is not readable");
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("File is not readable", ex);
        }
    }
}
