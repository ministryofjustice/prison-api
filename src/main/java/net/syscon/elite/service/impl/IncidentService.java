package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.model.Questionnaire;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.impl.IncidentCaseRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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

    @PreAuthorize("hasAnyRole('SYSTEM_READ_ONLY', 'SYSTEM_USER')")
    public IncidentCase getIncidentCase(@NotNull final long incidentCaseId) {
        return repository.getIncidentCases(List.of(incidentCaseId)).stream().findFirst().orElseThrow(EntityNotFoundException.withId(incidentCaseId));
    }

    @VerifyBookingAccess
    public List<IncidentCase> getIncidentCasesByBookingId(@NotNull final long bookingId, final List<String> incidentTypes, final List<String> participationRoles) {
        bookingService.checkBookingExists(bookingId);
        return repository.getIncidentCasesByBookingId(bookingId, incidentTypes, participationRoles);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_READ_ONLY', 'SYSTEM_USER')")
    public List<IncidentCase> getIncidentCasesByOffenderNo(@NotNull final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        bookingService.getBookingIdByOffenderNo(offenderNo);
        return repository.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles);
    }

    public Questionnaire getQuestionnaire(@NotNull final String category, @NotNull final String code) {
        return repository.getQuestionnaire(category, code).orElseThrow(EntityNotFoundException.withId(format("%s/%s", category, code)));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_READ_ONLY', 'SYSTEM_USER')")
    public Page<String> getIncidentCandidates(LocalDateTime cutoffTimestamp, final long offset, final long limit) {
        return repository.getIncidentCandidates(cutoffTimestamp, offset, limit);
    }
}
