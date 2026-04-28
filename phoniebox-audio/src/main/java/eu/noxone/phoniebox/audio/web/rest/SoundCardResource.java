package eu.noxone.phoniebox.audio.web.rest;

import eu.noxone.phoniebox.audio.application.AudioSettingKeys;
import eu.noxone.phoniebox.audio.application.port.in.ListSoundCardsUseCase;
import eu.noxone.phoniebox.settings.application.port.in.GetSettingUseCase;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingCommand;
import eu.noxone.phoniebox.settings.application.port.in.SetSettingUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * REST resource for audio output device discovery and selection.
 *
 * <pre>
 * GET /api/audio/sound-cards          → list available output devices (with selected flag)
 * PUT /api/audio/sound-cards/selected → choose an output device (or null for system default)
 * </pre>
 */
@Path("/api/audio/sound-cards")
@Produces(MediaType.APPLICATION_JSON)
public class SoundCardResource {

  private final ListSoundCardsUseCase listUseCase;
  private final GetSettingUseCase getSetting;
  private final SetSettingUseCase setSetting;

  @Inject
  public SoundCardResource(
      final ListSoundCardsUseCase listUseCase,
      final GetSettingUseCase getSetting,
      final SetSettingUseCase setSetting) {
    this.listUseCase = listUseCase;
    this.getSetting = getSetting;
    this.setSetting = setSetting;
  }

  @GET
  public List<SoundCardResponse> listSoundCards() {
    String selected = getSetting.getSetting(AudioSettingKeys.SELECTED_MIXER_NAME).orElse(null);
    return listUseCase.listSoundCards().stream()
        .map(card -> SoundCardResponse.from(card, selected))
        .toList();
  }

  @PUT
  @Path("/selected")
  @Consumes(MediaType.APPLICATION_JSON)
  public List<SoundCardResponse> selectSoundCard(final SelectSoundCardRequest request) {
    setSetting.setSetting(
        new SetSettingCommand(AudioSettingKeys.SELECTED_MIXER_NAME, request.mixerName()));
    String selected = getSetting.getSetting(AudioSettingKeys.SELECTED_MIXER_NAME).orElse(null);
    return listUseCase.listSoundCards().stream()
        .map(card -> SoundCardResponse.from(card, selected))
        .toList();
  }
}
