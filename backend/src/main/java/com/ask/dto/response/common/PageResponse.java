package com.ask.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Standard paginated response wrapper.
 * Used by every list endpoint to provide consistent pagination metadata.
 *
 * @param <T> the type of items in the page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /** The list of items for the current page */
    private List<T> content;

    /** Current page number (0-indexed) */
    private int pageNumber;

    /** Number of items per page */
    private int pageSize;

    /** Total number of items across all pages */
    private long totalElements;

    /** Total number of pages */
    private int totalPages;

    /** Whether this is the last page */
    private boolean last;

    /** Whether this is the first page */
    private boolean first;

    /**
     * Creates a PageResponse from a Spring Data Page object.
     *
     * @param page   the Spring Data page
     * @param content the mapped content list (DTOs, not entities)
     * @param <T>    the DTO type
     * @return a PageResponse wrapping the page metadata and content
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<?> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
