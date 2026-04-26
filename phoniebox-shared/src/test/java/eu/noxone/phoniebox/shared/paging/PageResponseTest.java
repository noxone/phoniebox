package eu.noxone.phoniebox.shared.paging;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResponseTest {

    // ── totalPages ────────────────────────────────────────────────────────────

    @Test
    void totalPages_returnsZero_whenNoElements() {
        assertEquals(0, page(0, 10, 0).totalPages());
    }

    @Test
    void totalPages_returnsOne_whenFewerElementsThanPageSize() {
        assertEquals(1, page(0, 10, 5).totalPages());
    }

    @Test
    void totalPages_returnsOne_whenElementsEqualPageSize() {
        assertEquals(1, page(0, 10, 10).totalPages());
    }

    @Test
    void totalPages_returnsTwo_whenOneMoreThanPageSize() {
        assertEquals(2, page(0, 10, 11).totalPages());
    }

    // ── isFirst ───────────────────────────────────────────────────────────────

    @Test
    void isFirst_returnsTrueOnPageZero() {
        assertTrue(page(0, 10, 50).isFirst());
    }

    @Test
    void isFirst_returnsFalseOnLaterPage() {
        assertFalse(page(1, 10, 50).isFirst());
    }

    // ── isLast ────────────────────────────────────────────────────────────────

    @Test
    void isLast_returnsTrueWhenNoElements() {
        assertTrue(page(0, 10, 0).isLast());
    }

    @Test
    void isLast_returnsTrueOnSinglePage() {
        assertTrue(page(0, 10, 5).isLast());
    }

    @Test
    void isLast_returnsTrueOnFinalPage() {
        // 25 elements, size 10 → 3 pages (0,1,2); page 2 is last
        assertTrue(page(2, 10, 25).isLast());
    }

    @Test
    void isLast_returnsFalseOnFirstPage_whenMultiplePagesExist() {
        assertFalse(page(0, 10, 25).isLast());
    }

    // ── hasNext ───────────────────────────────────────────────────────────────

    @Test
    void hasNext_returnsTrueWhenMorePagesExist() {
        assertTrue(page(0, 10, 25).hasNext());
    }

    @Test
    void hasNext_returnsFalseOnLastPage() {
        assertFalse(page(2, 10, 25).hasNext());
    }

    @Test
    void hasNext_returnsFalseWhenEmpty() {
        assertFalse(page(0, 10, 0).hasNext());
    }

    // ── hasPrevious ───────────────────────────────────────────────────────────

    @Test
    void hasPrevious_returnsFalseOnFirstPage() {
        assertFalse(page(0, 10, 50).hasPrevious());
    }

    @Test
    void hasPrevious_returnsTrueOnLaterPage() {
        assertTrue(page(1, 10, 50).hasPrevious());
    }

    // ── map ───────────────────────────────────────────────────────────────────

    @Test
    void map_transformsContent() {
        final var ints = new PageResponse<>(List.of(1, 2, 3), 0, 10, 3);
        assertEquals(List.of("1", "2", "3"), ints.map(Object::toString).content());
    }

    @Test
    void map_preservesPaginationMetadata() {
        final var original = new PageResponse<>(List.of("x"), 3, 10, 42);
        final var mapped = original.map(String::length);
        assertEquals(3, mapped.page());
        assertEquals(10, mapped.size());
        assertEquals(42, mapped.totalElements());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private static PageResponse<Void> page(final int page, final int size, final long totalElements) {
        return new PageResponse<>(List.of(), page, size, totalElements);
    }
}
