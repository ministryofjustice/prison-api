package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.Questionnaire;
import uk.gov.justice.hmpps.prison.api.resource.QuestionnaireResource;
import uk.gov.justice.hmpps.prison.service.IncidentService;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("${api.base.path}/questionnaires")
public class QuestionnaireResourceImpl implements QuestionnaireResource {
    private final IncidentService incidentService;

    public QuestionnaireResourceImpl(final IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @Override
    public Questionnaire getQuestionnaire(@NotNull final String category, @NotNull final String code) {
        return incidentService.getQuestionnaire(category, code);
    }
}
