package net.syscon.prison.api.resource.impl;

import lombok.AllArgsConstructor;
import net.syscon.prison.api.model.IncidentCase;
import net.syscon.prison.api.resource.IncidentsResource;
import net.syscon.prison.service.IncidentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base.path}/incidents")
@AllArgsConstructor
public class IncidentsResourceImpl implements IncidentsResource {

    private final IncidentService incidentService;

    public IncidentCase getIncident(final Long incidentId) {
        return incidentService.getIncidentCase(incidentId);

    }

}
