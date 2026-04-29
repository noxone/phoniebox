package eu.noxone.phoniebox.audio.application.port.out;

import eu.noxone.phoniebox.audio.application.SoundCard;
import java.util.List;

public interface SoundCardDiscoveryPort {
  List<SoundCard> discoverSoundCards();
}
