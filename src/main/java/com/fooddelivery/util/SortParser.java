package com.fooddelivery.util;

import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import java.util.Set;
import org.springframework.data.domain.Sort;

public final class SortParser {

    private static final Set<String> ALLOWED_PROPERTIES = Set.of("name", "id", "rating");

    private SortParser() {
    }

    public static Sort parse(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by("id").ascending();
        }
        String[] parts = sortParam.split(":");
        String property = parts[0].trim();
        if (!ALLOWED_PROPERTIES.contains(property)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Unsupported sort property: " + property);
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dir = parts[1].trim().toUpperCase();
            if ("DESC".equals(dir) || "DSC".equals(dir)) {
                direction = Sort.Direction.DESC;
            } else if (!"ASC".equals(dir)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Unsupported sort direction: " + parts[1]);
            }
        }
        if ("rating".equals(property)) {
            return Sort.by(direction, "ratingSum");
        }
        return Sort.by(direction, property);
    }
}
