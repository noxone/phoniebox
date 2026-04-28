package eu.noxone.phoniebox.audio.application.port.in;

import eu.noxone.phoniebox.audio.application.SoundCard;
import java.util.List;

public interface ListSoundCardsUseCase {
  List<SoundCard> listSoundCards();
}
