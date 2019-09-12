package net.syscon.elite.api.resource.impl;

import io.jsonwebtoken.lang.Collections;
import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.api.model.CategorisationDetail;
import net.syscon.elite.api.model.CategoryApprovalDetail;
import net.syscon.elite.api.model.OffenderCategorise;
import net.syscon.elite.api.resource.OffenderAssessmentResource;
import net.syscon.elite.api.support.CategoryInformationType;
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
    public GetOffenderAssessmentsAssessmentCodeResponse getOffenderAssessmentsAssessmentCode(final String assessmentCode, final List<String> offenderList, final Boolean latestOnly, final Boolean activeOnly) {

        final var results = applyDefaultsAndGetAssessmentsByCode(assessmentCode, offenderList, latestOnly, activeOnly);
        return GetOffenderAssessmentsAssessmentCodeResponse.respond200WithApplicationJson(results);
    }

    @Override
    public PostOffenderAssessmentsAssessmentCodeResponse postOffenderAssessmentsAssessmentCode(final String assessmentCode, final List<String> offenderList, final Boolean latestOnly, final Boolean activeOnly) {
        validateOffenderList(offenderList);

        final var results = applyDefaultsAndGetAssessmentsByCode(assessmentCode, offenderList, latestOnly, activeOnly);
        return PostOffenderAssessmentsAssessmentCodeResponse.respond200WithApplicationJson(results);
    }

    private List<Assessment> applyDefaultsAndGetAssessmentsByCode(String assessmentCode, List<String> offenderList, Boolean latestOnly, Boolean activeOnly) {
        var latest = latestOnly == null ? true : latestOnly;
        var active = activeOnly == null ? true : activeOnly;

        return inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode, latest, active);
    }

    @Override
    public PostOffenderAssessmentsCsraListResponse postOffenderAssessmentsCsraList(final List<String> offenderList) {

        validateOffenderList(offenderList);

        final var results = inmateService.getInmatesAssessmentsByCode(offenderList, null, true, true);
        return PostOffenderAssessmentsCsraListResponse.respond200WithApplicationJson(results);
    }

    @Override
    public List<OffenderCategorise> getOffenderCategorisations(final String agencyId, final String type, final LocalDate date) {
        CategoryInformationType enumType;
        try{
            enumType = CategoryInformationType.valueOf(type);
        } catch(IllegalArgumentException e){
            throw new BadRequestException("Categorisation type is invalid: " + type);
        }
        return inmateService.getCategory(agencyId, enumType, date);
    }

    @Override
    public List<OffenderCategorise> getOffenderCategorisations(final String agencyId, final Set<Long> bookingIds, final Boolean latestOnly) {
        var latest = latestOnly == null ? true : latestOnly;
        final var results = inmateService.getOffenderCategorisations(agencyId, bookingIds, latest);
        return results;
    }

    @Override
    @ProxyUser
    public Response createCategorisation(final CategorisationDetail detail) {
        return Response.ok()
                .status(201)
                .entity(inmateService.createCategorisation(detail.getBookingId(), detail))
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .build();
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

    @Override
    @ProxyUser
    public Response updateCategorisationNextReviewDate(final Long bookingId,  final LocalDate nextReviewDate) {
        inmateService.updateCategorisationNextReviewDate(bookingId, nextReviewDate);
        return Response.ok()
                .status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .build();
    }

    private void validateOffenderList(final List offenderList) {
        if (Collections.isEmpty(offenderList)) {
            throw new BadRequestException("List of Offender Ids must be provided.");
        }
    }
}
