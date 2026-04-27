package eu.noxone.phoniebox.media.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import eu.noxone.phoniebox.media.domain.model.mediafile.MediaFileId;
import java.sql.Types;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MediaFileIdUserTypeTest {

  private static final UUID FIXED_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final String FIXED_STRING = "123e4567-e89b-12d3-a456-426614174000";

  private final MediaFileIdUserType userType = new MediaFileIdUserType();

  @Test
  void getSqlType_returnsVarchar() {
    assertEquals(Types.VARCHAR, userType.getSqlType());
  }

  @Test
  void returnedClass_isMediaFileId() {
    assertEquals(MediaFileId.class, userType.returnedClass());
  }

  @Test
  void isMutable_returnsFalse() {
    assertFalse(userType.isMutable());
  }

  @Test
  void deepCopy_returnsSameInstance() {
    MediaFileId id = MediaFileId.of(FIXED_UUID);
    assertSame(id, userType.deepCopy(id));
  }

  @Test
  void disassemble_returnsUuidString() {
    MediaFileId id = MediaFileId.of(FIXED_UUID);
    assertEquals(FIXED_STRING, userType.disassemble(id));
  }

  @Test
  void disassemble_nullReturnsNull() {
    assertNull(userType.disassemble(null));
  }

  @Test
  void assemble_parsesUuidString() {
    assertEquals(MediaFileId.of(FIXED_UUID), userType.assemble(FIXED_STRING, null));
  }

  @Test
  void assemble_nullReturnsNull() {
    assertNull(userType.assemble(null, null));
  }

  @Test
  void roundTrip() {
    MediaFileId original = MediaFileId.of(FIXED_UUID);
    assertEquals(original, userType.assemble(userType.disassemble(original), null));
  }
}
