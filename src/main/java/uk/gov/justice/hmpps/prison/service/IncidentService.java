package uk.gov.justice.hmpps.prison.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.api.model.Questionnaire;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.IncidentCaseRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;

@Service
public class IncidentService {

    private final IncidentCaseRepository repository;
    private final BookingService bookingService;

    public IncidentService(final IncidentCaseRepository repository, final BookingService bookingService) {
        this.repository = repository;
        this.bookingService = bookingService;
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER')")
    public IncidentCase getIncidentCase(@NotNull final long incidentCaseId) {
        return repository.getIncidentCases(List.of(incidentCaseId)).stream().findFirst().orElseThrow(EntityNotFoundException.withId(incidentCaseId));
    }

    @VerifyBookingAccess
    public List<IncidentCase> getIncidentCasesByBookingId(@NotNull final long bookingId, final List<String> incidentTypes, final List<String> participationRoles) {
        bookingService.checkBookingExists(bookingId);
        return repository.getIncidentCasesByBookingId(bookingId, incidentTypes, participationRoles);
    }

    @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA','SYSTEM_USER')")
    public List<IncidentCase> getIncidentCasesByOffenderNo(@NotNull final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        bookingService.getOffenderIdentifiers(offenderNo);
        return repository.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles);
    }

    public Questionnaire getQuestionnaire(@NotNull final String category, @NotNull final String code) {
        return repository.getQuestionnaire(category, code).orElseThrow(EntityNotFoundException.withId(format("%s/%s", category, code)));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER')")
    public Page<String> getIncidentCandidates(LocalDateTime cutoffTimestamp, final long offset, final long limit) {
        return repository.getIncidentCandidates(cutoffTimestamp, offset, limit);
    }
}
