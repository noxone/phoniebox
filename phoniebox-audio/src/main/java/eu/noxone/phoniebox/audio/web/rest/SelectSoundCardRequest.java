package eu.noxone.phoniebox.audio.web.rest;

/** {@code mixerName} is {@code null} to revert to the system default output device. */
public record SelectSoundCardRequest(String mixerName) {}
