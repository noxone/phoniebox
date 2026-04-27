package eu.noxone.phoniebox.media.web.rest;

import eu.noxone.phoniebox.media.domain.model.audiostream.AudioStream;

import java.util.UUID;

public record AudioStreamResponse(UUID id, String name, String url, String mimeType) {

    public static AudioStreamResponse from(final AudioStream stream) {
        return new AudioStreamResponse(
                stream.getId().getValue(),
                stream.getName().getValue(),
                stream.getUrl().getValue(),
                stream.getMimeType());
    }
}
