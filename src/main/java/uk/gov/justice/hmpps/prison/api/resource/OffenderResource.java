package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.OffenderDamageObligationResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.api.model.PrisonerIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PrisonerInPrisonSummary;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary;
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking;
import uk.gov.justice.hmpps.prison.api.model.RequestToCreate;
import uk.gov.justice.hmpps.prison.api.model.RequestToDischargePrisoner;
import uk.gov.justice.hmpps.prison.api.model.RequestToRecall;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOut;
import uk.gov.justice.hmpps.prison.api.model.UpdateCaseNote;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSearchResponse;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation.Status;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CaseNoteFilter;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.AdjudicationSearchCriteria;
import uk.gov.justice.hmpps.prison.service.AdjudicationService;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.GlobalSearchService;
import uk.gov.justice.hmpps.prison.service.IncidentService;
import uk.gov.justice.hmpps.prison.service.InmateAlertService;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.MovementsService;
import uk.gov.justice.hmpps.prison.service.OffenderAddressService;
import uk.gov.justice.hmpps.prison.service.OffenderDamageObligationService;
import uk.gov.justice.hmpps.prison.service.OffenderTransactionHistoryService;
import uk.gov.justice.hmpps.prison.service.PrisonerCreationService;
import uk.gov.justice.hmpps.prison.service.PrisonerReleaseAndTransferService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

@RestController
@Api(tags = {"offenders"})
@Validated
@RequestMapping("${api.base.path}/offenders")
@RequiredArgsConstructor
public class OffenderResource {

