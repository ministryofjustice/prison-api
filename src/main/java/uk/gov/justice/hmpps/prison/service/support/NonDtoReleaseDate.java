package uk.gov.justice.hmpps.prison.service.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.NonDtoReleaseDateType;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representation of a non-DTO release date with implementation of {@link Comparable} to facilitate priority ordering.
 */
@EqualsAndHashCode
@Getter
@ToString
public final class NonDtoReleaseDate implements Comparable<NonDtoReleaseDate> {
    private final NonDtoReleaseDateType releaseDateType;
    private final LocalDate releaseDate;
    private final boolean isOverride;

    public NonDtoReleaseDate(final NonDtoReleaseDateType releaseDateType, final LocalDate releaseDate, final boolean isOverride) {
        Objects.requireNonNull(releaseDateType, "A release date type must be defined.");
        Objects.requireNonNull(releaseDate, "A release date must be defined.");

        this.releaseDateType = releaseDateType;
        this.releaseDate = releaseDate;
        this.isOverride = isOverride;
    }

    @Override
    public int compareTo(final NonDtoReleaseDate otherNonDtoReleaseDate) {
        final var HIGHER_PRIORITY = -1;
        final var SAME_PRIORITY = 0;
        final var LOWER_PRIORITY = 1;

        // If equivalent, same priority.
        if (this == otherNonDtoReleaseDate) {
            return SAME_PRIORITY;
        }

        // If same type, override has priority.
        if (Objects.equals(this.releaseDateType, otherNonDtoReleaseDate.releaseDateType)
                && (this.isOverride ^ otherNonDtoReleaseDate.isOverride)) {

            return this.isOverride ? HIGHER_PRIORITY : LOWER_PRIORITY;
        }

        // Otherwise, later date has priority
        if (this.releaseDate.isAfter(otherNonDtoReleaseDate.releaseDate)) {
            return HIGHER_PRIORITY;
        } else if (this.releaseDate.isBefore(otherNonDtoReleaseDate.releaseDate)) {
            return LOWER_PRIORITY;
        }

        // At this point, both (or neither) are overrides with same release date.
        // Priority now determined by natural ordering of enumerated release date type.
        if (this.releaseDateType.compareTo(otherNonDtoReleaseDate.releaseDateType) < 0) {
            return HIGHER_PRIORITY;
        } else if (this.releaseDateType.compareTo(otherNonDtoReleaseDate.releaseDateType) > 0) {
            return LOWER_PRIORITY;
        }

        return SAME_PRIORITY;
    }
}
