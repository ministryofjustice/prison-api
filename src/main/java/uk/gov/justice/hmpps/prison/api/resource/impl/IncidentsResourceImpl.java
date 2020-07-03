package uk.gov.justice.hmpps.prison.api.resource.impl;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.api.resource.IncidentsResource;
import uk.gov.justice.hmpps.prison.service.IncidentService;

@RestController
@RequestMapping("${api.base.path}/incidents")
@AllArgsConstructor
public class IncidentsResourceImpl implements IncidentsResource {

    private final IncidentService incidentService;

    public IncidentCase getIncident(final Long incidentId) {
        return incidentService.getIncidentCase(incidentId);

    }

}