    private final IncidentService incidentService;
    private final InmateService inmateService;
    private final InmateAlertService alertService;
    private final OffenderAddressService addressService;
    private final AdjudicationService adjudicationService;
    private final CaseNoteService caseNoteService;
    private final BookingService bookingService;
    private final GlobalSearchService globalSearchService;
    private final AuthenticationFacade authenticationFacade;
    private final PrisonerCreationService prisonerCreationService;
    private final PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;
    private final OffenderDamageObligationService offenderDamageObligationService;
    private final OffenderTransactionHistoryService offenderTransactionHistoryService;
    private final MovementsService movementsService;

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Full details about the current state of an offender")
    @GetMapping("/{offenderNo}")
    public InmateDetail getOffender(
        @RequestHeader(value = "version", defaultValue = "1.0", required = false) @ApiParam(value = "Version of Offender details, default is 1.0, Beta is version 1.1_beta and is WIP (do not use in production)", defaultValue = "1.0") final String version,
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Offender Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offenderNo of offender", example = "A1234AA", required = true) final String offenderNo) {
        if ("1.1_beta".equals(version)) {
            // TODO: This is WIP as not all data is yet mapped
            return bookingService.getOffender(offenderNo);
        }
        return inmateService.findOffender(offenderNo, true);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Full details about the current state of an offender")
    @GetMapping("/{offenderNo}/prison-timeline")
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'VIEW_PRISONER_DATA')")
    public PrisonerInPrisonSummary getOffenderPrisonPeriods(@Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Offender Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offenderNo of offender", example = "A1234AA", required = true) final String offenderNo) {
        return movementsService.getPrisonerInPrisonSummary(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "Forbidden - user not authorised to create a prisoner.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("*** BETA *** Creates a prisoner. BOOKING_CREATE role")
    @PostMapping
    @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail createPrisoner(@RequestBody @NotNull @Valid final RequestToCreate requestToCreate) {
        return prisonerCreationService.createPrisoner(requestToCreate);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "Forbidden - user not authorised to release a prisoner.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("*** BETA *** Releases a prisoner from their current prison location. Must be an active prisoner in currently inside a prison, requires the RELEASE_PRISONER role")
    @PutMapping("/{offenderNo}/release")
    @PreAuthorize("hasRole('RELEASE_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    @VerifyOffenderAccess
    public InmateDetail releasePrisoner(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToReleasePrisoner requestToReleasePrisoner) {
        return prisonerReleaseAndTransferService.releasePrisoner(offenderNo, requestToReleasePrisoner, null);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "Forbidden - user not authorised to release a prisoner.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("*** BETA *** Discharges a prisoner to hospital, requires the RELEASE_PRISONER role")
    @PutMapping("/{offenderNo}/discharge-to-hospital")
    @PreAuthorize("hasRole('RELEASE_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail dischargePrisonerToHospital(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToDischargePrisoner requestToDischargePrisoner) {
        return prisonerReleaseAndTransferService.dischargeToHospital(offenderNo, requestToDischargePrisoner);
    }


    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "Forbidden - user not authorised to recall a prisoner.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("*** BETA *** Recalls a prisoner into prison. TRANSFER_PRISONER role")
    @PutMapping("/{offenderNo}/recall")
    @PreAuthorize("hasRole('TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail recallPrisoner(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToRecall requestToRecall) {
        return prisonerReleaseAndTransferService.recallPrisoner(offenderNo, requestToRecall);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "Forbidden - user not authorised to receive prisoner on new bookings", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("*** BETA *** Receives a prisoner on a new booking. BOOKING_CREATE role")
    @PostMapping("/{offenderNo}/booking")
    @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail newBooking(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestForNewBooking requestForNewBooking) {
        return prisonerReleaseAndTransferService.newBooking(offenderNo, requestForNewBooking);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("*** BETA *** Marks a prisoner as in transit from their current prison location to a new prison. Must be an active prisoner in currently inside a prison, requires the TRANSFER_PRISONER role")
    @PutMapping("/{offenderNo}/transfer-out")
    @PreAuthorize("hasRole('TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail transferOutPrisoner(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToTransferOut requestToTransferOut) {
        return prisonerReleaseAndTransferService.transferOutPrisoner(offenderNo, requestToTransferOut);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "Forbidden - user not authorised to transfer a prisoner", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("*** BETA *** Transfer a prisoner into a prison. Must be an out prisoner in currently in transfer status, requires the TRANSFER_PRISONER role")
    @PutMapping("/{offenderNo}/transfer-in")
    @PreAuthorize("hasRole('TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail transferInPrisoner(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToTransferIn requestToTransferIn) {
        return prisonerReleaseAndTransferService.transferInPrisoner(offenderNo, requestToTransferIn);
    }

    @ApiResponses({
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Returns the next prisoner number (NOMS ID or Offender No) that can be used to create an offender")
    @GetMapping("/next-sequence")
    @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public PrisonerIdentifier getNextPrisonerIdentifier() {
        return prisonerCreationService.getNextPrisonerIdentifier();
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = IncidentCase.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Return a set Incidents for a given offender No.", notes = "Can be filtered by participation type and incident type")
    @GetMapping("/{offenderNo}/incidents")
    public List<IncidentCase> getIncidentsByOffenderNo(@PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo, @RequestParam("incidentType") @ApiParam(value = "incidentType", example = "ASSAULT", allowMultiple = true) final List<String> incidentTypes, @RequestParam("participationRoles") @ApiParam(value = "participationRoles", example = "ASSIAL", allowMultiple = true, allowableValues = "ACTINV,ASSIAL,FIGHT,IMPED,PERP,SUSASS,SUSINV,VICT,AI,PAS,AO") final List<String> participationRoles) {
        return incidentService.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List")})
    @ApiOperation(value = "Return a list of offender nos across the estate for which an incident has recently occurred or changed", notes = "This query is slow and can take several minutes")
    @GetMapping("/incidents/candidates")
    public ResponseEntity<List<String>> getIncidentCandidates(@RequestParam("fromDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam(value = "A recent timestamp that indicates the earliest time to consider. NOTE More than a few days in the past can result in huge amounts of data.", required = true, example = "2019-10-22T03:00") @NotNull final LocalDateTime fromDateTime, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first offender in returned list.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "1000", required = false) @ApiParam(value = "Requested limit to number of offenders returned.", defaultValue = "1000") final Long pageLimit) {
        var paged = incidentService.getIncidentCandidates(fromDateTime,
            nvl(pageOffset, 0L),
            nvl(pageLimit, 1000L));

        return ResponseEntity.ok().headers(paged.getPaginationHeaders()).body(paged.getItems());
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Return a list of addresses for a given offender, most recent first.")
    @GetMapping("/{offenderNo}/addresses")
    public List<AddressDto> getAddressesByOffenderNo(@PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @NotNull String offenderNo) {
        return addressService.getAddressesByOffenderNo(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AdjudicationSearchResponse.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Return a list of adjudications for a given offender")
    @GetMapping("/{offenderNo}/adjudications")
    public ResponseEntity<AdjudicationSearchResponse> getAdjudicationsByOffenderNo(@PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo,
                                                                                   @RequestParam(value = "offenceId", required = false) @ApiParam("An offence id to allow optionally filtering by type of offence") final String offenceId,
                                                                                   @RequestParam(value = "agencyId", required = false) @ApiParam("An agency id to allow optionally filtering by the agency in which the offence occurred") final String agencyId,
                                                                                   @RequestParam(value = "finding", required = false) @ApiParam(value = "Finding code to allow optionally filtering by type of finding", example = "NOT_PROVED") final String finding,
                                                                                   @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Adjudications must have been reported on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Adjudications must have been reported on or before this date (in YYYY-MM-DD format).") LocalDate toDate,
                                                                                   @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of adjudications.", defaultValue = "0") final Long pageOffset,
                                                                                   @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of adjudications returned.", defaultValue = "10") final Long pageLimit) {

        val criteria = AdjudicationSearchCriteria.builder()
            .offenderNumber(offenderNo)
            .offenceId(offenceId)
            .agencyId(agencyId)
            .findingCode(finding)
            .startDate(fromDate)
            .endDate(toDate)
            .pageRequest(new PageRequest(pageOffset, pageLimit))
            .build();

        val page = adjudicationService.findAdjudications(criteria);

        return ResponseEntity.ok()
            .headers(page.getPaginationHeaders())
            .body(AdjudicationSearchResponse.builder()
                .results(page.getItems())
                .offences(adjudicationService.findAdjudicationsOffences(criteria.getOffenderNumber()))
                .agencies(adjudicationService.findAdjudicationAgencies(criteria.getOffenderNumber()))
                .build());
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = AdjudicationDetail.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Return a specific adjudication")
    @GetMapping("/{offenderNo}/adjudications/{adjudicationNo}")
    public AdjudicationDetail getAdjudication(@PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo, @PathVariable("adjudicationNo") @ApiParam(value = "adjudicationNo", required = true) @NotNull final long adjudicationNo) {
        return adjudicationService.findAdjudication(offenderNo, adjudicationNo);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Alert.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Return a list of alerts for a given offender No.", notes = "System or cat tool access only")
    @GetMapping("/{offenderNo}/alerts")
    public List<Alert> getAlertsByOffenderNo(@PathVariable("offenderNo") @ApiParam(value = "Noms ID or Prisoner number", required = true, example = "A1234AA") @NotNull final String offenderNo, @RequestParam(value = "latestOnly", defaultValue = "true", required = false) @ApiParam("Only get alerts for the latest booking (prison term)") final Boolean latestOnly, @RequestParam(value = "query", required = false) @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - " +
        "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active</p> ", required = false, example = "alertCode:eq:'XA',or:alertCode:eq:'RSS'") final String query, @RequestHeader(value = "Sort-Fields", defaultValue = "bookingId,alertType", required = false) @ApiParam(value = "Comma separated list of one or more Alert fields", allowableValues = "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active", defaultValue = "bookingId,alertType") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order", defaultValue = "ASC") final Order sortOrder) {
        return alertService.getInmateAlertsByOffenderNos(
            offenderNo,
            nvl(latestOnly, true),
            query,
            StringUtils.defaultIfBlank(sortFields, "bookingId,alertId"),
            nvl(sortOrder, Order.ASC));
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = Alert.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Return a list of alerts for latest booking for a given offender No.", notes = "System or cat tool access only")
    @GetMapping("/{offenderNo}/bookings/latest/alerts")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION"})
    public List<Alert> getAlertsFotLatestBookingByOffenderNo(
        @PathVariable("offenderNo") @ApiParam(value = "Noms ID or Prisoner number", required = true, example = "A1234AA") @NotNull final String offenderNo,
        @RequestParam(value = "alertCodes", required = false) @ApiParam(value = "Comma separated list of alertCodes to filter by", example = "XA,RSS") final String alertCodes,
        @RequestParam(value = "sort", defaultValue = "alertType", required = false) @ApiParam(value = "Comma separated list of one or more Alert fields", allowableValues = "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active", defaultValue = "alertType") final String sort,
        @RequestParam(value = "direction", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order", defaultValue = "ASC", example = "DESC") final Order direction) {
        final var query = Optional.ofNullable(alertCodes).map(codes -> Arrays.stream(codes.split(","))
            .map(alertCode -> String.format("alertCode:eq:'%s'", alertCode))
            .collect(Collectors.joining(",or:"))).orElse(null);
        return alertService.getInmateAlertsByOffenderNos(
            offenderNo,
            true,
            query,
            sort,
            direction);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List")})
    @ApiOperation(value = "Return a list of offender nos across the estate for which an alert has recently been created or changed", notes = "This query is slow and can take several minutes")
    @GetMapping("/alerts/candidates")
    public ResponseEntity<List<String>> getAlertCandidates(@RequestParam("fromDateTime") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam(value = "A recent timestamp that indicates the earliest time to consider. NOTE More than a few days in the past can result in huge amounts of data.", required = true, example = "2019-11-22T03:00") @NotNull final LocalDateTime fromDateTime, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first offender in returned list.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "1000", required = false) @ApiParam(value = "Requested limit to number of offenders returned.", defaultValue = "1000") final Long pageLimit) {
        return alertService.getAlertCandidates(fromDateTime,
            nvl(pageOffset, 0L),
            nvl(pageLimit, 1000L)).getResponse();
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = CaseNote.class)})
    @ApiOperation(value = "Offender case notes", notes = "Retrieve an offenders case notes for latest booking")
    @GetMapping("/{offenderNo}/case-notes/v2")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Page<CaseNote> getOffenderCaseNotes(@PathVariable("offenderNo") @ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true, example = "A1234AA") final String offenderNo,
                                               @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "start contact date to search from", example = "2021-02-03") final LocalDate from,
                                               @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "end contact date to search up to (including this date)", example = "2021-02-04") final LocalDate to,
                                               @RequestParam(value = "type", required = false) @ApiParam(value = "Filter by case note type", example = "GEN") final String type,
                                               @RequestParam(value = "subType", required = false) @ApiParam(value = "Filter by case note sub-type", example = "OBS") final String subType,
                                               @RequestParam(value = "prisonId", required = false) @ApiParam(value = "Filter by the ID of the prison", example = "LEI") final String prisonId,
                                               @PageableDefault(sort = {"occurrenceDateTime"}, direction = Sort.Direction.DESC) final Pageable pageable) {

        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);

        final var caseNoteFilter = CaseNoteFilter.builder()
            .type(type)
            .subType(subType)
            .prisonId(prisonId)
            .startDate(from)
            .endDate(to)
            .bookingId(latestBookingByOffenderNo.getBookingId())
            .build();

        return caseNoteService.getCaseNotes(caseNoteFilter, pageable);
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = CaseNote.class)})
    @ApiOperation(value = "Offender case note detail.", notes = "Retrieve an single offender case note", nickname = "getOffenderCaseNote")
    @GetMapping("/{offenderNo}/case-notes/{caseNoteId}")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public CaseNote getOffenderCaseNote(@PathVariable("offenderNo") @ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true) final String offenderNo, @PathVariable("caseNoteId") @ApiParam(value = "The case note id", required = true) final Long caseNoteId) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        try {
            return caseNoteService.getCaseNote(latestBookingByOffenderNo.getBookingId(), caseNoteId);
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @ApiResponses({
        @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
        @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender. Will attach to the latest booking", nickname = "createOffenderCaseNote")
    @PostMapping("/{offenderNo}/case-notes")
    @HasWriteScope
    @ProxyUser
    public CaseNote createOffenderCaseNote(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of offender", required = true, example = "A1234AA") final String offenderNo, @RequestBody @ApiParam(value = "", required = true) final NewCaseNote body) {
        try {
            return caseNoteService.createCaseNote(offenderNo, body, authenticationFacade.getCurrentUsername());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @ApiResponses({
        @ApiResponse(code = 201, message = "Case Note amendment processed successfully. Updated case note is returned.", response = CaseNote.class),
        @ApiResponse(code = 400, message = "Invalid request - e.g. amendment text not provided.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "Forbidden - user not authorised to amend case note.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Resource not found - offender or case note does not exist or is not accessible to user.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    @ApiOperation(value = "Amend offender case note.", notes = "Amend offender case note.", nickname = "updateOffenderCaseNote")
    @PutMapping("/{offenderNo}/case-notes/{caseNoteId}")
    @HasWriteScope
    @ProxyUser
    public CaseNote updateOffenderCaseNote(@PathVariable("offenderNo") @ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true, example = "A1234AA") final String offenderNo, @PathVariable("caseNoteId") @ApiParam(value = "The case note id", required = true, example = "1212134") final Long caseNoteId, @RequestBody @ApiParam(value = "", required = true) final UpdateCaseNote body) {
        try {
            return caseNoteService.updateCaseNote(offenderNo, caseNoteId, authenticationFacade.getCurrentUsername(), body.getText());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = OffenderSentenceDetail.class)})
    @ApiOperation(value = "Offender Sentence Details", notes = "Retrieve an single offender sentence details", nickname = "getOffenderSentenceDetails")
    @GetMapping("/{offenderNo}/sentences")
    public OffenderSentenceDetail getOffenderSentenceDetail(@PathVariable("offenderNo") @ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true) final String offenderNo) {
        return bookingService.getOffenderSentenceDetail(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = OffenderNumber.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Return a list of all unique Noms IDs (also called Prisoner number and offenderNo).")
    @GetMapping("/ids")
    public ResponseEntity<List<OffenderNumber>> getOffenderNumbers(@RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first Noms ID in returned list.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "100", required = false) @ApiParam(value = "Requested limit to the Noms IDs returned.", defaultValue = "100") final Long pageLimit) {

        final var offenderNumbers = globalSearchService.getOffenderNumbers(
            nvl(pageOffset, 0L),
            nvl(pageLimit, 100L));

        return ResponseEntity.ok()
            .headers(offenderNumbers.getPaginationHeaders())
            .body(offenderNumbers.getItems());
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offenders IEP (Incentives & Earned Privileges) summary for the latest booking only.", notes = "Offenders IEP (Incentives & Earned Privileges) summary.", nickname = "getLatestBookingIEPSummaryForOffender")
    @GetMapping("/{offenderNo}/iepSummary")
    public PrivilegeSummary getLatestBookingIEPSummaryForOffender(@NotNull @PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") final String offenderNo, @RequestParam(value = "withDetails", required = false, defaultValue = "false") @ApiParam(value = "Toggle to return IEP detail entries in response (or not).", required = true) final boolean withDetails) {
        var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return bookingService.getBookingIEPSummary(booking.getBookingId(), withDetails);
    }

    @ApiResponses({
        @ApiResponse(code = 404, message = "Offender does not exists or is in a different caseload to the user", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Return a list of damage obligations")
    @GetMapping("/{offenderNo}/damage-obligations")
    public OffenderDamageObligationResponse getOffenderDamageObligations(@NotNull @PathVariable("offenderNo") @ApiParam(value = "offenderNo", required = true, example = "A1234AA") final String offenderNo, @RequestParam(value = "status", required = false, defaultValue = "ALL") @ApiParam(value = "Filter by obligation status. Leave blank to return all", required = false, example = "ACTIVE", allowableValues = "INACT,PAID,ONH,ACTIVE,APPEAL") final String status) {
        final var damageObligations = offenderDamageObligationService.getDamageObligations(offenderNo, lookupStatusOrDefaultToAll(status));
        return new OffenderDamageObligationResponse(damageObligations);
    }

    private Status lookupStatusOrDefaultToAll(final String status) {
        return Arrays.stream(Status.values())
            .filter(statusEnum -> status != null && statusEnum.name().equals(status.toUpperCase()))
            .findFirst()
            .orElse(Status.ALL);
    }

    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Not a digital offender. Offender has no account at this prison.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Prison, offender or accountType not found", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Retrieve an offender's financial transaction history for cash, spends or savings.",
        notes = "Transactions are returned in order of entryDate descending and sequence ascending).<br/>" +
            "All transaction amounts are represented as pence values.")
    @GetMapping("/{offenderNo}/transaction-history")
    public ResponseEntity<List<OffenderTransactionHistoryDto>> getTransactionsHistory(
        @ApiParam(name = "offenderNo", value = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo", required = true) @NotNull final String offenderNo,
        @ApiParam(name = "account_code", value = "Account code", example = "spends", required = false, allowableValues = "spends,cash,savings") @RequestParam(value = "account_code", required = false) final String accountCode,
        @ApiParam(name = "from_date", value = "Start date for transactions, format yyyy-MM-dd", example = "2019-04-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "from_date", required = false) final LocalDate fromDate,
        @ApiParam(name = "to_date", value = "To date for transactions, format yyyy-MM-dd", example = "2019-05-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "to_date", required = false) final LocalDate toDate,
        @ApiParam(name = "transaction_type", value = "Transaction type", example = "A_EARN") @RequestParam(value = "transaction_type", required = false) final String transactionType
    ) {
        var histories =
            offenderTransactionHistoryService.getTransactionHistory(offenderNo, accountCode, fromDate, toDate, transactionType);

        return ResponseEntity.ok(histories);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Military Records", notes = "Military Records", nickname = "getMilitaryRecords")
    @GetMapping("/{offenderNo}/military-records")
    public MilitaryRecords getMilitaryRecords(
        @ApiParam(name = "offenderNo", value = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo", required = true) @NotNull final String offenderNo
    ) {
        final var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        try {
            return bookingService.getMilitaryRecords(booking.getBookingId());
        } catch (EntityNotFoundException e) {
            // rethrow against the offender number rather than the booking id
            throw EntityNotFoundException.withId(offenderNo);
        }
    }
}
