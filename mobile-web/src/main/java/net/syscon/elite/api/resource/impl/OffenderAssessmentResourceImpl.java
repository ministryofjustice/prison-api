package net.syscon.elite.api.resource.impl;

import io.jsonwebtoken.lang.Collections;
import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.api.resource.OffenderAssessmentResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import java.util.List;

/**
 * Implementation of Offender Assessments (/offender-assessments) endpoint.
 */
@RestResource
@Path("/offender-assessments")
public class OffenderAssessmentResourceImpl implements OffenderAssessmentResource {
    private final InmateService inmateService;

    public OffenderAssessmentResourceImpl(InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    public GetOffenderAssessmentsAssessmentCodeResponse getOffenderAssessmentsAssessmentCode(String assessmentCode, List<String> offenderList) {
        final List<Assessment> results = inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode);
        return GetOffenderAssessmentsAssessmentCodeResponse.respond200WithApplicationJson(results);
    }

    @Override
    public PostOffenderAssessmentsAssessmentCodeResponse postOffenderAssessmentsAssessmentCode(String assessmentCode, List<String> offenderList) {

        validateOffenderList(offenderList);

        final List<Assessment> results = inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode);
        return PostOffenderAssessmentsAssessmentCodeResponse.respond200WithApplicationJson(results);
    }

    private void validateOffenderList(List offenderList) {
        if (Collections.isEmpty(offenderList)) {
            throw new BadRequestException("List of Offender Ids must be provided.");
        }
    }
}
