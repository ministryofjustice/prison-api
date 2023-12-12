package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;
import uk.gov.justice.hmpps.prison.api.model.BaseSentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.HdcChecks;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalc;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.curfews.OffenderCurfewService;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "offender-sentences")
@Validated
@RequestMapping(value = "${api.base.path}/offender-sentences", produces = "application/json")
public class OffenderSentenceResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;
    private final OffenderCurfewService offenderCurfewService;

    public OffenderSentenceResource(
            final AuthenticationFacade authenticationFacade,
            final BookingService bookingService,
            final OffenderCurfewService offenderCurfewService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
        this.offenderCurfewService = offenderCurfewService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders (with associated sentence detail)", description = """
        <h3>Algorithm</h3>
        <ul>
          <li>If there is a confirmed release date, the offender release date is the confirmed release date.</li>
          <li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li>
          <li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li>
        </ul>
        """)
    @GetMapping
    @SlowReportQuery
    public List<OffenderSentenceDetail> getOffenderSentences(@RequestParam(value = "agencyId", required = false) @Parameter(description = "agency/prison to restrict results, if none provided current active caseload will be used, unless offenderNo list is specified") final String agencyId, @RequestParam(value = "offenderNo", required = false) @Parameter(description = "a list of offender numbers to search.") final List<String> offenderNos) {
        return bookingService.getOffenderSentencesSummary(
                agencyId,
                offenderNos);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sentence details for offenders who are candidates for Home Detention Curfew."),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders eligible for HDC", description = "Version 1")
    @GetMapping("/home-detention-curfew-candidates")
    @SlowReportQuery
    public List<OffenderSentenceCalc<BaseSentenceCalcDates>> getOffenderSentencesHomeDetentionCurfewCandidates() {
        return offenderCurfewService.getHomeDetentionCurfewCandidates(authenticationFacade.getCurrentUsername());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HDC information", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = HomeDetentionCurfew.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @Operation(summary = "Retrieve the current state of the latest Home Detention Curfew for a booking")
    @GetMapping("/booking/{bookingId}/home-detention-curfews/latest")
    @VerifyBookingAccess(overrideRoles = "VIEW_PRISONER_DATA")
    public HomeDetentionCurfew getLatestHomeDetentionCurfew(@PathVariable("bookingId") Long bookingId) {
        return offenderCurfewService.getLatestHomeDetentionCurfew(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of HDC status information"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @Operation(summary = "Retrieve the latest Home Detention Curfew status for a list of offender booking identifiers")
    @PostMapping("/home-detention-curfews/latest")
    @SlowReportQuery
    @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA')")
    public List<HomeDetentionCurfew> getBatchLatestHomeDetentionCurfew(@RequestBody @Parameter(description = "A list of booking ids", required = true) final List<Long> bookingIds) {
        validateBookingIdList(bookingIds);
        return offenderCurfewService.getBatchLatestHomeDetentionCurfew(bookingIds);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "The checks passed flag was set"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "423", description = "Curfew or HDC status in use for this booking id (possibly in P-Nomis).", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @Operation(summary = "Set the HDC checks passed flag")
    @PutMapping("/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
    @ProxyUser
    @PreAuthorize("hasAnyRole('MAINTAIN_HDC') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> setCurfewChecks(@PathVariable("bookingId") final Long bookingId, @RequestBody @Valid final HdcChecks hdcChecks) {
        offenderCurfewService.setHdcChecks(bookingId, hdcChecks);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "The checks passed flag was cleared"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "423", description = "Curfew or HDC status in use for this booking id (possibly in P-Nomis).", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @Operation(summary = "Clear the HDC checks passed flag")
    @DeleteMapping("/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
    @ProxyUser
    @PreAuthorize("hasAnyRole('MAINTAIN_HDC') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> clearCurfewChecks(@PathVariable("bookingId") Long bookingId) {
        offenderCurfewService.deleteHdcChecks(bookingId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "The new approval status was set"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "423", description = "Curfew or HDC status in use for this booking id (possibly in P-Nomis).", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @Operation(summary = "Set the HDC approval status")
    @PutMapping("/booking/{bookingId}/home-detention-curfews/latest/approval-status")
    @ProxyUser
    @PreAuthorize("hasAnyRole('MAINTAIN_HDC') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> setApprovalStatus(@PathVariable("bookingId") final Long bookingId, @RequestBody @Valid final ApprovalStatus approvalStatus) {
        offenderCurfewService.setApprovalStatus(bookingId, approvalStatus);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "The new approval status was cleared"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "423", description = "Curfew or HDC status in use for this booking id (possibly in P-Nomis).", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @Operation(summary = "Clear the HDC approval status")
    @DeleteMapping("/booking/{bookingId}/home-detention-curfews/latest/approval-status")
    @ProxyUser
    @PreAuthorize("hasAnyRole('MAINTAIN_HDC') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> clearApprovalStatus(@PathVariable("bookingId") Long bookingId) {
        offenderCurfewService.deleteApprovalStatus(bookingId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The list of offenders is returned.")})
    @Operation(summary = "Retrieves list of offenders (with associated sentence detail) - POST version to allow large offender lists.", description = "Retrieves list of offenders (with associated sentence detail) - POST version to allow large offender lists.")
    @PostMapping
    @SlowReportQuery
    public List<OffenderSentenceDetail> postOffenderSentences(@RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos) {
        validateOffenderList(offenderNos);

        //no agency id filter required here as offenderNos will always be provided
        return bookingService.getOffenderSentencesSummary(null, offenderNos);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The list of offenders is returned.")})
    @Operation(summary = "Retrieves list of offenders (with associated sentence detail) - POST version using booking id lists.", description = "Retrieves list of offenders (with associated sentence detail) - POST version using booking id lists.")
    @PostMapping("/bookings")
    @SlowReportQuery
    public List<OffenderSentenceDetail> postOffenderSentencesBookings(@RequestBody @Parameter(description = "The required booking ids (mandatory)", required = true) final List<Long> bookingIds) {
        validateOffenderList(bookingIds);
        return bookingService.getBookingSentencesSummary(bookingIds);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sentence term details for a prisoner."),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Sentence term details for a prisoner")
    @GetMapping("/booking/{bookingId}/sentenceTerms")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<OffenderSentenceTerms> getOffenderSentenceTerms(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId, @RequestParam(value = "filterBySentenceTermCodes", required = false) final List<String> filterBySentenceTermCodes) {
        return bookingService.getOffenderSentenceTerms(bookingId, filterBySentenceTermCodes);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sentence and offence details for a prisoner.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OffenderSentenceAndOffences.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Sentence and offence details  for a prisoner")
    @GetMapping("/booking/{bookingId}/sentences-and-offences")
    public List<OffenderSentenceAndOffences> getSentenceAndOffenceDetails(@PathVariable("bookingId") @Parameter(description = "The required booking id (mandatory)", required = true) final Long bookingId) {
        return bookingService.getSentenceAndOffenceDetails(bookingId);
    }

    private void validateOffenderList(final List<?> offenderList) {
        if (CollectionUtils.isEmpty(offenderList)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "List of Offender Ids must be provided");
        }
    }

    private void validateBookingIdList(final List<Long> bookingIdList) {
        if (CollectionUtils.isEmpty(bookingIdList)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "List of Booking Ids must be provided");
        }
    }
}
