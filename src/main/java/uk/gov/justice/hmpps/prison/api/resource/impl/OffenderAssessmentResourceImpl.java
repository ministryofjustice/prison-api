package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;
import uk.gov.justice.hmpps.prison.api.resource.OffenderAssessmentResource;
import uk.gov.justice.hmpps.prison.api.support.AssessmentStatusType;
import uk.gov.justice.hmpps.prison.api.support.CategoryInformationType;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.InmateService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of Offender Assessments (/offender-assessments) endpoint.
 */
@RestController
@RequestMapping("${api.base.path}/offender-assessments")
public class OffenderAssessmentResourceImpl implements OffenderAssessmentResource {
    private final InmateService inmateService;

    public OffenderAssessmentResourceImpl(final InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    public List<Assessment> getOffenderAssessmentsAssessmentCode(final String assessmentCode, final List<String> offenderList, final Boolean latestOnly, final Boolean activeOnly) {

        return applyDefaultsAndGetAssessmentsByCode(assessmentCode, offenderList, latestOnly, activeOnly);
    }

    @Override
    public List<Assessment> postOffenderAssessmentsAssessmentCode(final String assessmentCode, final List<String> offenderList, final Boolean latestOnly, final Boolean activeOnly) {
        validateOffenderList(offenderList);

        return applyDefaultsAndGetAssessmentsByCode(assessmentCode, offenderList, latestOnly, activeOnly);
    }

    private List<Assessment> applyDefaultsAndGetAssessmentsByCode(final String assessmentCode, final List<String> offenderList, final Boolean latestOnly, final Boolean activeOnly) {
        final var latest = latestOnly == null ? true : latestOnly;
        final var active = activeOnly == null ? true : activeOnly;

        return inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode, latest, active, false);
    }

    @Override
    public List<Assessment> postOffenderAssessmentsCsraList(final List<String> offenderList) {
        validateOffenderList(offenderList);
        return inmateService.getInmatesAssessmentsByCode(offenderList, null, true, true, true);
    }

    @Override
    public List<Assessment> getAssessments(final List<String> offenderList, final Boolean latestOnly, final Boolean activeOnly) {
        final var latest = latestOnly == null || latestOnly;
        final var active = activeOnly == null || activeOnly;
        validateOffenderList(offenderList);
        return inmateService.getInmatesAssessmentsByCode(offenderList, null, latest, active, false);
    }

    @Override
    public List<OffenderCategorise> getOffenderCategorisations(final String agencyId, final String type, final LocalDate date) {
        final CategoryInformationType enumType;
        try {
            enumType = CategoryInformationType.valueOf(type);
        } catch (final IllegalArgumentException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Categorisation type is invalid: " + type);
        }
        return inmateService.getCategory(agencyId, enumType, date);
    }

    @Override
    public List<OffenderCategorise> getOffenderCategorisations(final String agencyId, final Set<Long> bookingIds, final Boolean latestOnly) {
        final var latest = latestOnly == null ? true : latestOnly;
        return inmateService.getOffenderCategorisations(agencyId, bookingIds, latest);
    }

    @Override
    public List<OffenderCategorise> getOffenderCategorisationsSystem(final Set<Long> bookingIds, final Boolean latestOnly) {
        final var latest = latestOnly == null ? true : latestOnly;
        return inmateService.getOffenderCategorisationsSystem(bookingIds, latest);
    }

    @Override
    @ProxyUser
    public ResponseEntity<Map<String, Long>> createCategorisation(final CategorisationDetail detail) {
        final var resultMap = inmateService.createCategorisation(detail.getBookingId(), detail);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultMap);
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> updateCategorisation(final CategorisationUpdateDetail detail){
        inmateService.updateCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.ok().build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> approveCategorisation(final CategoryApprovalDetail detail) {
        inmateService.approveCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> rejectCategorisation(CategoryRejectionDetail detail) {
        inmateService.rejectCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> setCategorisationInactive(final Long bookingId, final String status){

        final AssessmentStatusType enumType;
        try {
            enumType = StringUtils.isEmpty(status) ? null : AssessmentStatusType.valueOf(status);
        } catch (final IllegalArgumentException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Assessment status type is invalid: " + status);
        }

        inmateService.setCategorisationInactive(bookingId, enumType);
        return ResponseEntity.ok().build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> updateCategorisationNextReviewDate(final Long bookingId, final LocalDate nextReviewDate) {
        inmateService.updateCategorisationNextReviewDate(bookingId, nextReviewDate);
        return ResponseEntity.ok().build();
    }

    private void validateOffenderList(final List<?> offenderList) {
        if (CollectionUtils.isEmpty(offenderList)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "List of Offender Ids must be provided.");
        }
    }
}
