package com.example.fileuploadauth.repository;

import com.example.fileuploadauth.model.StoredFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    List<StoredFile> findByOwnerUsernameOrderByUploadedAtDesc(String username);

    Optional<StoredFile> findByIdAndOwnerUsername(Long id, String username);
}
