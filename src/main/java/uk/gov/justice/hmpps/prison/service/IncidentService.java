package uk.gov.justice.hmpps.prison.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.repository.IncidentCaseRepository;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentCaseRepository repository;

    public IncidentService(final IncidentCaseRepository repository) {
        this.repository = repository;
    }

    public IncidentCase getIncidentCase(@NotNull final long incidentCaseId) {
        return repository.getIncidentCases(List.of(incidentCaseId)).stream().findFirst().orElseThrow(EntityNotFoundException.withId(incidentCaseId));
    }

    public List<IncidentCase> getIncidentCasesByOffenderNo(@NotNull final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        return repository.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles);
    }
}
