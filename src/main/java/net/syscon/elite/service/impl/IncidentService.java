package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.model.Questionnaire;
import net.syscon.elite.repository.impl.IncidentCaseRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.lang.String.format;

@Service
public class IncidentService {

    private final IncidentCaseRepository repository;
    private final BookingService bookingService;

    public IncidentService(IncidentCaseRepository repository, BookingService bookingService) {
        this.repository = repository;
        this.bookingService = bookingService;
    }

    @PreAuthorize("hasAnyRole('SYSTEM_READ_ONLY', 'SYSTEM_USER')")
    public IncidentCase getIncidentCase(@NotNull long incidentCaseId) {
        return repository.getIncidentCases(List.of(incidentCaseId)).stream().findFirst().orElseThrow(EntityNotFoundException.withId(incidentCaseId));
    }

    @VerifyBookingAccess
    public List<IncidentCase> getIncidentCasesByBookingId(@NotNull long bookingId, List<String> incidentTypes, List<String> participationRoles) {
        bookingService.checkBookingExists(bookingId);
        return repository.getIncidentCasesByBookingId(bookingId, incidentTypes, participationRoles);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_READ_ONLY', 'SYSTEM_USER')")
    public List<IncidentCase> getIncidentCasesByOffenderNo(@NotNull String offenderNo, List<String> incidentTypes, List<String> participationRoles) {
        bookingService.getBookingIdByOffenderNo(offenderNo);
        return repository.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles);
    }

    public Questionnaire getQuestionnaire(@NotNull String category, @NotNull String code) {
        return repository.getQuestionnaire(category, code).orElseThrow(EntityNotFoundException.withId(format("%s/%s", category, code)));
    }
}
