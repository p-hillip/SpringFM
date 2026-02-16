package com.phrontend.springfm.files;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID>, JpaSpecificationExecutor<StoredFile> {
    Page<StoredFile> findByTitleContainingIgnoreCaseOrFilenameContainingIgnoreCase(String title,
                                                                                   String filename,
                                                                                   Pageable pageable);
}
