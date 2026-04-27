package eu.noxone.phoniebox.media.application.port.in;

import java.util.UUID;

/**
 * Carries the editable tag fields for a single media file. Null values clear the corresponding tag;
 * non-null values replace it.
 */
public record UpdateTagsCommand(UUID id, String title, String artist, String album, String genre) {}
