package net.syscon.elite.api.model;

import java.time.LocalDate;

public interface ReleaseDateAware {
    Long getBookingId();
    void setReleaseDate(LocalDate releaseDate);
}
