package com.example.fileuploadauth.dto;

import com.example.fileuploadauth.model.StoredFile;
import java.time.Instant;

public record FileResponse(
        Long id,
        String originalFilename,
        String contentType,
        long size,
        Instant uploadedAt
) {

    public static FileResponse from(StoredFile file) {
        return new FileResponse(
                file.getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getUploadedAt()
        );
    }
}
