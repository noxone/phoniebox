package eu.noxone.phoniebox.shared.domain;

import java.util.UUID;

/**
 * Marker interface for anything the audio module can play back.
 *
 * <p>Implementations provide the identity and MIME-type hint needed to locate
 * and decode the audio data.  The actual byte stream is resolved separately
 * by the audio module through its {@code AudioStreamPort} outbound port, keeping
 * this interface free of I/O concerns and usable in any layer.
 *
 * <p>Intended to be implemented by domain entities in feature modules (e.g.
 * {@code MediaFile}) so that the audio module remains decoupled from those modules.
 */
public interface Playable {

    /** Unique identifier used by the audio module to locate the audio data. */
    UUID getPlayableId();

    /** MIME type of the audio content (e.g. {@code "audio/mpeg"}, {@code "audio/wav"}). */
    String getMimeType();
}
