package com.phrontend.springfm.files;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileSearchService {

    private final StoredFileRepository storedFileRepository;

    @Transactional(readOnly = true)
    public Page<StoredFile> search(String query, List<FileCategory> categories, PageRequest pageRequest) {
        Specification<StoredFile> specification = Specification.where(StoredFileSpecifications.matchesQuery(query))
                .and(StoredFileSpecifications.categoryIn(categories));
        return storedFileRepository.findAll(specification, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<String> suggest(String query, List<FileCategory> categories) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Page<StoredFile> page = storedFileRepository
                .findByTitleContainingIgnoreCaseOrFilenameContainingIgnoreCase(
                        query,
                        query,
                        PageRequest.of(0, 25, Sort.by(Sort.Direction.DESC, "uploadedAt"))
                );

        Set<String> suggestions = new LinkedHashSet<>();
        for (StoredFile file : page.getContent()) {
            if (categories != null && !categories.isEmpty() && !categories.contains(file.getCategory())) {
                continue;
            }
            if (file.getTitle() != null && file.getTitle().toLowerCase().contains(query.toLowerCase())) {
                suggestions.add(file.getTitle());
            }
            if (file.getFilename() != null && file.getFilename().toLowerCase().contains(query.toLowerCase())) {
                suggestions.add(file.getFilename());
            }
            if (suggestions.size() >= 10) {
                break;
            }
        }

        return List.copyOf(suggestions);
    }
}
