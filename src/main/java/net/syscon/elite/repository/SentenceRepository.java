package net.syscon.elite.repository;

import net.syscon.elite.api.model.OffenceDetail;
import net.syscon.elite.api.model.OffenceHistoryDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SentenceRepository {
    List<OffenceDetail> getMainOffenceDetails(Long bookingId);

    List<OffenceDetail> getMainOffenceDetails(List<Long> bookingIds);

    List<OffenceHistoryDetail> getOffenceHistory(String offenderNo);

    Optional<LocalDate> getConfirmedReleaseDate(Long bookingId);
}
