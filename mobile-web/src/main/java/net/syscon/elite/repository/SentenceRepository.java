package net.syscon.elite.repository;

import net.syscon.elite.api.model.OffenceDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SentenceRepository {
    List<OffenceDetail> getMainOffenceDetails(Long bookingId);

    Optional<LocalDate> getConfirmedReleaseDate(Long bookingId);
}
