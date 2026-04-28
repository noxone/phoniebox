package eu.noxone.phoniebox.audio.application.service;

import eu.noxone.phoniebox.audio.application.SoundCard;
import eu.noxone.phoniebox.audio.application.port.in.ListSoundCardsUseCase;
import eu.noxone.phoniebox.audio.application.port.out.SoundCardDiscoveryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class SoundCardApplicationService implements ListSoundCardsUseCase {

  private final SoundCardDiscoveryPort discoveryPort;

  @Inject
  public SoundCardApplicationService(final SoundCardDiscoveryPort discoveryPort) {
    this.discoveryPort = discoveryPort;
  }

  @Override
  public List<SoundCard> listSoundCards() {
    return discoveryPort.discoverSoundCards();
  }
}
