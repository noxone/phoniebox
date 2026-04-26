package eu.noxone.phoniebox.shared.paging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageRequestTest {

    @Test
    void constructor_setsPageAndSize() {
        final var req = new PageRequest(2, 50);
        assertEquals(2, req.page());
        assertEquals(50, req.size());
    }

    @Test
    void constructor_throwsForNegativePage() {
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(-1, 10));
    }

    @Test
    void constructor_throwsForZeroSize() {
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(0, 0));
    }

    @Test
    void constructor_throwsForNegativeSize() {
        assertThrows(IllegalArgumentException.class, () -> new PageRequest(0, -5));
    }

    @Test
    void constructor_clampsOversizedSizeToMaxSize() {
        final var req = new PageRequest(0, PageRequest.MAX_SIZE + 1);
        assertEquals(PageRequest.MAX_SIZE, req.size());
    }

    @Test
    void constructor_acceptsExactlyMaxSize() {
        final var req = new PageRequest(0, PageRequest.MAX_SIZE);
        assertEquals(PageRequest.MAX_SIZE, req.size());
    }

    @Test
    void of_createsEquivalentRequest() {
        final var req = PageRequest.of(3, 25);
        assertEquals(3, req.page());
        assertEquals(25, req.size());
    }

    @Test
    void first_returnsPageZeroWithMaxSize() {
        final var req = PageRequest.first();
        assertEquals(0, req.page());
        assertEquals(PageRequest.MAX_SIZE, req.size());
    }
}
