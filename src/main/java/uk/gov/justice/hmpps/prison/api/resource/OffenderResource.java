package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
import uk.gov.justice.hmpps.prison.api.model.OffenderContacts;
import uk.gov.justice.hmpps.prison.api.model.OffenderDamageObligationResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderRestrictions;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.api.model.PrisonerIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PrisonerInPrisonSummary;
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn;
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking;
import uk.gov.justice.hmpps.prison.api.model.RequestForTemporaryAbsenceArrival;
import uk.gov.justice.hmpps.prison.api.model.RequestToCreate;
import uk.gov.justice.hmpps.prison.api.model.RequestToDischargePrisoner;
import uk.gov.justice.hmpps.prison.api.model.RequestToRecall;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOut;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToCourt;
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToTemporaryAbsence;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary;
import uk.gov.justice.hmpps.prison.api.model.UpdateCaseNote;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSearchResponse;
import uk.gov.justice.hmpps.prison.api.model.adjudications.OffenderAdjudicationHearing;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation.Status;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CaseNoteFilter;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.AdjudicationSearchCriteria;
import uk.gov.justice.hmpps.prison.service.AdjudicationService;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.IncidentService;
import uk.gov.justice.hmpps.prison.service.InmateAlertService;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.MovementsService;
import uk.gov.justice.hmpps.prison.service.OffenderAddressService;
import uk.gov.justice.hmpps.prison.service.OffenderDamageObligationService;
import uk.gov.justice.hmpps.prison.service.OffenderLocation;
import uk.gov.justice.hmpps.prison.service.OffenderLocationService;
import uk.gov.justice.hmpps.prison.service.OffenderTransactionHistoryService;
import uk.gov.justice.hmpps.prison.service.PrisonerTransferService;
import uk.gov.justice.hmpps.prison.service.enteringandleaving.BookingIntoPrisonService;
import uk.gov.justice.hmpps.prison.service.enteringandleaving.DischargeToHospitalService;
import uk.gov.justice.hmpps.prison.service.enteringandleaving.ReleasePrisonerService;
import uk.gov.justice.hmpps.prison.service.enteringandleaving.TransferIntoPrisonService;
import uk.gov.justice.hmpps.prison.service.enteringandleaving.PrisonerCreationService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@Tag(name = "offenders")
@Validated
@RequestMapping(value = "${api.base.path}/offenders", produces = "application/json")
@RequiredArgsConstructor
public class OffenderResource {

