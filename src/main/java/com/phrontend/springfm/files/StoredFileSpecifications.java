package com.phrontend.springfm.files;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class StoredFileSpecifications {

    private StoredFileSpecifications() {
    }

    public static Specification<StoredFile> matchesQuery(String query) {
        if (query == null || query.isBlank()) {
            return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        String like = "%" + query.toLowerCase() + "%";
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), like),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("filename")), like),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("metadataText")), like)
        );
    }

    public static Specification<StoredFile> categoryIn(List<FileCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, criteriaQuery, criteriaBuilder) -> root.get("category").in(categories);
    }
}
