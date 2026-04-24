package eu.noxone.phoniebox.media.infrastructure.persistence;

import eu.noxone.phoniebox.media.domain.model.MediaFileId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MediaFileIdConverterTest {

    private static final UUID FIXED_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private final MediaFileIdConverter converter = new MediaFileIdConverter();

    @Test
    void convertToDatabaseColumn_returnsUuidString() {
        assertEquals("123e4567-e89b-12d3-a456-426614174000",
                converter.convertToDatabaseColumn(MediaFileId.of(FIXED_UUID)));
    }

    @Test
    void convertToDatabaseColumn_nullReturnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_parsesUuidString() {
        assertEquals(MediaFileId.of(FIXED_UUID),
                converter.convertToEntityAttribute("123e4567-e89b-12d3-a456-426614174000"));
    }

    @Test
    void convertToEntityAttribute_nullReturnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void roundTrip() {
        MediaFileId original = MediaFileId.of(FIXED_UUID);
        assertEquals(original, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original)));
    }
}