    private final IncidentService incidentService;
    private final InmateService inmateService;
    private final InmateAlertService alertService;
    private final OffenderAddressService addressService;
    private final AdjudicationService adjudicationService;
    private final CaseNoteService caseNoteService;
    private final BookingService bookingService;
    private final AuthenticationFacade authenticationFacade;
    private final PrisonerCreationService prisonerCreationService;
    private final PrisonerTransferService prisonerTransferService;
    private final OffenderDamageObligationService offenderDamageObligationService;
    private final OffenderTransactionHistoryService offenderTransactionHistoryService;
    private final MovementsService movementsService;
    private final BookingIntoPrisonService bookingIntoPrisonService;
    private final TransferIntoPrisonService transferIntoPrisonService;
    private final OffenderLocationService offenderLocationService;
    private final ReleasePrisonerService releasePrisonerService;
    private final DischargeToHospitalService dischargeToHospitalService;

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Full details about the current state of an offender")
    @GetMapping("/{offenderNo}")
    public InmateDetail getOffender(
        @RequestHeader(value = "version", defaultValue = "1.0", required = false) @Parameter(description = "Version of Offender details, default is 1.0, Beta is version 1.1_beta and is WIP (do not use in production)") final String version,
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Offender Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of offender", example = "A1234AA", required = true) final String offenderNo) {
        if ("1.1_beta".equals(version)) {
            // TODO: This is WIP as not all data is yet mapped
            return bookingService.getOffender(offenderNo);
        }
        return inmateService.findOffender(offenderNo, true, false);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Summary of the different periods this prisoner has been in prison.", description = "This is a summary of the different periods this prisoner has been in prison. It includes the dates of each period, the prison and the reason for the movement. Each booking is divided into periods of time spent in prison separated by periods when the were out either via a release or a temporary absence (periods at court are not included). The periods are ordered by date ascending, therefore the final period will be their last time in prison. For each period the prison admitted into and optionally released from will be listed. These can be different if there has been transfers in between the dates. Transfers are not listed but prisons the person was detailed at are listed in the unordered prison list")
    @GetMapping("/{offenderNo}/prison-timeline")
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'VIEW_PRISONER_DATA')")
    public PrisonerInPrisonSummary getOffenderPrisonPeriods(@Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Offender Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of offender", example = "A1234AA", required = true) final String offenderNo) {
        return movementsService.getPrisonerInPrisonSummary(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to create a prisoner.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Creates a prisoner and optional receives them into a prison by creating a new booking. BOOKING_CREATE role")
    @PostMapping
    @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail createPrisoner(@RequestBody @NotNull @Valid final RequestToCreate requestToCreate) {
        return prisonerCreationService.createPrisoner(requestToCreate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to release a prisoner.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "*** BETA *** Releases a prisoner from their current prison location. Must be an active prisoner in currently inside a prison, requires the RELEASE_PRISONER role")
    @PutMapping("/{offenderNo}/release")
    @PreAuthorize("hasRole('RELEASE_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    @VerifyOffenderAccess
    public InmateDetail releasePrisoner(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToReleasePrisoner requestToReleasePrisoner) {
        return releasePrisonerService.releasePrisoner(offenderNo, requestToReleasePrisoner, true);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to release a prisoner.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Discharges a prisoner to hospital, requires the RELEASE_PRISONER role")
    @PutMapping("/{offenderNo}/discharge-to-hospital")
    @PreAuthorize("hasRole('RELEASE_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail dischargePrisonerToHospital(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToDischargePrisoner requestToDischargePrisoner) {
        return dischargeToHospitalService.dischargeToHospital(offenderNo, requestToDischargePrisoner);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to recall a prisoner.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Recalls a prisoner into prison. TRANSFER_PRISONER role")
    @PutMapping("/{offenderNo}/recall")
    @PreAuthorize("hasRole('TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail recallPrisoner(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToRecall requestToRecall) {
        return bookingIntoPrisonService.recallPrisoner(offenderNo, requestToRecall);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to receive prisoner on new bookings", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Receives a prisoner on a new booking. BOOKING_CREATE role")
    @PostMapping("/{offenderNo}/booking")
    @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail newBooking(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestForNewBooking requestForNewBooking) {
        return bookingIntoPrisonService.newBooking(offenderNo, requestForNewBooking);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "*** BETA *** Marks a prisoner as in transit from their current prison location to a new prison. Must be an active prisoner in currently inside a prison, requires the TRANSFER_PRISONER role")
    @PutMapping("/{offenderNo}/transfer-out")
    @PreAuthorize("hasRole('TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail transferOutPrisoner(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToTransferOut requestToTransferOut) {
        return prisonerTransferService.transferOutPrisoner(offenderNo, requestToTransferOut);
    }
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "*** ALPHA *** transfer a prisoner to a court with the option to release the prisoners bed, requires the TRANSFER_PRISONER_ALPHA role")
    @PutMapping("/{offenderNo}/court-transfer-out")
    @PreAuthorize("hasRole('TRANSFER_PRISONER_ALPHA') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail transferOutPrisonerToCourt(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToTransferOutToCourt requestToTransferOut) {
        return prisonerTransferService.transferOutPrisonerToCourt(offenderNo, requestToTransferOut);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "*** ALPHA *** transfer a prisoner to a temporary absence with the option to release the prisoners bed, requires the TRANSFER_PRISONER_ALPHA role. Only support scenarios are unscheduled to city and scheduled to address")
    @PutMapping("/{offenderNo}/temporary-absence-out")
    @PreAuthorize("hasRole('TRANSFER_PRISONER_ALPHA') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail transferOutPrisonerToTemporaryAbsence(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToTransferOutToTemporaryAbsence requestToTransferOut) {
        return prisonerTransferService.transferOutPrisonerToTemporaryAbsence(offenderNo, requestToTransferOut);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to transfer a prisoner", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "*** BETA *** Transfer a prisoner into a prison. Must be an out prisoner in currently in transfer status, requires the TRANSFER_PRISONER role")
    @PutMapping("/{offenderNo}/transfer-in")
    @PreAuthorize("hasRole('TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail transferInPrisoner(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestToTransferIn requestToTransferIn) {
        return transferIntoPrisonService.transferInFromPrison(offenderNo, requestToTransferIn);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to transfer a prisoner", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Transfer a prisoner into a prison from court. Must be an out prisoner in currently in transfer status, requires the TRANSFER_PRISONER role")
    @PutMapping("/{offenderNo}/court-transfer-in")
    @PreAuthorize("hasRole('TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail courtTransferIn(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestForCourtTransferIn requestForCourtTransferIn) {
        return transferIntoPrisonService.transferInViaCourt(offenderNo, requestForCourtTransferIn);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to transfer a prisoner", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Transfer a prisoner into a prison from temporary absence. Must be an out prisoner in currently in TAP status, requires the TRANSFER_PRISONER role")
    @PutMapping("/{offenderNo}/temporary-absence-arrival")
    @PreAuthorize("hasRole('TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public InmateDetail temporaryAbsenceArrival(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Prisoner Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offenderNo of prisoner", example = "A1234AA", required = true) final String offenderNo,
        @RequestBody @NotNull @Valid final RequestForTemporaryAbsenceArrival requestForTemporaryAbsenceArrival) {
        return transferIntoPrisonService.transferInAfterTemporaryAbsence(offenderNo, requestForTemporaryAbsenceArrival);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Returns the next prisoner number (NOMS ID or Offender No) that can be used to create an offender")
    @GetMapping("/next-sequence")
    @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public PrisonerIdentifier getNextPrisonerIdentifier() {
        return prisonerCreationService.getNextPrisonerIdentifier();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return a set Incidents for a given offender No.", description = "Can be filtered by participation type and incident type")
    @GetMapping("/{offenderNo}/incidents")
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'VIEW_INCIDENTS')")
    public List<IncidentCase> getIncidentsByOffenderNo(@PathVariable("offenderNo") @Parameter(description = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo, @RequestParam("incidentType") @Parameter(description = "incidentType", example = "ASSAULT") final List<String> incidentTypes, @RequestParam("participationRoles") @Parameter(description = "participationRoles", example = "ASSIAL", schema = @Schema(implementation = String.class, allowableValues = {"ACTINV","ASSIAL","FIGHT","IMPED","PERP","SUSASS","SUSINV","VICT","AI","PAS","AO"})) final List<String> participationRoles) {
        return incidentService.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return a list of addresses for a given offender, most recent first.")
    @GetMapping("/{offenderNo}/addresses")
    @SlowReportQuery
    public List<AddressDto> getAddressesByOffenderNo(@PathVariable("offenderNo") @Parameter(description = "offenderNo", required = true, example = "A1234AA") @NotNull String offenderNo) {
        return addressService.getAddressesByOffenderNo(offenderNo);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AdjudicationSearchResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return a list of adjudications for a given offender",
        description = "Deprecated - use Adjudications API to get adjudications, requires VIEW_ADJUDICATIONS",
        deprecated = true, hidden = true)
    @GetMapping("/{offenderNo}/adjudications")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER","VIEW_ADJUDICATIONS", "VIEW_PRISONER_DATA"})
    public ResponseEntity<AdjudicationSearchResponse> getAdjudicationsByOffenderNo(@PathVariable("offenderNo") @Parameter(description = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo,
                                                                                   @RequestParam(value = "offenceId", required = false) @Parameter(description = "An offence id to allow optionally filtering by type of offence") final String offenceId,
                                                                                   @RequestParam(value = "agencyId", required = false) @Parameter(description = "An agency id to allow optionally filtering by the agency in which the offence occurred") final String agencyId,
                                                                                   @RequestParam(value = "finding", required = false) @Parameter(description = "Finding code to allow optionally filtering by type of finding", example = "NOT_PROVEN") final String finding,
                                                                                   @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "Adjudications must have been reported on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "Adjudications must have been reported on or before this date (in YYYY-MM-DD format).") LocalDate toDate,
                                                                                   @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of adjudications.") final Long pageOffset,
                                                                                   @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of adjudications returned.") final Long pageLimit) {

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
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AdjudicationDetail.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return a specific adjudication",
        description = "Deprecated - use Adjudications API to get adjudications, requires VIEW_ADJUDICATIONS",
        deprecated = true, hidden = true)
    @GetMapping("/{offenderNo}/adjudications/{adjudicationNo}")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "VIEW_ADJUDICATIONS"})
    public AdjudicationDetail getAdjudication(@PathVariable("offenderNo") @Parameter(description = "offenderNo", required = true, example = "A1234AA") @NotNull final String offenderNo, @PathVariable("adjudicationNo") @Parameter(description = "adjudicationNo", required = true) final long adjudicationNo) {
        return adjudicationService.findAdjudication(offenderNo, adjudicationNo);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return a list of alerts for latest booking for a given offender No.", description = "System or cat tool access only")
    @GetMapping("/{offenderNo}/bookings/latest/alerts")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION"})
    public List<Alert> getAlertsForLatestBookingByOffenderNo(
        @PathVariable("offenderNo") @Parameter(description = "Noms ID or Prisoner number", required = true, example = "A1234AA") @NotNull final String offenderNo,
        @RequestParam(value = "alertCodes", required = false) @Parameter(description = "Comma separated list of alertCodes to filter by", example = "XA,RSS") final String alertCodes,
        @RequestParam(value = "sort", defaultValue = "alertType", required = false) @Parameter(description = "Comma separated list of one or more Alert fields", schema = @Schema(implementation = String.class, allowableValues = {"alertId","bookingId","alertType","alertCode","comment","dateCreated","dateExpires","active"})) final String sort,
        @RequestParam(value = "direction", defaultValue = "ASC", required = false) @Parameter(description = "Sort order", example = "DESC") final String direction) {
        return alertService.getAlertsForLatestBookingForOffender(
            offenderNo,
            alertCodes,
            sort,
            Direction.fromString(direction));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return a list of alerts for all booking for a given offender No.", description = "System or cat tool access only")
    @GetMapping("/{offenderNo}/alerts/v2")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION"})
    public List<Alert> getAlertsForAllBookingByOffenderNo(
        @PathVariable("offenderNo") @Parameter(description = "Noms ID or Prisoner number", required = true, example = "A1234AA") @NotNull final String offenderNo,
        @RequestParam(value = "alertCodes", required = false) @Parameter(description = "Comma separated list of alertCodes to filter by", example = "XA,RSS") final String alertCodes,
        @RequestParam(value = "sort", defaultValue = "alertType", required = false) @Parameter(description = "Comma separated list of one or more Alert fields", schema = @Schema(implementation = String.class, allowableValues = {"alertId","bookingId","alertType","alertCode","comment","dateCreated","dateExpires","active"})) final String sort,
        @RequestParam(value = "direction", defaultValue = "ASC", required = false) @Parameter(description = "Sort order", example = "DESC") final String direction) {
        return alertService.getAlertsForAllBookingsForOffender(
            offenderNo,
            alertCodes,
            sort,
            Direction.fromString(direction));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseNote.class))})})
    @Operation(summary = "Offender case notes", description = "Retrieve an offenders case notes for latest booking", hidden = true)
    @GetMapping("/{offenderNo}/case-notes/v2")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_CASE_NOTES"})
    @SlowReportQuery
    public Page<CaseNote> getOffenderCaseNotes(@PathVariable("offenderNo") @Parameter(description = "Noms ID or Prisoner number (also called offenderNo)", required = true, example = "A1234AA") final String offenderNo,
                                               @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "start contact date to search from", example = "2021-02-03") final LocalDate from,
                                               @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DATE) @Parameter(description = "end contact date to search up to (including this date)", example = "2021-02-04") final LocalDate to,
                                               @RequestParam(value = "type", required = false) @Parameter(description = "Filter by case note type", example = "GEN") final String type,
                                               @RequestParam(value = "subType", required = false) @Parameter(description = "Filter by case note sub-type", example = "OBS") final String subType,
                                               @RequestParam(value = "prisonId", required = false) @Parameter(description = "Filter by the ID of the prison", example = "LEI") final String prisonId,
                                               @ParameterObject @PageableDefault(sort = {"occurrenceDateTime"}, direction = Sort.Direction.DESC) final Pageable pageable) {

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
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseNote.class))})})
    @Operation(summary = "Offender case note detail.", description = "Retrieve an single offender case note", hidden = true)
    @GetMapping("/{offenderNo}/case-notes/{caseNoteId}")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_CASE_NOTES"})
    public CaseNote getOffenderCaseNote(@PathVariable("offenderNo") @Parameter(description = "Noms ID or Prisoner number (also called offenderNo)", required = true) final String offenderNo, @PathVariable("caseNoteId") @Parameter(description = "The case note id", required = true) final Long caseNoteId) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        try {
            return caseNoteService.getCaseNote(latestBookingByOffenderNo.getBookingId(), caseNoteId);
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "The Case Note has been recorded. The updated object is returned including the status.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseNote.class))}),
        @ApiResponse(responseCode = "409", description = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Create case note for offender.", description = "Create case note for offender. Will attach to the latest booking", hidden = true)
    @PostMapping("/{offenderNo}/case-notes")
    @HasWriteScope
    @ProxyUser
    public CaseNote createOffenderCaseNote(@PathVariable("offenderNo") @Parameter(description = "The offenderNo of offender", required = true, example = "A1234AA") final String offenderNo, @RequestBody @Parameter(required = true) final NewCaseNote body) {
        try {
            return caseNoteService.createCaseNote(offenderNo, body, authenticationFacade.getCurrentUsername());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Case Note amendment processed successfully. Updated case note is returned.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseNote.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. amendment text not provided.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to amend case note.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Resource not found - offender or case note does not exist or is not accessible to user.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal server error.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Amend offender case note.", description = "Amend offender case note.", hidden = true)
    @PutMapping("/{offenderNo}/case-notes/{caseNoteId}")
    @HasWriteScope
    @ProxyUser
    public CaseNote updateOffenderCaseNote(@PathVariable("offenderNo") @Parameter(description = "Noms ID or Prisoner number (also called offenderNo)", required = true, example = "A1234AA") final String offenderNo, @PathVariable("caseNoteId") @Parameter(description = "The case note id", required = true, example = "1212134") final Long caseNoteId, @RequestBody @Parameter(required = true) final UpdateCaseNote body) {
        try {
            return caseNoteService.updateCaseNote(offenderNo, caseNoteId, authenticationFacade.getCurrentUsername(), body.getText());
        } catch (EntityNotFoundException e) {
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OffenderSentenceDetail.class))})})
    @Operation(summary = "Offender Sentence Details", description = "Retrieve an single offender sentence details")
    @GetMapping("/{offenderNo}/sentences")
    public OffenderSentenceDetail getOffenderSentenceDetail(@PathVariable("offenderNo") @Parameter(description = "Noms ID or Prisoner number (also called offenderNo)", required = true) final String offenderNo) {
        return bookingService.getOffenderSentenceDetail(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender Sentence Details", description = "Retrieve an single offender sentence details")
    @GetMapping("/{offenderNo}/booking/latest/sentence-summary")
    public SentenceSummary getLatestSentenceSummary(@PathVariable("offenderNo") @Parameter(description = "Noms ID or Prisoner number (also called offenderNo)", required = true) final String offenderNo) {
        return bookingService.getSentenceSummary(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Offender does not exists or is in a different caseload to the user", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Return a list of damage obligations")
    @GetMapping("/{offenderNo}/damage-obligations")
    public OffenderDamageObligationResponse getOffenderDamageObligations(@NotNull @PathVariable("offenderNo") @Parameter(description = "offenderNo", required = true, example = "A1234AA") final String offenderNo, @RequestParam(value = "status", required = false, defaultValue = "ALL") @Parameter(description = "Filter by obligation status. Leave blank to return all", example = "ACTIVE", schema = @Schema(implementation = String.class, allowableValues = {"INACT","PAID","ONH","ACTIVE","APPEAL"})) final String status) {
        final var damageObligations = offenderDamageObligationService.getDamageObligations(offenderNo, lookupStatusOrDefaultToAll(status));
        return new OffenderDamageObligationResponse(damageObligations);
    }

    private Status lookupStatusOrDefaultToAll(final String status) {
        return Arrays.stream(Status.values())
            .filter(statusEnum -> status != null && statusEnum.name().equals(status.toUpperCase()))
            .findFirst()
            .orElse(Status.ALL);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Not a digital offender. Offender has no account at this prison.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Prison, offender or accountType not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Retrieve an offender's financial transaction history for cash, spends or savings.",
        description = "Transactions are returned in order of entryDate descending and sequence ascending).<br/>" +
            "All transaction amounts are represented as pence values.")
    @GetMapping("/{offenderNo}/transaction-history")
    @SlowReportQuery
    public ResponseEntity<List<OffenderTransactionHistoryDto>> getTransactionsHistory(
        @Parameter(name = "offenderNo", description = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo") @NotNull final String offenderNo,
        @Parameter(name = "account_code", description = "Account code", example = "spends", schema = @Schema(implementation = String.class, allowableValues = {"spends","cash","savings"})) @RequestParam(value = "account_code", required = false) final String accountCode,
        @Parameter(name = "from_date", description = "Start date for transactions, format yyyy-MM-dd", example = "2019-04-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "from_date", required = false) final LocalDate fromDate,
        @Parameter(name = "to_date", description = "To date for transactions, format yyyy-MM-dd", example = "2019-05-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "to_date", required = false) final LocalDate toDate,
        @Parameter(name = "transaction_type", description = "Transaction type", example = "A_EARN") @RequestParam(value = "transaction_type", required = false) final String transactionType
    ) {
        var histories =
            offenderTransactionHistoryService.getTransactionHistory(offenderNo, accountCode, fromDate, toDate, transactionType);

        return ResponseEntity.ok(histories);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Military Records", description = "Military Records")
    @GetMapping("/{offenderNo}/military-records")
    public MilitaryRecords getMilitaryRecords(
        @Parameter(name = "offenderNo", description = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo") @NotNull final String offenderNo
    ) {
        final var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        try {
            return bookingService.getMilitaryRecords(booking.getBookingId());
        } catch (EntityNotFoundException e) {
            // rethrow against the offender number rather than the booking id
            throw EntityNotFoundException.withId(offenderNo);
        }
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender Contacts", description = "Active Contacts including restrictions, using latest offender booking  and including inactive contacts by default")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "OFFENDER_CONTACTS"})
    @GetMapping("/{offenderNo}/contacts")
    public OffenderContacts getOffenderContacts(
            @Parameter(name = "offenderNo", description = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo") @NotNull final String offenderNo,
            @Parameter(name = "approvedVisitorsOnly", description = "return only contacts approved for visits") @RequestParam(value = "approvedVisitorsOnly", required = false, defaultValue = "false") final boolean approvedVisitors,
            @Parameter(name = "activeOnly", description = "return only active contacts, nb visitors can be inactive contacts") @RequestParam(value = "activeOnly", required = false, defaultValue = "false") final boolean activeOnly
    ) {
        final var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return bookingService.getOffenderContacts(booking.getBookingId(), approvedVisitors, activeOnly);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Gets the offender visit restrictions for a given offender using the latest booking",
        description = "Get offender visit restrictions by offender No. <p>Requires a relationship (via caseload) with the offender or VISIT_SCHEDULER role.</p>")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "VISIT_SCHEDULER"})
    @GetMapping("/{offenderNo}/offender-restrictions")
    public OffenderRestrictions getVisitRestrictions(
            @Parameter(name = "offenderNo", description = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo") @NotNull final String offenderNo,
            @Parameter(name = "activeRestrictionsOnly", description = "return only restriction that are active (derived from startDate and expiryDate)") @RequestParam(value = "activeRestrictionsOnly", required = false, defaultValue = "true") final boolean activeRestrictionsOnly)
    {
        final var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return bookingService.getOffenderRestrictions(booking.getBookingId(),activeRestrictionsOnly );
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "All scheduled events for offender.", description = "All scheduled events for offender.")
    @GetMapping("/{offenderNo}/events")
    public List<ScheduledEvent> getEvents(
        @Parameter(name = "offenderNo", description = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo") @NotNull final String offenderNo,
        @Parameter(description = "Returned events must be scheduled on or after this date (in YYYY-MM-DD format).") @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DATE) final LocalDate fromDate,
        @Parameter(description = "Returned events must be scheduled on or before this date (in YYYY-MM-DD format).") @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DATE) final LocalDate toDate) {

        final var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return bookingService.getEvents(booking.getBookingId(), fromDate, toDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "All future (scheduled) events for offender", description = "All future events for offender that are in a scheduled and not cancelled state.")
    @GetMapping("/{offenderNo}/scheduled-events")
    public List<ScheduledEvent> getScheduledEvents(
        @Parameter(name = "offenderNo", description = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo") @NotNull final String offenderNo,
        @Parameter(description = "Returned events must be scheduled on or after this date (in YYYY-MM-DD format).  The from date must be on or after today.") @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DATE) final LocalDate fromDate,
        @Parameter(description = "Returned events must be scheduled on or before this date (in YYYY-MM-DD format).") @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DATE) final LocalDate toDate) {

        final var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return bookingService.getScheduledEvents(booking.getBookingId(), fromDate, toDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Housing location for prisoner", description = """
        <p>Housing location split out into different levels for a prisoner, or an empty response if the prisoner is not currently in a prison.</p>
        <p>There will be either 3 or 4 levels returned depending on the layout in NOMIS.
        Level 1 is the top level, so normally a wing or a house block and level 3 / 4 will be the individual cell.</p>
        <p>This endpoint returns the prison levels as recorded in NOMIS and may not accurately reflect the physical layout of the prison.
        For example Bristol has wings, spurs and landings, but this endpoint will only return wings and landings as spurs are not mapped in NOMIS.
        Another example is Moorland where 5-1-B-014 in NOMIS is Wing 5, Landing 1, Cell B and Cell 014, whereas in reality it should be Houseblock 5, Spur 1, Wing B and Cell 014 instead.
        This endpoint will therefore also return different information from Whereabouts API as that service re-maps the NOMIS layout to include spurs etc.</p>
        <p>If the current location is temporary (reception, court, tap, cell swap or early conditional licence) then the previous permanent location is also returned, provided
        that the location is at the same prison and they haven't moved to a different prison in the meantime.</p>
        <p>Requires a relationship (via caseload) with the prisoner or VIEW_PRISONER_DATA role.</p>
        """)
    @GetMapping("/{offenderNo}/housing-location")
    public OffenderLocation getHousingLocation(
        @Parameter(name = "offenderNo", description = "Offender No", example = "A1234AA", required = true) @PathVariable(value = "offenderNo") @NotNull final String offenderNo) {
        final var booking = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return offenderLocationService.getOffenderLocation(booking.getBookingId(), booking);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Gets a list of offender adjudication hearings", description = """
        <p>This endpoint returns a list of offender adjudication hearings for 1 or more offenders for a given date range and optional time slot.</p>
        <p>If the date range goes beyond 31 days then an exception will be thrown.</p>
        <p>At least one offender number must be supplied if not then an exception will be thrown.</p>
        <p>If the time slot is provided then the results will be further restricted to the hearings that fall in that time slot.</p>
        """)
    @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA', 'VIEW_ADJUDICATIONS')")
    @PostMapping("/adjudication-hearings")
    public List<OffenderAdjudicationHearing> getOffenderAdjudicationHearings(
        @Parameter(description = "The offender numbers. Offender numbers have the format:<b>G0364GX</b>", required = true) @RequestBody final Set<String> offenderNos,
        @RequestParam(value = "agencyId") String agencyId,
        @RequestParam(value = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "timeSlot", required = false) @Parameter(description = "AM, PM or ED") final TimeSlot timeSlot
        ) {
        return adjudicationService.findOffenderAdjudicationHearings(agencyId, fromDate, toDate, offenderNos, timeSlot);
    }
}
