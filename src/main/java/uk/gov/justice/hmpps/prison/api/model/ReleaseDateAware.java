package uk.gov.justice.hmpps.prison.api.model;

import java.time.LocalDate;

public interface ReleaseDateAware {
    Long getBookingId();
    void setReleaseDate(LocalDate releaseDate);
}
