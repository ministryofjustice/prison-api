package uk.gov.justice.hmpps.prison.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.repository.IncidentCaseRepository;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentCaseRepository repository;
    private final BookingService bookingService;

    public IncidentService(final IncidentCaseRepository repository, final BookingService bookingService) {
        this.repository = repository;
        this.bookingService = bookingService;
    }

    public IncidentCase getIncidentCase(@NotNull final long incidentCaseId) {
        return repository.getIncidentCases(List.of(incidentCaseId)).stream().findFirst().orElseThrow(EntityNotFoundException.withId(incidentCaseId));
    }

    public List<IncidentCase> getIncidentCasesByOffenderNo(@NotNull final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        bookingService.getOffenderIdentifiers(offenderNo, "SYSTEM_USER");
        return repository.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles);
    }
}
