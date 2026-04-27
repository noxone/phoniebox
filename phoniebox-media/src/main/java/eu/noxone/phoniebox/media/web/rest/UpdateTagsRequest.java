package eu.noxone.phoniebox.media.web.rest;

/**
 * JSON request body for {@code PATCH /api/media/{id}}. All fields are nullable: null clears the
 * tag, a non-null string replaces it.
 */
public record UpdateTagsRequest(
    String trackTitle, String trackArtist, String trackAlbum, String trackGenre) {}
