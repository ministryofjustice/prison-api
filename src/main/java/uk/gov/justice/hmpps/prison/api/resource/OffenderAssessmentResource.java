package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail;
import uk.gov.justice.hmpps.prison.api.model.AssessmentSummary;
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;
import uk.gov.justice.hmpps.prison.api.support.AssessmentStatusType;
import uk.gov.justice.hmpps.prison.api.support.CategoryInformationType;
import uk.gov.justice.hmpps.prison.core.ProgrammaticAuthorisation;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.OffenderAssessmentService;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Tag(name = "offender-assessments")
@Validated
@RequestMapping(value = "${api.base.path}/offender-assessments", produces = "application/json")
public class OffenderAssessmentResource {
    private final InmateService inmateService;
    private final OffenderAssessmentService offenderAssessmentService;

    public OffenderAssessmentResource(final InmateService inmateService, final OffenderAssessmentService offenderAssessmentService) {
        this.inmateService = inmateService;
        this.offenderAssessmentService = offenderAssessmentService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender assessment detail for multiple offenders.")
    @ProgrammaticAuthorisation("Data is limited to the users caseload")
    @GetMapping("/{assessmentCode}")
    @SlowReportQuery
    public List<Assessment> getOffenderAssessmentsAssessmentCode(
        @PathVariable("assessmentCode") @Parameter(description = "Assessment Type Code", required = true) final String assessmentCode,
        @RequestBody @NotEmpty @RequestParam("offenderNo") @Parameter(description = "The required offender numbers", required = true) final List<String> offenderList,
        @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @Parameter(description = "Returns only assessments for the current sentence if true, otherwise assessments for all previous sentences are included") final Boolean latestOnly,
        @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @Parameter(description = "Returns only active assessments if true, otherwise inactive and pending assessments are included") final Boolean activeOnly,
        @RequestParam(value = "mostRecentOnly", required = false) @Parameter(description = "Returns only the last assessment per sentence if true, otherwise all assessments for the booking are included") final Boolean mostRecentOnly
    ) {
        return applyDefaultsAndGetAssessmentsByCode(assessmentCode, offenderList, latestOnly, activeOnly, mostRecentOnly);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The assessment list is returned.")})
    @Operation(summary = "Retrieves Offender assessment details for multiple offenders - POST version to allow large offender lists.")
    @ProgrammaticAuthorisation("Data is limited to the users caseload")
    @PostMapping("/{assessmentCode}")
    @SlowReportQuery
    public List<Assessment> postOffenderAssessmentsAssessmentCode(
        @PathVariable("assessmentCode") @Parameter(description = "Assessment Type Code", required = true) final String assessmentCode,
        @RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> offenderList,
        @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @Parameter(description = "Returns only assessments for the current sentence if true, otherwise assessments for all previous sentences are included") final Boolean latestOnly,
        @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @Parameter(description = "Returns only active assessments if true, otherwise inactive and pending assessments are included") final Boolean activeOnly,
        @RequestParam(value = "mostRecentOnly", required = false) @Parameter(description = "Returns only the last assessment per sentence if true, otherwise all assessments for the booking are included") final Boolean mostRecentOnly
    ) {
        validateOffenderList(offenderList);

        return applyDefaultsAndGetAssessmentsByCode(assessmentCode, offenderList, latestOnly, activeOnly, mostRecentOnly);
    }

    private List<Assessment> applyDefaultsAndGetAssessmentsByCode(final String assessmentCode, final List<String> offenderList, final Boolean latestOnly, final Boolean activeOnly, final Boolean mostRecentOnly) {
        final var latest = latestOnly == null || latestOnly;
        final var active = activeOnly == null || activeOnly;
        final var mostRecent = mostRecentOnly == null ? latest : mostRecentOnly; // backwards compatibility

        return inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode, latest, active, false, mostRecent);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The CSRA assessment list is returned, 1 per offender.")})
    @Operation(summary = "Retrieves Offender CSRAs for multiple offenders - POST version to allow large offender lists.")
    @ProgrammaticAuthorisation("Data is limited to the users caseload")
    @PostMapping("/csra/list")
    @SlowReportQuery
    public List<Assessment> postOffenderAssessmentsCsraList(@RequestBody @NotEmpty @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> offenderList) {
        validateOffenderList(offenderList);
        return inmateService.getInmatesAssessmentsByCode(offenderList, null, true, true, true, true);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Retrieves CSRAs for the given offender, ordered by the latest first.")
    @VerifyOffenderAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/csra/{offenderNo}")
    public List<AssessmentSummary> getOffenderCsraAssessments(@PathVariable("offenderNo") @Parameter(description = "The offender number") final String offenderNo) {
        return offenderAssessmentService.getOffenderAssessments(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Retrieves details of a single CSRA assessment.")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/csra/{bookingId}/assessment/{assessmentSeq}")
    public AssessmentDetail getOffenderCsraAssessment(
        @PathVariable("bookingId") @Parameter(description = "The booking id of offender") final Long bookingId, @PathVariable("assessmentSeq") @Parameter(description = "The assessment sequence number for the given offender booking") final Integer assessmentSeq
    ) {
        return offenderAssessmentService.getOffenderAssessment(bookingId, assessmentSeq);
    }

    @Operation(summary = "Returns category information on Offenders at a prison.")
    @VerifyAgencyAccess(overrideRoles = {"VIEW_ASSESSMENTS"})
    @GetMapping("/category/{agencyId}")
    @SlowReportQuery
    public List<OffenderCategorise> getOffenderCategorisations(
        @PathVariable("agencyId") @Parameter(description = "Prison id", required = true) final String agencyId,
        @NotNull(message = "Categorisation type must not be null") @RequestParam("type") @Parameter(description = """
            Indicates which type of category information is required.
                <li>UNCATEGORISED: Offenders who need to be categorised,</li>
                <li>CATEGORISED: Offenders who have an approved categorisation,</li>
                <li>RECATEGORISATIONS: Offenders who will soon require recategorisation</li>""", required = true) final String type,
        @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = """
            For type CATEGORISED: The past date from which categorisations are returned.<br />
            For type RECATEGORISATIONS: the future cutoff date: list includes all prisoners who require re-categorisation on or before this date.<br />
            For type UNCATEGORISED: Ignored; do not set this parameter.""") final LocalDate date
    ) {
        final CategoryInformationType enumType;
        try {
            enumType = CategoryInformationType.valueOf(type);
        } catch (final IllegalArgumentException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Categorisation type is invalid: " + type);
        }
        return inmateService.getCategory(agencyId, enumType, date);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The list of offenders with categorisation details is returned if categorisation record exists")})
    @Operation(summary = "Returns Categorisation details for supplied Offenders - POST version to allow large offender lists.", description = "Categorisation details for all supplied Offenders using SYSTEM access")
    @PreAuthorize("hasRole('VIEW_PRISONER_DATA')")
    @PostMapping("/category")
    @SlowReportQuery
    public List<OffenderCategorise> getOffenderCategorisationsSystem(@RequestBody @Parameter(description = "The required booking Ids (mandatory)", required = true) final Set<Long> bookingIds, @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @Parameter(description = "Only get the latest category for each booking") final Boolean latestOnly) {
        final var latest = latestOnly == null || latestOnly;
        return inmateService.getOffenderCategorisationsSystem(bookingIds, latest);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. category does not exist.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Record new offender categorisation.", description = "Create new categorisation record. The booking id and new sequence number is returned.")
    @PreAuthorize("hasAnyRole('MAINTAIN_ASSESSMENTS','CREATE_CATEGORISATION','CREATE_RECATEGORISATION')")
    @PostMapping("/category/categorise")
    @ProxyUser
    public ResponseEntity<Map<String, Long>> createCategorisation(@Valid @RequestBody @Parameter(description = "Categorisation details", required = true) final CategorisationDetail detail) {
        final var resultMap = inmateService.createCategorisation(detail.getBookingId(), detail);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(resultMap);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. category does not exist.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update a pending offender categorisation.", description = "This is intended for use by the categoriser to correct any problems with a pending (in-progress) categorisation." +
        " Fields left as null will be left unchanged")
    @PreAuthorize("hasAnyRole('MAINTAIN_ASSESSMENTS','CREATE_CATEGORISATION','CREATE_RECATEGORISATION')")
    @PutMapping("/category/categorise")
    @ProxyUser
    public ResponseEntity<Void> updateCategorisation(@Valid @RequestBody @Parameter(description = "Categorisation details", required = true) final CategorisationUpdateDetail detail) {
        inmateService.updateCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error - e.g. category does not exist.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Approve a pending offender categorisation.", description = "Update categorisation record with approval.")
    @PreAuthorize("hasAnyRole('MAINTAIN_ASSESSMENTS','APPROVE_CATEGORISATION')")
    @PutMapping("/category/approve")
    @ProxyUser
    public ResponseEntity<Void> approveCategorisation(@Valid @RequestBody @Parameter(description = "Approval details", required = true) final CategoryApprovalDetail detail) {
        inmateService.approveCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error - e.g. comment too long or committee code does not exist.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Reject a pending offender categorisation.", description = "Update categorisation record with rejection.")
    @PreAuthorize("hasAnyRole('MAINTAIN_ASSESSMENTS','APPROVE_CATEGORISATION')")
    @PutMapping("/category/reject")
    @ProxyUser
    public ResponseEntity<Void> rejectCategorisation(@Valid @RequestBody @Parameter(description = "Rejection details", required = true) final CategoryRejectionDetail detail) {
        inmateService.rejectCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. invalid status.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to update categorisations.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Set all active or pending (status A or P) categorisations inactive", description = "This endpoint should only be used with edge case categorisations.")
    @PreAuthorize("hasRole('MAINTAIN_ASSESSMENTS')")
    @PutMapping("/category/{bookingId}/inactive")
    @ProxyUser
    public ResponseEntity<Void> setCategorisationInactive(
        @PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId,
        @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") @Parameter(description = "Indicates which categorisation statuses to set." +
            "<li>ACTIVE (default): set all active (i.e. approved) categorisations inactive,</li>" +
            "<li>PENDING: set all pending (i.e. awaiting approval) categorisations inactive,</li>", schema = @Schema(implementation = String.class, allowableValues = {"ACTIVE", "PENDING"})) final String status
    ) {
        final AssessmentStatusType enumType;
        try {
            enumType = ObjectUtils.isEmpty(status) ? null : AssessmentStatusType.valueOf(status);
        } catch (final IllegalArgumentException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Assessment status type is invalid: " + status);
        }

        inmateService.setCategorisationInactive(bookingId, enumType);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Active categorisation not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to update the categorisation.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update the next review date on the latest active categorisation", description = "Update categorisation record with new next review date.")
    @PreAuthorize("hasRole('MAINTAIN_ASSESSMENTS')")
    @PutMapping("/category/{bookingId}/nextReviewDate/{nextReviewDate}")
    @ProxyUser
    public ResponseEntity<Void> updateCategorisationNextReviewDate(
        @PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable("nextReviewDate") @Parameter(description = "The new next review date (in YYYY-MM-DD format)", required = true) final LocalDate nextReviewDate
    ) {
        inmateService.updateCategorisationNextReviewDate(bookingId, nextReviewDate);
        return ResponseEntity.ok().build();
    }

    private void validateOffenderList(final List<?> offenderList) {
        if (CollectionUtils.isEmpty(offenderList)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "List of Offender Ids must be provided.");
        }
    }
}
