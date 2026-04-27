package eu.noxone.phoniebox.audio.application;

/** Thrown when audio playback fails due to an unreadable stream or unavailable sound device. */
public class AudioPlaybackException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public AudioPlaybackException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
