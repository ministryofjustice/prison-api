package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountBalance;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountTransactions;
import uk.gov.justice.hmpps.prison.api.model.v1.ActiveOffender;
import uk.gov.justice.hmpps.prison.api.model.v1.Alerts;
import uk.gov.justice.hmpps.prison.api.model.v1.AvailableDates;
import uk.gov.justice.hmpps.prison.api.model.v1.Bookings;
import uk.gov.justice.hmpps.prison.api.model.v1.ContactList;
import uk.gov.justice.hmpps.prison.api.model.v1.CreateTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Event;
import uk.gov.justice.hmpps.prison.api.model.v1.Events;
import uk.gov.justice.hmpps.prison.api.model.v1.Hold;
import uk.gov.justice.hmpps.prison.api.model.v1.Image;
import uk.gov.justice.hmpps.prison.api.model.v1.LiveRoll;
import uk.gov.justice.hmpps.prison.api.model.v1.Location;
import uk.gov.justice.hmpps.prison.api.model.v1.Offender;
import uk.gov.justice.hmpps.prison.api.model.v1.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.v1.PaymentResponse;
import uk.gov.justice.hmpps.prison.api.model.v1.StorePaymentRequest;
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Transfer;
import uk.gov.justice.hmpps.prison.api.model.v1.UnavailabilityReason;
import uk.gov.justice.hmpps.prison.api.model.v1.VisitSlots;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.v1.NomisApiV1Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;

import static uk.gov.justice.hmpps.prison.util.DateTimeConverter.optionalStrToLocalDateTime;
import static uk.gov.justice.hmpps.prison.util.ResourceUtils.getUniqueClientId;

@RestController
@Tag(name = "v1")
@Validated
@RequestMapping("${api.base.path}/v1")
public class NomisApiV1Resource {

    public static final String NOMS_ID_REGEX_PATTERN = "[a-zA-Z][0-9]{4}[a-zA-Z]{2}";
    public static final String CLIENT_UNIQUE_REF_PATTERN = "[a-zA-Z0-9-_]+";
    private final NomisApiV1Service service;

