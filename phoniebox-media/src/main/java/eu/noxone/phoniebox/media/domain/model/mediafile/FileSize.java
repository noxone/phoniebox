package eu.noxone.phoniebox.media.domain.model.mediafile;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

/** Size of a file in bytes. Must not be negative. */
public final class FileSize extends DefaultDomainAttribute<Long> {

    private FileSize(final long value) {
        super(value);
    }

    public static FileSize of(final long value) {
        if (value < 0) {
            throw new IllegalArgumentException("File size must not be negative, was: " + value);
        }
        return new FileSize(value);
    }
}
