package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.OffenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SentenceRepository {
    List<OffenceDetail> getMainOffenceDetails(Long bookingId);

    List<OffenceDetail> getMainOffenceDetails(List<Long> bookingIds);

    List<OffenceHistoryDetail> getOffenceHistory(String offenderNo, boolean convictionsOnly);

    List<OffenceHistoryDetail> getActiveOffencesForBooking(Long bookingId, boolean convictionsOnly);

    Optional<LocalDate> getConfirmedReleaseDate(Long bookingId);
}
