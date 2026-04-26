package eu.noxone.phoniebox.shared.paging;

/**
 * Paging parameters for list queries. {@code page} is 0-based; {@code size} is clamped to
 * {@link #MAX_SIZE} if the caller requests more.
 */
public record PageRequest(int page, int size) {

    public static final int MAX_SIZE = 1000;

    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page must not be negative");
        if (size < 1) throw new IllegalArgumentException("size must be at least 1");
        size = Math.min(size, MAX_SIZE);
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    public static PageRequest first() {
        return new PageRequest(0, MAX_SIZE);
    }
}
