package com.phrontend.springfm.files;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileService {

    private final StoredFileRepository storedFileRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public StoredFile requireById(UUID id) {
        return storedFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(StoredFile file) {
        return storageService.loadAsResource(file.getStoragePath());
    }
}
