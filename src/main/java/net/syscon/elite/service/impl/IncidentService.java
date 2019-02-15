package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.model.Questionnaire;
import net.syscon.elite.repository.impl.IncidentCaseRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.lang.String.format;

@Service
public class IncidentService {

    private final IncidentCaseRepository repository;

    public IncidentService(IncidentCaseRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasAnyRole('SYSTEM_READ_ONLY', 'SYSTEM_USER')")
    public IncidentCase getIncidentCase(@NotNull long incidentCaseId) {
        return repository.getIncidentCases(List.of(incidentCaseId)).stream().findFirst().orElseThrow(EntityNotFoundException.withId(incidentCaseId));
    }

    @VerifyBookingAccess
    public List<IncidentCase> getIncidentCasesByBookingId(@NotNull long bookingId, String incidentType, List<String> participationRoles) {
        return repository.getIncidentCasesByBookingId(bookingId, incidentType, participationRoles);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_READ_ONLY', 'SYSTEM_USER')")
    public List<IncidentCase> getIncidentCasesByOffenderNo(@NotNull String offenderNo, String incidentType, List<String> participationRoles) {
        return repository.getIncidentCasesByOffenderNo(offenderNo, incidentType, participationRoles);
    }

    public Questionnaire getQuestionnaire(@NotNull String category, @NotNull String code) {
        return repository.getQuestionnaire(category, code).orElseThrow(EntityNotFoundException.withId(format("%s/%s", category, code)));
    }
}
