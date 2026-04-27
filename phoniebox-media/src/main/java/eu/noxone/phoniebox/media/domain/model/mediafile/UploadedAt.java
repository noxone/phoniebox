package eu.noxone.phoniebox.media.domain.model.mediafile;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

import java.time.Instant;

/** The point in time at which a media file was uploaded. */
public final class UploadedAt extends DefaultDomainAttribute<Instant> {

    private UploadedAt(final Instant value) {
        super(value);
    }

    /** Captures the current instant as the upload timestamp. */
    public static UploadedAt now() {
        return new UploadedAt(Instant.now());
    }

    public static UploadedAt of(final Instant value) {
        return new UploadedAt(value);
    }
}
