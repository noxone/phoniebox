package eu.noxone.phoniebox.media.domain.model;

import eu.noxone.phoniebox.shared.domain.DefaultDomainAttribute;

import java.time.Instant;

/** The point in time at which a media file's metadata was last edited (or uploaded if never edited). */
public final class UpdatedAt extends DefaultDomainAttribute<Instant> {

    private UpdatedAt(final Instant value) {
        super(value);
    }

    public static UpdatedAt now() {
        return new UpdatedAt(Instant.now());
    }

    public static UpdatedAt of(final Instant value) {
        return new UpdatedAt(value);
    }
}
