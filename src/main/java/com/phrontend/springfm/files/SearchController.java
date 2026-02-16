package com.phrontend.springfm.files;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "title", "title",
            "filename", "filename",
            "category", "category",
            "uploadedAt", "uploadedAt",
            "uploadedBy", "uploadedBy",
            "fileSize", "fileSize"
    );

    private final FileSearchService fileSearchService;

    @GetMapping
    public SearchResponse search(@RequestParam(value = "q", required = false) String query,
                                 @RequestParam(value = "categories", required = false) List<FileCategory> categories,
                                 @RequestParam(value = "sortField", defaultValue = "uploadedAt") String sortField,
                                 @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "20") int size) {
        String mappedSortField = SORT_FIELDS.getOrDefault(sortField, "uploadedAt");
        Sort.Direction direction = sortDir == null || sortDir.toLowerCase(Locale.ROOT).startsWith("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, mappedSortField));

        Page<StoredFile> results = fileSearchService.search(query, categories, pageRequest);
        List<FileResult> mapped = results.getContent().stream()
                .map(FileResult::fromEntity)
                .toList();

        return new SearchResponse(mapped, results.getTotalElements(), results.getNumber(), results.getSize());
    }

    @GetMapping("/suggest")
    public SearchSuggestionResponse suggest(@RequestParam(value = "q", required = false) String query,
                                            @RequestParam(value = "categories", required = false) List<FileCategory> categories) {
        return new SearchSuggestionResponse(fileSearchService.suggest(query, categories));
    }
}
