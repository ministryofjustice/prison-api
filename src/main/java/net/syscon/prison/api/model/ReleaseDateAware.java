package net.syscon.prison.api.model;

import java.time.LocalDate;

public interface ReleaseDateAware {
    Long getBookingId();
    void setReleaseDate(LocalDate releaseDate);
}
