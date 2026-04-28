package eu.noxone.phoniebox.audio.infrastructure.sound;

import eu.noxone.phoniebox.audio.application.SoundCard;
import eu.noxone.phoniebox.audio.application.port.out.SoundCardDiscoveryPort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

@ApplicationScoped
public class JavaxSoundCardDiscoveryAdapter implements SoundCardDiscoveryPort {

  @Override
  public List<SoundCard> discoverSoundCards() {
    return Arrays.stream(AudioSystem.getMixerInfo())
        .filter(info -> AudioSystem.getMixer(info).getSourceLineInfo().length > 0)
        .filter(this::mixerCanPlayAudio)
        .map(info -> new SoundCard(info.getName(), info.getDescription()))
        .toList();
  }

  private boolean mixerCanPlayAudio(Mixer.Info mixerInfo) {
    return Arrays.stream(AudioSystem.getMixer(mixerInfo).getSourceLines())
        .anyMatch(this::lineCanPlayAudio);
  }

  private boolean lineCanPlayAudio(Line line) {
    var lineInfo = line.getLineInfo();
    return lineInfo.getLineClass() == SourceDataLine.class;
  }
}
