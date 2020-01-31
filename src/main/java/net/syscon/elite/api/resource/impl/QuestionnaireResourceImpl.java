package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Questionnaire;
import net.syscon.elite.api.resource.QuestionnaireResource;
import net.syscon.elite.service.impl.IncidentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
