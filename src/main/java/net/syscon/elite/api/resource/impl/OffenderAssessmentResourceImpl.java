package net.syscon.elite.api.resource.impl;

import io.jsonwebtoken.lang.Collections;
import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.api.model.CategorisationDetail;
import net.syscon.elite.api.model.CategoryApprovalDetail;
import net.syscon.elite.api.model.OffenderCategorise;
import net.syscon.elite.api.resource.OffenderAssessmentResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    public GetOffenderAssessmentsAssessmentCodeResponse getOffenderAssessmentsAssessmentCode(String assessmentCode, List<String> offenderList, Boolean latestOnly) {
        final List<Assessment> results = inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode, !Boolean.FALSE.equals(latestOnly));
        return GetOffenderAssessmentsAssessmentCodeResponse.respond200WithApplicationJson(results);
    }

    @Override
    public PostOffenderAssessmentsAssessmentCodeResponse postOffenderAssessmentsAssessmentCode(String assessmentCode, List<String> offenderList) {

        validateOffenderList(offenderList);

        final List<Assessment> results = inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode, true);
        return PostOffenderAssessmentsAssessmentCodeResponse.respond200WithApplicationJson(results);
    }

    @Override
    public PostOffenderAssessmentsCsraListResponse postOffenderAssessmentsCsraList(List<String> offenderList) {

        validateOffenderList(offenderList);

        final List<Assessment> results = inmateService.getInmatesAssessmentsByCode(offenderList, null, true);
        return PostOffenderAssessmentsCsraListResponse.respond200WithApplicationJson(results);
    }

    @Override
    public GetUncategorisedResponse getUncategorised(String agencyId) {
        final List<OffenderCategorise> results = inmateService.getUncategorised(agencyId);
        return GetUncategorisedResponse.respond200WithApplicationJson(results);
    }

    @Override
    public CreateCategorisationResponse createCategorisation(CategorisationDetail detail) {
        inmateService.createCategorisation(detail.getBookingId(), detail);
        return CreateCategorisationResponse.respond201WithApplicationJson();
    }

    @Override
    public Response approveCategorisation(CategoryApprovalDetail detail) {
        inmateService.approveCategorisation(detail.getBookingId(), detail);
        return Response.ok()
                .status(201)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .build();
    }

    private void validateOffenderList(List offenderList) {
        if (Collections.isEmpty(offenderList)) {
            throw new BadRequestException("List of Offender Ids must be provided.");
        }
    }
}
