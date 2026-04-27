package eu.noxone.phoniebox.media.domain.model.mediafile;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

/** The original file name as supplied by the uploader. */
public final class OriginalFileName extends DefaultDomainAttribute<String> {

  private OriginalFileName(final String value) {
    super(value);
  }

  public static OriginalFileName of(final String value) {
    return new OriginalFileName(value);
  }
}
