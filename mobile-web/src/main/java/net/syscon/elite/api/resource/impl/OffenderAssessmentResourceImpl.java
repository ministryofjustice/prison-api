package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.api.resource.OffenderAssessmentResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;

import javax.ws.rs.Path;

import java.util.List;

/**
 * Implementation of Booking (/bookings) endpoint.
 */
@RestResource
@Path("/offender-assessments")
public class OffenderAssessmentResourceImpl implements OffenderAssessmentResource {
    private final InmateService inmateService;

    public OffenderAssessmentResourceImpl(InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    public GetAssessmentsByCodeResponse getAssessmentsByCode(String assessmentCode, List<Long> bookingId) {
        final List<Assessment> results = inmateService.getInmatesAssessmentsByCode(bookingId, assessmentCode);
        return GetAssessmentsByCodeResponse.respond200WithApplicationJson(results);
    }
}
