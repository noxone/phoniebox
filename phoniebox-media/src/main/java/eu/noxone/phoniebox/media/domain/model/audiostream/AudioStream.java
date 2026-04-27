package eu.noxone.phoniebox.media.domain.model.audiostream;

import eu.noxone.phoniebox.media.domain.model.shared.MimeType;
import eu.noxone.phoniebox.shared.domain.DefaultDomainEntity;
import eu.noxone.phoniebox.shared.domain.Playable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "audio_streams")
public class AudioStream extends DefaultDomainEntity<AudioStreamId> implements Playable {

  public static final String KIND = "AUDIO_STREAM";

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private AudioStreamId id;

  @Column(name = "name", nullable = false)
  private StreamName name;

  @Column(name = "url", nullable = false)
  private StreamUrl url;

  @Column(name = "mime_type", nullable = false)
  private MimeType mimeType;

  protected AudioStream() {}

  private AudioStream(
      final AudioStreamId id, final StreamName name, final StreamUrl url, final MimeType mimeType) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.name = Objects.requireNonNull(name, "name must not be null");
    this.url = Objects.requireNonNull(url, "url must not be null");
    this.mimeType = Objects.requireNonNull(mimeType, "mimeType must not be null");
  }

  public static AudioStream create(
      final StreamName name, final StreamUrl url, final MimeType mimeType) {
    return new AudioStream(AudioStreamId.newId(), name, url, mimeType);
  }

  @Override
  public AudioStreamId getId() {
    return id;
  }

  public StreamName getName() {
    return name;
  }

  public StreamUrl getUrl() {
    return url;
  }

  public MimeType getMimeTypeAttribute() {
    return mimeType;
  }

  // ── Playable ──────────────────────────────────────────────────────────────

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
    return mimeType.getValue();
  }
}
