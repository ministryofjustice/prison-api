package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.OffenderDamageObligationResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary;
import uk.gov.justice.hmpps.prison.api.model.UpdateCaseNote;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSearchResponse;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.model.OffenderDamageObligationModel;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderDamageObligationRepository;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = {"/offenders"})
@Validated
public interface OffenderResource {

    @GetMapping("/{offenderNo}")
    @ApiOperation(value = "Full details about the current state of an offender")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    InmateDetail getOffender(@ApiParam(value = "The offenderNo of offender", example = "A1234AA", required = true) @PathVariable("offenderNo") @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Offender Number format incorrect") final String offenderNo);

    @GetMapping("/next-sequence")
    @ApiOperation(value = "Returns the next prisoner number (NOMS ID or Offender No) that can be used to create an offender")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    PrisonerIdentifier getNextPrisonerIdentifier();

    @GetMapping("/{offenderNo}/incidents")
    @ApiOperation(value = "Return a set Incidents for a given offender No.",
            notes = "Can be filtered by participation type and incident type")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = IncidentCase.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<IncidentCase> getIncidentsByOffenderNo(@ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathVariable("offenderNo") @NotNull String offenderNo,
                                                  @ApiParam(value = "incidentType", example = "ASSAULT", allowMultiple = true) @RequestParam("incidentType") List<String> incidentTypes,
                                                  @ApiParam(value = "participationRoles", example = "ASSIAL", allowMultiple = true, allowableValues = "ACTINV,ASSIAL,FIGHT,IMPED,PERP,SUSASS,SUSINV,VICT,AI,PAS,AO") @RequestParam("participationRoles") List<String> participationRoles);

    @GetMapping("/incidents/candidates")
    @ApiOperation(value = "Return a list of offender nos across the estate for which an incident has recently occurred or changed",
            notes = "This query is slow and can take several minutes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List")})
    ResponseEntity<List<String>> getIncidentCandidates(@ApiParam(value = "A recent timestamp that indicates the earliest time to consider. NOTE More than a few days in the past can result in huge amounts of data.", required = true, example = "2019-10-22T03:00") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("fromDateTime") @NotNull LocalDateTime fromDateTime,
                                                       @ApiParam(value = "Requested offset of first offender in returned list.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                       @ApiParam(value = "Requested limit to number of offenders returned.", defaultValue = "1000") @RequestHeader(value = "Page-Limit", defaultValue = "1000", required = false) Long pageLimit);

    @GetMapping("/{offenderNo}/addresses")
    @ApiOperation(value = "Return a list of addresses for a given offender, most recent first.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<AddressDto> getAddressesByOffenderNo(@ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathVariable("offenderNo") @NotNull String offenderNo);

    @GetMapping("/{offenderNo}/adjudications")
    @ApiOperation(value = "Return a list of adjudications for a given offender")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AdjudicationSearchResponse.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<AdjudicationSearchResponse> getAdjudicationsByOffenderNo(@ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathVariable("offenderNo") @NotNull String offenderNo,
                                          @ApiParam(value = "An offence id to allow optionally filtering by type of offence") @RequestParam(value = "offenceId", required = false) String offenceId,
                                          @ApiParam(value = "An agency id to allow optionally filtering by the agency in which the offence occurred") @RequestParam(value = "agencyId", required = false) String agencyId,
                                          @ApiParam(value = "Finding code to allow optionally filtering by type of finding",example = "NOT_PROVED") @RequestParam(value = "finding", required = false) String finding,
                                          @ApiParam(value = "Adjudications must have been reported on or after this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                          @ApiParam(value = "Adjudications must have been reported on or before this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                          @ApiParam(value = "Requested offset of first record in returned collection of adjudications.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                          @ApiParam(value = "Requested limit to number of adjudications returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit);

    @GetMapping("/{offenderNo}/adjudications/{adjudicationNo}")
    @ApiOperation(value = "Return a specific adjudication")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AdjudicationDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    AdjudicationDetail getAdjudication(@ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathVariable("offenderNo") @NotNull String offenderNo,
                                       @ApiParam(value = "adjudicationNo", required = true) @PathVariable("adjudicationNo") @NotNull long adjudicationNo);

    @GetMapping("/{offenderNo}/alerts")
    @ApiOperation(value = "Return a list of alerts for a given offender No.", notes = "System or cat tool access only")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Alert.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<Alert> getAlertsByOffenderNo(@ApiParam(value = "Noms ID or Prisoner number", required = true, example = "A1234AA") @PathVariable("offenderNo") @NotNull String offenderNo,
                                                         @ApiParam(value = "Only get alerts for the latest booking (prison term)") @RequestParam(value = "latestOnly", defaultValue = "true", required = false) Boolean latestOnly,
                                                         @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - " +
                                                                 "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active</p> ",
                                                                 required = false, example = "alertCode:eq:'XA',or:alertCode:eq:'RSS'") @RequestParam(value = "query", required = false) String query,
                                                         @ApiParam(value = "Comma separated list of one or more Alert fields",
                                                                 allowableValues = "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active",
                                                                 defaultValue = "bookingId,alertType") @RequestHeader(value = "Sort-Fields", defaultValue = "bookingId,alertType", required = false) String sortFields,
                                                         @ApiParam(value = "Sort order", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/alerts/candidates")
    @ApiOperation(value = "Return a list of offender nos across the estate for which an alert has recently been created or changed",
            notes = "This query is slow and can take several minutes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List")})
    ResponseEntity<List<String>>  getAlertCandidates(@ApiParam(value = "A recent timestamp that indicates the earliest time to consider. NOTE More than a few days in the past can result in huge amounts of data.", required = true, example = "2019-11-22T03:00") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("fromDateTime") @NotNull LocalDateTime fromDateTime,
                                @ApiParam(value = "Requested offset of first offender in returned list.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                @ApiParam(value = "Requested limit to number of offenders returned.", defaultValue = "1000") @RequestHeader(value = "Page-Limit", defaultValue = "1000", required = false) Long pageLimit);

    @GetMapping("/{offenderNo}/case-notes")
    @ApiOperation(value = "Offender case notes", notes = "Retrieve an offenders case notes for latest booking", nickname = "getOffenderCaseNotes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = CaseNote.class, responseContainer = "List")})
    ResponseEntity<List<CaseNote>>  getOffenderCaseNotes(@ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true, example = "A1234AA") @PathVariable("offenderNo") String offenderNo,
                                  @ApiParam(value = "start contact date to search from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "from", required = false) LocalDate from,
                                  @ApiParam(value = "end contact date to search up to (including this date)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "to", required = false) LocalDate to,
                                  @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - creationDateTime, type, subType, source</p> ", required = true) @RequestParam(value = "query", required = false) String query,
                                  @ApiParam(value = "Requested offset of first record in returned collection of caseNote records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                  @ApiParam(value = "Requested limit to number of caseNote records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>creationDateTime, type, subType, source</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{offenderNo}/case-notes/{caseNoteId}")
    @ApiOperation(value = "Offender case note detail.", notes = "Retrieve an single offender case note", nickname = "getOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = CaseNote.class)})
    CaseNote getOffenderCaseNote(@ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true) @PathVariable("offenderNo") String offenderNo,
                                 @ApiParam(value = "The case note id", required = true) @PathVariable("caseNoteId") Long caseNoteId);

    @GetMapping("/{offenderNo}/sentences")
    @ApiOperation(value = "Offender Sentence Details", notes = "Retrieve an single offender sentence details", nickname = "getOffenderSentenceDetails")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = OffenderSentenceDetail.class)})
    OffenderSentenceDetail getOffenderSentenceDetail(@ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true) @PathVariable("offenderNo") String offenderNo);

    @PostMapping("/{offenderNo}/case-notes")
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender. Will attach to the latest booking", nickname = "createOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
            @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    CaseNote createOffenderCaseNote(@ApiParam(value = "The offenderNo of offender", required = true, example = "A1234AA") @PathVariable("offenderNo") String offenderNo,
                                    @ApiParam(value = "", required = true) @RequestBody NewCaseNote body);

    @PutMapping("/{offenderNo}/case-notes/{caseNoteId}")
    @ApiOperation(value = "Amend offender case note.", notes = "Amend offender case note.", nickname = "updateOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Case Note amendment processed successfully. Updated case note is returned.", response = CaseNote.class),
            @ApiResponse(code = 400, message = "Invalid request - e.g. amendment text not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to amend case note.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - offender or case note does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    CaseNote updateOffenderCaseNote(@ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true, example = "A1234AA") @PathVariable("offenderNo") String offenderNo,
                                    @ApiParam(value = "The case note id", required = true, example = "1212134") @PathVariable("caseNoteId") Long caseNoteId,
                                    @ApiParam(value = "", required = true) @RequestBody UpdateCaseNote body);

    @GetMapping("/ids")
    @ApiOperation(value = "Return a list of all unique Noms IDs (also called Prisoner number and offenderNo).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderNumber.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<List<OffenderNumber>> getOffenderNumbers(
            @ApiParam(value = "Requested offset of first Noms ID in returned list.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
            @ApiParam(value = "Requested limit to the Noms IDs returned.", defaultValue = "100") @RequestHeader(value = "Page-Limit", defaultValue = "100", required = false) Long pageLimit);

    @GetMapping("/{offenderNo}/iepSummary")
    @ApiOperation(value = "Offenders IEP (Incentives & Earned Privileges) summary for the latest booking only.", notes = "Offenders IEP (Incentives & Earned Privileges) summary.", nickname = "getLatestBookingIEPSummaryForOffender")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    PrivilegeSummary getLatestBookingIEPSummaryForOffender(
            @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathVariable("offenderNo") @NotNull String offenderNo,
            @ApiParam(value = "Toggle to return IEP detail entries in response (or not).", required = true) @RequestParam(value = "withDetails", required = false, defaultValue = "false") boolean withDetails);


    @GetMapping("/{offenderNo}/damage-obligations")
    @ApiOperation(value = "Return a list of damage obligations")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Offender does not exists", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    OffenderDamageObligationResponse getOffenderDamageObligations(
            @ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathVariable("offenderNo") @NotNull String offenderNo,
            @ApiParam(value = "Filter by obligation status. Leave blank to return all", required = false, example = "ACTIVE") @RequestParam(value = "status", required = false, defaultValue = "") String status);

}
