package com.phrontend.springfm.files;

import com.phrontend.springfm.user.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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
    private final UserService userService;

    @GetMapping
    public SearchResponse search(@RequestParam(value = "q", required = false) String query,
                                 @RequestParam(value = "categories", required = false) List<FileCategory> categories,
                                 @RequestParam(value = "sortField", defaultValue = "uploadedAt") String sortField,
                                 @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "size", defaultValue = "20") int size) {
        log.info("Search request: query='{}', categories={}, sortField={}, sortDir={}, page={}, size={}",
            query, categories, sortField, sortDir, page, size);

        // Convert 1-based page (from frontend) to 0-based page (for Spring Data)
        int zeroBasedPage = Math.max(0, page - 1);

        String mappedSortField = SORT_FIELDS.getOrDefault(sortField, "uploadedAt");
        Sort.Direction direction = sortDir == null || sortDir.toLowerCase(Locale.ROOT).startsWith("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(zeroBasedPage, size, Sort.by(direction, mappedSortField));

        Page<StoredFile> results = fileSearchService.search(query, categories, pageRequest);
        log.info("Search results: found {} total, returning page {} with {} results",
            results.getTotalElements(), results.getNumber(), results.getContent().size());

        // Fetch user display names for all unique uploaders
        Map<String, String> userDisplayNames = new HashMap<>();
        for (StoredFile file : results.getContent()) {
            String userId = file.getUploadedBy();
            if (!userDisplayNames.containsKey(userId)) {
                try {
                    Long userIdLong = Long.parseLong(userId);
                    String displayName = userService.requireById(userIdLong).getDisplayName();
                    userDisplayNames.put(userId, displayName);
                } catch (Exception e) {
                    log.warn("Could not fetch display name for user {}: {}", userId, e.getMessage());
                    userDisplayNames.put(userId, "Unknown User");
                }
            }
        }

        List<FileResult> mapped = results.getContent().stream()
                .map(file -> FileResult.fromEntity(file, userDisplayNames.get(file.getUploadedBy())))
                .toList();

        // Return 1-based page number to frontend
        return new SearchResponse(mapped, results.getTotalElements(), results.getNumber() + 1, results.getSize());
    }

    @GetMapping("/suggest")
    public SearchSuggestionResponse suggest(@RequestParam(value = "q", required = false) String query,
                                            @RequestParam(value = "categories", required = false) List<FileCategory> categories) {
        return new SearchSuggestionResponse(fileSearchService.suggest(query, categories));
    }
}
