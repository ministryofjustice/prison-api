package net.syscon.elite.api.resource.impl;

import io.jsonwebtoken.lang.Collections;
import net.syscon.elite.api.model.CategorisationDetail;
import net.syscon.elite.api.model.CategoryApprovalDetail;
import net.syscon.elite.api.resource.OffenderAssessmentResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Implementation of Offender Assessments (/offender-assessments) endpoint.
 */
@RestResource
@Path("/offender-assessments")
public class OffenderAssessmentResourceImpl implements OffenderAssessmentResource {
    private final InmateService inmateService;

    public OffenderAssessmentResourceImpl(final InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    public GetOffenderAssessmentsAssessmentCodeResponse getOffenderAssessmentsAssessmentCode(final String assessmentCode, final List<String> offenderList, final Boolean latestOnly) {
        final var results = inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode, !Boolean.FALSE.equals(latestOnly));
        return GetOffenderAssessmentsAssessmentCodeResponse.respond200WithApplicationJson(results);
    }

    @Override
    public PostOffenderAssessmentsAssessmentCodeResponse postOffenderAssessmentsAssessmentCode(final String assessmentCode, final List<String> offenderList) {

        validateOffenderList(offenderList);

        final var results = inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode, true);
        return PostOffenderAssessmentsAssessmentCodeResponse.respond200WithApplicationJson(results);
    }

    @Override
    public PostOffenderAssessmentsCsraListResponse postOffenderAssessmentsCsraList(final List<String> offenderList) {

        validateOffenderList(offenderList);

        final var results = inmateService.getInmatesAssessmentsByCode(offenderList, null, true);
        return PostOffenderAssessmentsCsraListResponse.respond200WithApplicationJson(results);
    }

    @Override
    public GetUncategorisedResponse getUncategorised(final String agencyId) {
        final var results = inmateService.getUncategorised(agencyId);
        return GetUncategorisedResponse.respond200WithApplicationJson(results);
    }

    @Override
    public GetUncategorisedResponse getApprovedCategorised(final String agencyId, final LocalDate fromDate) {
        final var cutOffDate = fromDate != null ? fromDate : LocalDate.now().minusMonths(1);
        final var results = inmateService.getApprovedCategorised(agencyId, cutOffDate);
        return GetUncategorisedResponse.respond200WithApplicationJson(results);
    }

    @Override
    public GetUncategorisedResponse getOffenderCategorisations(final String agencyId, final Set<Long> bookingIds) {
        final var results = inmateService.getOffenderCategorisations(agencyId, bookingIds);
        return GetUncategorisedResponse.respond200WithApplicationJson(results);
    }

    @Override
    @ProxyUser
    public CreateCategorisationResponse createCategorisation(final CategorisationDetail detail) {
        inmateService.createCategorisation(detail.getBookingId(), detail);
        return CreateCategorisationResponse.respond201WithApplicationJson();
    }

    @Override
    @ProxyUser
    public Response approveCategorisation(final CategoryApprovalDetail detail) {
        inmateService.approveCategorisation(detail.getBookingId(), detail);
        return Response.ok()
                .status(201)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .build();
    }

    private void validateOffenderList(final List offenderList) {
        if (Collections.isEmpty(offenderList)) {
            throw new BadRequestException("List of Offender Ids must be provided.");
        }
    }
}
