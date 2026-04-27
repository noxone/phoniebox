package eu.noxone.phoniebox.media.application.port.in;

/** Command to add a new internet radio stream. */
public record AddAudioStreamCommand(String name, String url, String mimeType) {
}
