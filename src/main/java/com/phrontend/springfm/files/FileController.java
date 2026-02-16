package com.phrontend.springfm.files;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable("id") UUID id) {
        StoredFile file = fileService.requireById(id);
        Resource resource = fileService.loadAsResource(file);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.getContentType() != null && !file.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(file.getContentType());
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getFilename())
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(file.getFileSize())
                .body(resource);
    }
}
