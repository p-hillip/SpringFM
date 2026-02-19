package com.phrontend.springfm.files;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stored_files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory category;

    @Column(nullable = false)
    private Instant uploadedAt;

    @Column(nullable = false)
    private String uploadedBy;

    @Column(nullable = false)
    private long fileSize;

    @Column(name = "metadata_text", columnDefinition = "TEXT")
    private String metadataText;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "sha256")
    private String sha256;
}
