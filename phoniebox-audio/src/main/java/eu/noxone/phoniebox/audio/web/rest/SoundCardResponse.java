package eu.noxone.phoniebox.audio.web.rest;

import eu.noxone.phoniebox.audio.application.SoundCard;

public record SoundCardResponse(String name, String description, boolean selected) {
  public static SoundCardResponse from(final SoundCard card, final String selectedMixerName) {
    return new SoundCardResponse(
        card.name(), card.description(), card.name().equals(selectedMixerName));
  }
}
