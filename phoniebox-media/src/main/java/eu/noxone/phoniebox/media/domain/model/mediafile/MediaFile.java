package eu.noxone.phoniebox.media.domain.model.mediafile;

import eu.noxone.phoniebox.media.domain.model.audio.AudioMetadata;
import eu.noxone.phoniebox.media.domain.model.audio.TrackAlbum;
import eu.noxone.phoniebox.media.domain.model.audio.TrackArtist;
import eu.noxone.phoniebox.media.domain.model.audio.TrackGenre;
import eu.noxone.phoniebox.media.domain.model.audio.TrackTitle;
import eu.noxone.phoniebox.shared.domain.DefaultDomainEntity;
import eu.noxone.phoniebox.shared.domain.Playable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root representing an uploaded sound file.
 *
 * <p>The physical bytes are stored separately (managed by {@code FileStoragePort}). This entity
 * holds only identity and descriptive metadata.
 *
 * <p>This class is both a domain entity (extends {@link DefaultDomainEntity}) and a JPA entity
 * ({@code @Entity}). The JPA annotations are considered acceptable in the domain layer because they
 * are declarative metadata from the standard Jakarta Persistence API — they carry no Quarkus or
 * Hibernate-specific behaviour. Attribute type conversion is handled by {@code @AttributeConverter}
 * classes and Hibernate {@code UserType} implementations in the infrastructure layer — registered
 * globally via {@code @TypeRegistration} in {@code package-info.java} — so this class carries no
 * references to the infrastructure layer.
 *
 * <p>Fields are non-final to allow Hibernate to populate them after constructing the instance via
 * the protected no-arg constructor. The domain factory method {@link #create} remains the only
 * public way to create a new aggregate.
 */
@Entity
@Table(name = "media_files")
public class MediaFile extends DefaultDomainEntity<MediaFileId> implements Playable {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private MediaFileId id;

  @Embedded private MediaFileMetadata metadata;

  @Column(name = "uploaded_at", nullable = false)
  private UploadedAt uploadedAt;

  @Column(name = "updated_at", nullable = false)
  private UpdatedAt updatedAt;

  @Embedded private AudioMetadata audioMetadata;

  /** Required by JPA. Not for use by application code. */
  protected MediaFile() {}

  private MediaFile(
      final MediaFileId id, final MediaFileMetadata metadata, final UploadedAt uploadedAt) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
    this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt must not be null");
    this.updatedAt = UpdatedAt.of(uploadedAt.getValue());
  }

  /**
   * Creates a brand-new {@code MediaFile} with a fresh {@link MediaFileId} and the current
   * timestamp.
   */
  public static MediaFile create(final MediaFileMetadata metadata) {
    return new MediaFile(MediaFileId.newId(), metadata, UploadedAt.now());
  }

  @Override
  public MediaFileId getId() {
    return id;
  }

  public MediaFileMetadata getMetadata() {
    return metadata;
  }

  public UploadedAt getUploadedAt() {
    return uploadedAt;
  }

  public UpdatedAt getUpdatedAt() {
    return updatedAt;
  }

  /** Returns extracted audio metadata, or empty if extraction was not attempted or failed. */
  public Optional<AudioMetadata> getAudioMetadata() {
    return Optional.ofNullable(audioMetadata);
  }

  // ── Playable ──────────────────────────────────────────────────────────────

  public static final String KIND = "MEDIA_FILE";

  @Override
  public String getPlayableKind() {
    return KIND;
  }

  @Override
  public UUID getPlayableId() {
    return id.getValue();
  }

  @Override
  public String getMimeType() {
    return metadata.getMimeType().getValue();
  }

  /** Stores audio metadata extracted after the file was persisted. */
  public void applyAudioMetadata(final AudioMetadata audioMetadata) {
    this.audioMetadata = audioMetadata;
  }

  /**
   * Updates the editable tag fields (title, artist, album, genre) and sets {@link #updatedAt} to
   * now. Technical fields (duration, bitrate, sample rate, track number, year) are preserved if
   * present. Null values clear the corresponding tag.
   */
  public void updateTagMetadata(
      final TrackTitle title,
      final TrackArtist artist,
      final TrackAlbum album,
      final TrackGenre genre) {
    this.audioMetadata =
        audioMetadata != null
            ? audioMetadata.withTags(title, artist, album, genre)
            : new AudioMetadata(null, null, null, title, artist, album, null, null, genre);
    this.updatedAt = UpdatedAt.now();
  }
}
