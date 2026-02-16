package com.phrontend.springfm.files;

import java.util.List;

public record SearchResponse(
        List<FileResult> results,
        long total,
        int page,
        int size
) {
}
