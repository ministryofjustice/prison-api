package net.syscon.elite.api.resource.impl;

import lombok.AllArgsConstructor;
import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.resource.IncidentsResource;
import net.syscon.elite.service.IncidentService;
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
