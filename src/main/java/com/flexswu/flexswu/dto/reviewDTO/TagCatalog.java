package com.flexswu.flexswu.dto.reviewDTO;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TagCatalog {
    private TagCatalog() {}

    // 1..16 허용
    public static final Set<Integer> VALID_CODES =
            IntStream.rangeClosed(1, 16).boxed().collect(Collectors.toUnmodifiableSet());
}
