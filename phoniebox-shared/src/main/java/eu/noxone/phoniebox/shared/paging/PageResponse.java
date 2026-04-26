package eu.noxone.phoniebox.shared.paging;

import java.util.List;
import java.util.function.Function;

/**
 * Generic page result carrying the current slice of items together with pagination metadata.
 *
 * <p>Use {@link #map(Function)} to convert content across layer boundaries without rebuilding
 * the metadata (e.g. domain entities → DTOs at the web layer).
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements) {

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return totalElements == 0 || page >= totalPages() - 1;
    }

    public boolean hasNext() {
        return !isLast();
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public <R> PageResponse<R> map(Function<T, R> mapper) {
        return new PageResponse<>(content.stream().map(mapper).toList(), page, size, totalElements);
    }
}