    public NomisApiV1Resource(final NomisApiV1Service service) {
        this.service = service;
    }


    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Returns general offender information.")
    @GetMapping("/offenders/{noms_id}")
    public Offender getOffender(@PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms ID", example = "A1417AE", required = true) @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getOffender(nomsId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get Current Photograph of the offender", description = "Returns a 480wx600h JPEG photograph of the offender. The data is base64 encoded within the image key.")
    @GetMapping("/offenders/{noms_id}/image")
    public Image getOffenderImage(@PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms ID", example = "A1417AE", required = true) @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getOffenderImage(nomsId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Current Location of the offender", description = "The levels shows the type of each level of the location address as defined on the Agency Details tab in Maintain Agency Locations screen (OUMAGLOC).<br/><br/>Since the offender's location can change often and is fairly sensitive (and therefore should not automatically be exposed to all services), this information is not included in the general offender information call.")
    @GetMapping("/offenders/{noms_id}/location")
    public Location getLatestBookingLocation(@PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms ID", example = "A1417AE", required = true) @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getLatestBookingLocation(nomsId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Legal cases for each booking and charges within each legal case.", description = "Returns all the bookings, the legal cases for each booking and charges within each legal case.<br/>" +
            "The ordering is as follows:<ul>" +
            "<li><strong>bookings</strong>: Current or latest booking first, others in descending order of booking date</li>" +
            "<li><strong>legal_cases</strong>: Active cases followed by inactive cases, further ordered by begin_date, latest first</li>" +
            "<li><strong>charges</strong>: Most serious active charge first, then remaining active charges, followed by inactive charges</li></ul>")
    @GetMapping("/offenders/{noms_id}/charges")
    public Bookings getBookings(@PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms ID", example = "A1417AE", required = true) @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getBookings(nomsId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid Noms ID", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Offender not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetch alerts by offender", description = "Returns all active alerts for the specified offender or those that meet the optional criteria. Active alerts are listed first, followed by inactive alerts, both sorted by ascending order of alert date.<br/>" +
            "<ul><li>if alert_type is specified then only alerts of that type are returned</li>" +
            "<li>if modified_since is specified then only those alerts created or modified on or after the specified date time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123</li>" +
            "<li>If include_inactive=true is specified then inactive alerts are also returned.</li></ul>")
    @GetMapping("/offenders/{noms_id}/alerts")
    public Alerts getAlerts(@Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1583AE", required = true) final String nomsId, @RequestParam(value = "alert_type", required = false) @Parameter(name = "alert_type", description = "Alert Type, if alert_type is specified then only alerts of that type are returned", example = "H") final String alertType, @RequestParam(value = "modified_since", required = false) @Parameter(name = "modified_since", description = "Modified Since - if modified_since is specified then only those alerts created or modified on or after the specified date time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123", example = "2017-10-07T12:23:45.678") final String modifiedSince, @RequestParam(value = "include_inactive", required = false, defaultValue = "false") @Parameter(name = "include_inactive", description = "Include Inactive alerts, If include_inactive=true is specified then inactive alerts are also returned.", example = "true") final boolean includeInactive) {
        final var alerts = service.getAlerts(nomsId, includeInactive, optionalStrToLocalDateTime(modifiedSince)).stream()
                .filter(a -> alertType == null || a.getType().getCode().equalsIgnoreCase(alertType))
                .toList();
        return Alerts.builder().alerts(alerts).build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid Noms ID", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Offender not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetch events", description = "Returns all events that required to update the prisoner self service application. Currently these are:" +
            "<ul><li>ALERT</li>" +
            "<li>DISCHARGE</li>" +
            "<li>IEP_CHANGED</li>" +
            "<li>INTERNAL_LOCATION_CHANGED</li>" +
            "<li>NOMS_ID_CHANGED</li>" +
            "<li>PERSONAL_DETAILS_CHANGED</li>" +
            "<li>PERSONAL_OFFICER_CHANGED</li>" +
            "<li>RECEPTION</li>" +
            "<li>SENTENCE_INFORMATION_CHANGED</li>" +
            "<li>BALANCE_UPDATE</li></ul>")
    @GetMapping("/offenders/events")
    public Events getOffenderEvents(@Size(max = 3) @RequestParam("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "BMI") final String prisonId, @RequestParam(value = "offender_id", required = false) @Parameter(name = "offender_id", description = "Offender Noms Id", example = "A1417AE") final String offenderIdentifier, @RequestParam(value = "event_type", required = false) @Parameter(name = "event_type", description = "Event Type", example = "ALERT") final String eventType, @RequestParam("from_datetime") @Parameter(name = "from_datetime", description = "From Date Time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123", example = "2017-10-07T12:23:45.678") final String fromDateTime, @RequestParam(value = "limit", required = false) @Parameter(name = "limit", description = "Number of events to return", example = "100") final Long limit) {
        final var events = service.getEvents(prisonId, new OffenderIdentifier(offenderIdentifier), eventType, optionalStrToLocalDateTime(fromDateTime), limit);
        return new Events(events);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "One of: <ul><li>Invalid transaction type - The transaction type has not been set up for the API for {prison_id}</li>" +
                    "<li>Finance Exception - This indicates an unexpected financial problem, check the nomis_api_log table for details.</li>" +
                    "<li>Only receipt transaction types allowed - Only transaction types with a transaction usage of Receipt are allowed.</li>" +
                    "<li>Sum of sub account balances not equal to current balance - The sum of all the sub account balances does equal the current balance held for the trust account</li>" +
                    "<li>Offender being transferred - The offender is currently in transit</li>" +
                    "<li>Offender still in specified prison - The offender is still at the specified prison. Use Record Transaction instead.</li></ul>", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "One of: <ul><li>Offender Not Found - No offender matching the specified offender_id has been found on nomis.</li>" +
                    "<li>Offender never at prison - The offender has never been at the specified prison</li></ul>", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Duplicate Post - A transaction already exists with the client_unique_ref provided.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Record transaction at previous Prison.", description = "<p>Post a financial transaction to Nomis to a prison that the offender is no longer at.</p>" +
            "<p>The valid prison_id and type combinations are defined in the Nomis transaction_operations table which is maintained by the Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. Only those prisons (Caseloads) and Transaction types associated with the NOMISAPI module are valid. Only Transaction types with a usage of R (Receipt) are valid." +
            "Transaction Types are maintained via the Maintain Transaction Types screen (OCMTRANS).</p>" +
            "<p>Transaction is posted to the specified prison.  if the account has been closed at this prison then it is re-opened first.</p>" +
            "<p>If the offender has been transferred to another prison then the funds are transferred to this prison.</p>" +
            "<p>If the account was previously closed then it will be closed again.</p>" +
            "<p>If the offender has been released then the funds are transferred to NACRO. Based on the Nomis Clear Inactive accounts screen (OTDCLINA).</p>")
    @PostMapping("/prison/{previous_prison_id}/offenders/{noms_id}/transfer_transactions")
    @HasWriteScope
    @ProxyUser
    public Transfer transferTransaction(@RequestHeader(value = "X-Client-Name", required = false) @Parameter(name = "X-Client-Name", description = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") final String clientName, @Size(max = 3) @NotNull @PathVariable("previous_prison_id") @Parameter(name = "previous_prison_id", description = "Prison ID", example = "BMI", required = true) final String previousPrisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1417AE", required = true) final String nomsId,
                                        @javax.validation.Valid @NotNull @RequestBody @Parameter(description = "Transaction Details", required = true) final CreateTransaction createTransaction) {

        final var uniqueClientId = getUniqueClientId(clientName, createTransaction.getClientUniqueRef());

        final var transfer = service.transferTransaction(previousPrisonId, nomsId,
                createTransaction.getType(), createTransaction.getDescription(),
                createTransaction.getAmountInPounds(), LocalDate.now(),
                createTransaction.getClientTransactionId(), uniqueClientId);

        return new Transfer(transfer.getCurrentLocation(), new Transaction(transfer.getTransaction().getId()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction Created", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class))}),
            @ApiResponse(responseCode = "400", description = "One of: <ul><li>Insufficient Funds - The prisoner has insufficient funds in the required account to cover the cost of the debit transaction</li>" +
                    "<li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li>" +
                    "<li>Invalid transaction type - The transaction type has not been set up for the API for {prison_id}</li>" +
                    "<li>Finance Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Duplicate post - The unique_client_ref has been used before", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Post a financial transaction to NOMIS.", description = "The valid prison_id and type combinations are defined in the Nomis transaction_operations table which is maintained by the Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. Only those prisons (Caseloads) and Transaction types associated with the NOMISAPI module are valid.<br/>" +
            "This will be setup by script intially as part of the deployment process as shown below<br/><br/>" +
            "<table>" +
            "<tr><th>Transaction Type</th><th>Description</th><th>Digital Prison</th><th>Non Digital Prison</th></tr>" +
            "<tr><td>CANT</td><td>Canteen Spend</td><td>Yes</td><td>No</td></tr>" +
            "<tr><td>REFND</td><td>Canteen Refund</td><td>Yes</td><td>No</td></tr>" +
            "<tr><td>PHONE</td><td>Phone Credit</td><td>Yes</td><td>No</td></tr>" +
            "<tr><td>MRPR</td><td>Misc Receipt - Private Cash</td><td>Yes</td><td>Yes</td></tr>" +
            "<tr><td>MTDS</td><td>Money through digital service</td><td>Yes</td><td>Yes</td></tr>" +
            "<tr><td>DTDS</td><td>Disbursement through Digital service</td><td>Yes</td><td>Yes</td></tr>" +
            "<tr><td>CASHD</td><td>Cash Disbursement</td><td>Yes</td><td>Yes</td></tr>" +
            "<tr><td>RELA</td><td>Money to Relatives</td><td>Yes</td><td>Yes</td></tr>" +
            "<tr><td>RELS</td><td>Money to Relatives- Spends</td><td>Yes</td><td>Yes</td></tr>" +
            "</table>Notes:<br/><ul>" +
            "<li>The sub_account the amount is debited or credited from will be determined by the transaction_type definition in NOMIS.</li>" +
            "<li>If the field X-Client-Name is present in the request header then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.</li>" +
            "<li>The client_unique_ref can have a maximum of 64 characters, only alphabetic, numeric, ‘-’ and ‘_’ characters are allowed</li></ul>")
    @PostMapping("/prison/{prison_id}/offenders/{noms_id}/transactions")
    @HasWriteScope
    @ProxyUser
    public Transaction createTransaction(@RequestHeader(value = "X-Client-Name", required = false) @Parameter(name = "X-Client-Name", description = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") final String clientName, @Size(max = 3) @NotNull @PathVariable("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "BMI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1417AE", required = true) final String nomsId,
                                         @javax.validation.Valid @NotNull @RequestBody @Parameter(description = "Transaction Details", required = true) final CreateTransaction createTransaction) {

        final var uniqueClientId = getUniqueClientId(clientName, createTransaction.getClientUniqueRef());

        final var result = service.createTransaction(prisonId, nomsId,
                createTransaction.getType(), createTransaction.getDescription(),
                createTransaction.getAmountInPounds(), LocalDate.now(),
                createTransaction.getClientTransactionId(), uniqueClientId);

        return new Transaction(result);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Offender Not Found - No offender matching the specified offender_id has been found on nomis.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Offender not in specified prison", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get holds.", description = "Gets every hold on an offender’s account or just the hold identified by the client_unique_ref")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/holds")
    public List<Hold> getHolds(@RequestHeader(value = "X-Client-Name", required = false) @Parameter(name = "X-Client-Name", description = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") final String clientName, @Size(max = 3) @NotNull @PathVariable("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "BMI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1417AE", required = true) final String nomsId, @Pattern(regexp = CLIENT_UNIQUE_REF_PATTERN) @Size(max = 64) @RequestParam(value = "client_unique_ref", required = false) @Parameter(name = "client_unique_ref", description = "Client unique reference") final String clientUniqueRef) {
        final var uniqueClientId = getUniqueClientId(clientName, clientUniqueRef);
        return service.getHolds(prisonId, nomsId, uniqueClientId, clientName);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Not a digital prison.  Prison not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetching live roll.")
    @GetMapping("/prison/{prison_id}/live_roll")
    public LiveRoll getLiveRoll(@Size(max = 3) @NotNull @PathVariable("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "BMI", required = true) final String prisonId) {
        return new LiveRoll(service.getLiveRoll(prisonId));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid Noms ID", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Offender not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get the PSS detail by offender", description = "Returns the PSS detail information for the specified offender including personal data, warnings, sentence details and location information.<br/>" +
            "<ul><li>The 'type' field is always OFFENDER_DETAILS_REQUEST</li><br/>" +
            "<li>The field 'offender_details_request' contains a JSON block of data containing the offender data.</li></ul>" +
            "The format of 'offender_details_request' is not specified here.")
    @GetMapping("/offenders/{noms_id}/pss_detail")
    public Event getOffenderPssDetail(@Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId) {
        return service.getOffenderPssDetail(nomsId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "One of: <ul><li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li><li>Invalid payment type</li>" +
                    "<li>Client reference more than 12 characters</li><li>Missing data in request</li>" +
                    "<li>Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested prison or offender could not be found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Duplicate post - after an error with a post this response will be given for subsequent duplicate attempts", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Store a payment for an offender account.", description = "Pay events will be stored in a table on receipt by Nomis to be processed by a batch job scheduled to run after the last Nomis payroll batch job but before the advances and scheduled payments batch jobs.\n" +
            "<br/>" +
            "Possible payment types are:<br/>" +
            "<table>" +
            "<tr><td>A_EARN</td><td>Credit, Offender Payroll</td></tr>" +
            "<tr><td>ADJ</td><td>Debit, Adjudication Award</td></tr>" +
            "</table><br/>Example request:<br/>" +
            "{\n" +
            "  \"type\": \"A_EARN\",\n" +
            "  \"description\": \"May earnings\",\n" +
            "  \"amount\": 1,\n" +
            "  \"client_transaction_id\": \"PAY-05-19\"\n" +
            "}" +
            "<br/>" +
            "The valid prison_id and type combinations are defined in the Nomis transaction_operations table which is maintained by the Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. Only those prisons (Caseloads) and Transaction types associated with the NOMISAPI module are valid.<br/>" +
            "This will be setup by script intially as part of the deployment process as shown below<br/><br/>")
    @PostMapping("/prison/{prison_id}/offenders/{noms_id}/payment")
    @HasWriteScope
    @ProxyUser
    public PaymentResponse storePayment(@Size(max = 3) @NotNull @PathVariable("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "BMI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1417AE", required = true) final String nomsId, @javax.validation.Valid @NotNull @RequestBody @Parameter(description = "Transaction Details", required = true) final StorePaymentRequest payment) {
        return service.storePayment(prisonId, nomsId, payment.getType(), payment.getDescription(), payment.getAmountInPounds(), LocalDate.now(), payment.getClientTransactionId());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Not a digital prison.  Prison not found. Offender has no account at this prison.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Prison or offender was not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Retrieve an offender's financial account balances.", description = "Returns balances for the offender’s three sub accounts (spends, savings and cash) at the specified prison.<br/>" +
            "All balance values are represented as pence values.")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/accounts")
    public AccountBalance getAccountBalance(@Size(max = 3) @NotNull @PathVariable("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "WLI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId) {
        return service.getAccountBalances(prisonId, nomsId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Not a digital prison.  Prison not found. Offender has no account at this prison.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Prison, offender or accountType not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Retrieve an offender's financial transaction history for cash, spends or savings.", description = "Transactions are returned in NOMIS ordee (Descending date followed by id).<br/>" +
            "All transaction amounts are represented as pence values.")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/accounts/{account_code}/transactions")
    public AccountTransactions getAccountTransactions(@Size(max = 3) @NotNull @PathVariable("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "WLI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId, @NotNull @PathVariable("account_code") @Parameter(name = "account_code", description = "Account code", example = "spends", required = true, schema = @Schema(implementation = String.class, allowableValues = {"spends","cash","savings"})) final String accountCode, @RequestParam(value = "from_date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(name = "from_date", description = "Start date for transactions (defaults to today if not supplied)", example = "2019-04-01") final LocalDate fromDate, @RequestParam(value = "to_date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(name = "to_date", description = "To date for transactions (defaults to today if not supplied)", example = "2019-05-01") final LocalDate toDate) {
        final var transactions = service.getAccountTransactions(prisonId, nomsId, accountCode, fromDate, toDate);
        return new AccountTransactions(transactions);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Not a digital prison.  Prison not found. Offender has no account at this prison.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Prison, offender or accountType not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Retrieve a single financial transaction using client unique ref.", description = "All transaction amounts are represented as pence values.")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/transactions/{client_unique_ref}")
    public AccountTransaction getTransactionByClientUniqueRef(@RequestHeader(value = "X-Client-Name", required = false) @Parameter(name = "X-Client-Name", description = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") final String clientName, @Size(max = 3) @NotNull @PathVariable("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "WLI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId, @Pattern(regexp = CLIENT_UNIQUE_REF_PATTERN) @Size(max = 64) @PathVariable("client_unique_ref") @Parameter(name = "client_unique_ref", description = "Client unique reference", required = true) final String clientUniqueRef) {
        final var uniqueClientId = getUniqueClientId(clientName, clientUniqueRef);

        return service.getTransactionByClientUniqueRef(prisonId, nomsId, uniqueClientId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid Noms ID", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Parameter exception (invalid date, time, format, type)", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Retrieve active offender", description = "offender id will be returned if offender is found")
    @GetMapping("/lookup/active_offender")
    public ActiveOffender getActiveOffender(@Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @RequestParam("noms_id") @Parameter(name = "noms_id", description = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId, @RequestParam(value = "date_of_birth", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @Parameter(name = "date_of_birth", description = "date of birth", example = "2019-05-01") final LocalDate birthDate) {
        return service.getActiveOffender(nomsId, birthDate);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid start and end date range", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Offender not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetch available_dates for offender", description = "returns list of dates")
    @GetMapping("offenders/{offender_id}/visits/available_dates")
    public AvailableDates getVisitAvailableDates(@PathVariable("offender_id") @NotNull @Parameter(name = "offender_id", description = "Offender Id", example = "1234567", required = true) final Long offenderId, @RequestParam("start_date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @Parameter(name = "start_date", description = "Start date", example = "2019-04-01", required = true) final LocalDate fromDate, @RequestParam("end_date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @Parameter(name = "end_date", description = "To date", example = "2019-05-01", required = true) final LocalDate toDate) {
        return service.getVisitAvailableDates(offenderId, fromDate, toDate);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid start and end date range", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Offender not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetch contacts list for offender", description = "returns list of contacts")
    @GetMapping("offenders/{offender_id}/visits/contact_list")
    public ContactList getVisitContactList(@PathVariable("offender_id") @NotNull @Parameter(name = "offender_id", description = "Offender Id", example = "1234567", required = true) final Long offenderId) {
        return service.getVisitContactList(offenderId);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Dates requested must be in future", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Offender not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetch unavailability reason for dates", description = "returns list of reason if unavailable date")
    @GetMapping("offenders/{offender_id}/visits/unavailability")
    public SortedMap<String, UnavailabilityReason> getVisitUnavailability(@PathVariable("offender_id") @NotNull @Parameter(name = "offender_id", description = "Offender Id", example = "1234567", required = true) final Long offenderId, @RequestParam("dates") @Parameter(name = "dates", description = "dates", example = "2019-05-01,2019-05-02", required = true) final String dates) {
        return service.getVisitUnavailability(offenderId, dates);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid start and end date range", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Prison Not Found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetch visit slots with capacity", description = "returns list slots with capacity details")
    @GetMapping("prison/{prison_id}/slots")
    public VisitSlots getVisitSlotsWithCapacity(@Size(max = 3) @PathVariable("prison_id") @Parameter(name = "prison_id", description = "Prison ID", example = "BMI", required = true) final String prisonId, @RequestParam("start_date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @Parameter(name = "start_date", description = "Start date", example = "2019-04-01", required = true) final LocalDate fromDate, @RequestParam("end_date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @Parameter(name = "end_date", description = "End date", example = "2019-05-01", required = true) final LocalDate toDate) {
        return service.getVisitSlotsWithCapacity(prisonId, fromDate, toDate);
    }
}
