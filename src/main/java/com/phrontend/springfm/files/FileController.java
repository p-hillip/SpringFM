package com.phrontend.springfm.files;

import com.phrontend.springfm.user.UserService;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final UserService userService;

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable("id") UUID id,
                                             @AuthenticationPrincipal String userId) {
        log.info("Download request: fileId={}, userId={}", id, userId);

        StoredFile file = fileService.requireById(id);
        log.info("File found: filename={}, size={}, contentType={}",
            file.getFilename(), file.getFileSize(), file.getContentType());

        Resource resource = fileService.loadAsResource(file);
        log.info("Resource loaded from storage: exists={}, readable={}",
            resource.exists(), resource.isReadable());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.getContentType() != null && !file.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(file.getContentType());
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getFilename())
                .build();

        log.info("Sending file: filename={}, mediaType={}", file.getFilename(), mediaType);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(file.getFileSize())
                .body(resource);
    }

    @PostMapping("/upload")
    @PreAuthorize("@authService.currentUser(#userId).canUpload()")
    public FileResult upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) FileCategory category,
            @RequestParam(value = "metadataText", required = false) String metadataText,
            @AuthenticationPrincipal String userId
    ) throws IOException {
        log.info("Upload request received: userId={}, filename={}, size={}, contentType={}",
            userId, file.getOriginalFilename(), file.getSize(), file.getContentType());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String uploadedBy = userId;
        StoredFile storedFile = fileService.upload(file, title, category, metadataText, uploadedBy);

        // Fetch display name for the uploaded file
        String displayName = "Unknown User";
        try {
            Long userIdLong = Long.parseLong(userId);
            displayName = userService.requireById(userIdLong).getDisplayName();
        } catch (Exception e) {
            log.warn("Could not fetch display name for user {}: {}", userId, e.getMessage());
        }

        return FileResult.fromEntity(storedFile, displayName);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id,
                                       @AuthenticationPrincipal String userId) throws IOException {
        log.info("Delete request: fileId={}, userId={}", id, userId);

        if (userId == null) {
            log.warn("Unauthenticated delete attempt for file {}", id);
            return ResponseEntity.status(401).build();
        }

        fileService.delete(id, userId);
        log.info("File deleted successfully: id={}", id);

        return ResponseEntity.noContent().build();
    }
}
