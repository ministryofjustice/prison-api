package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.QuestionnaireResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.impl.IncidentService;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RestResource
@Path("/questionnaires")
public class QuestionnaireResourceImpl implements QuestionnaireResource {
    private final IncidentService incidentService;

    public QuestionnaireResourceImpl(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @Override
    public QuestionnaireResponse getQuestionnaire(@NotNull String category, @NotNull String code) {
        return new QuestionnaireResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(), incidentService.getQuestionnaire(category, code));
    }
}
