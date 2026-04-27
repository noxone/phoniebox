package eu.noxone.phoniebox.audio.application;

public enum PlaybackStatus {
  /** No track selected or playback has ended. */
  IDLE,
  /** A track is actively playing. */
  PLAYING,
  /** Playback is paused mid-track. */
  PAUSED
}
