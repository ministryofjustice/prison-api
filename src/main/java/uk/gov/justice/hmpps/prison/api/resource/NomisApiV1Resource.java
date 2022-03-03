package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import java.util.stream.Collectors;

import static uk.gov.justice.hmpps.prison.util.DateTimeConverter.optionalStrToLocalDateTime;
import static uk.gov.justice.hmpps.prison.util.ResourceUtils.getUniqueClientId;

@RestController
@Api(tags = {"v1"})
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
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Returns general offender information.")
    @GetMapping("/offenders/{noms_id}")
    public Offender getOffender(@PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getOffender(nomsId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Get Current Photograph of the offender", notes = "Returns a 480wx600h JPEG photograph of the offender. The data is base64 encoded within the image key.")
    @GetMapping("/offenders/{noms_id}/image")
    public Image getOffenderImage(@PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getOffenderImage(nomsId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Current Location of the offender", notes = "The levels shows the type of each level of the location address as defined on the Agency Details tab in Maintain Agency Locations screen (OUMAGLOC).<br/><br/>Since the offender's location can change often and is fairly sensitive (and therefore should not automatically be exposed to all services), this information is not included in the general offender information call.")
    @GetMapping("/offenders/{noms_id}/location")
    public Location getLatestBookingLocation(@PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getLatestBookingLocation(nomsId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Legal cases for each booking and charges within each legal case.", notes = "Returns all the bookings, the legal cases for each booking and charges within each legal case.<br/>" +
            "The ordering is as follows:<ul>" +
            "<li><strong>bookings</strong>: Current or latest booking first, others in descending order of booking date</li>" +
            "<li><strong>legal_cases</strong>: Active cases followed by inactive cases, further ordered by begin_date, latest first</li>" +
            "<li><strong>charges</strong>: Most serious active charge first, then remaining active charges, followed by inactive charges</li></ul>")
    @GetMapping("/offenders/{noms_id}/charges")
    public Bookings getBookings(@PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getBookings(nomsId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Fetch alerts by offender", notes = "Returns all active alerts for the specified offender or those that meet the optional criteria. Active alerts are listed first, followed by inactive alerts, both sorted by ascending order of alert date.<br/>" +
            "<ul><li>if alert_type is specified then only alerts of that type are returned</li>" +
            "<li>if modified_since is specified then only those alerts created or modified on or after the specified date time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123</li>" +
            "<li>If include_inactive=true is specified then inactive alerts are also returned.</li></ul>")
    @GetMapping("/offenders/{noms_id}/alerts")
    public Alerts getAlerts(@Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1583AE", required = true) final String nomsId, @RequestParam(value = "alert_type", required = false) @ApiParam(name = "alert_type", value = "Alert Type, if alert_type is specified then only alerts of that type are returned", example = "H") final String alertType, @RequestParam(value = "modified_since", required = false) @ApiParam(name = "modified_since", value = "Modified Since - if modified_since is specified then only those alerts created or modified on or after the specified date time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123", example = "2017-10-07T12:23:45.678") final String modifiedSince, @RequestParam(value = "include_inactive", required = false, defaultValue = "false") @ApiParam(name = "include_inactive", value = "Include Inactive alerts, If include_inactive=true is specified then inactive alerts are also returned.", example = "true", defaultValue = "false") final boolean includeInactive) {
        final var alerts = service.getAlerts(nomsId, includeInactive, optionalStrToLocalDateTime(modifiedSince)).stream()
                .filter(a -> alertType == null || a.getType().getCode().equalsIgnoreCase(alertType))
                .toList();
        return Alerts.builder().alerts(alerts).build();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Fetch events", notes = "Returns all events that required to update the prisoner self service application. Currently these are:" +
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
    public Events getOffenderEvents(@Size(max = 3) @RequestParam("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI") final String prisonId, @RequestParam(value = "offender_id", required = false) @ApiParam(name = "offender_id", value = "Offender Noms Id", example = "A1417AE") final String offenderIdentifier, @RequestParam(value = "event_type", required = false) @ApiParam(name = "event_type", value = "Event Type", example = "ALERT") final String eventType, @RequestParam("from_datetime") @ApiParam(name = "from_datetime", value = "From Date Time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123", example = "2017-10-07T12:23:45.678") final String fromDateTime, @RequestParam(value = "limit", required = false) @ApiParam(name = "limit", value = "Number of events to return", example = "100") final Long limit) {
        final var events = service.getEvents(prisonId, new OffenderIdentifier(offenderIdentifier), eventType, optionalStrToLocalDateTime(fromDateTime), limit);
        return new Events(events);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "One of: <ul><li>Invalid transaction type - The transaction type has not been set up for the API for {prison_id}</li>" +
                    "<li>Finance Exception - This indicates an unexpected financial problem, check the nomis_api_log table for details.</li>" +
                    "<li>Only receipt transaction types allowed - Only transaction types with a transaction usage of Receipt are allowed.</li>" +
                    "<li>Sum of sub account balances not equal to current balance - The sum of all the sub account balances does equal the current balance held for the trust account</li>" +
                    "<li>Offender being transferred - The offender is currently in transit</li>" +
                    "<li>Offender still in specified prison - The offender is still at the specified prison. Use Record Transaction instead.</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "One of: <ul><li>Offender Not Found - No offender matching the specified offender_id has been found on nomis.</li>" +
                    "<li>Offender never at prison - The offender has never been at the specified prison</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Duplicate Post - A transaction already exists with the client_unique_ref provided.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Record transaction at previous Prison.", notes = "<p>Post a financial transaction to Nomis to a prison that the offender is no longer at.</p>" +
            "<p>The valid prison_id and type combinations are defined in the Nomis transaction_operations table which is maintained by the Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. Only those prisons (Caseloads) and Transaction types associated with the NOMISAPI module are valid. Only Transaction types with a usage of R (Receipt) are valid." +
            "Transaction Types are maintained via the Maintain Transaction Types screen (OCMTRANS).</p>" +
            "<p>Transaction is posted to the specified prison.  if the account has been closed at this prison then it is re-opened first.</p>" +
            "<p>If the offender has been transferred to another prison then the funds are transferred to this prison.</p>" +
            "<p>If the account was previously closed then it will be closed again.</p>" +
            "<p>If the offender has been released then the funds are transferred to NACRO. Based on the Nomis Clear Inactive accounts screen (OTDCLINA).</p>")
    @PostMapping("/prison/{previous_prison_id}/offenders/{noms_id}/transfer_transactions")
    @HasWriteScope
    @ProxyUser
    public Transfer transferTransaction(@RequestHeader(value = "X-Client-Name", required = false) @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") final String clientName, @Size(max = 3) @NotNull @PathVariable("previous_prison_id") @ApiParam(name = "previous_prison_id", value = "Prison ID", example = "BMI", required = true) final String previousPrisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) final String nomsId,
                                        @javax.validation.Valid @NotNull @RequestBody @ApiParam(value = "Transaction Details", required = true) final CreateTransaction createTransaction) {

        final var uniqueClientId = getUniqueClientId(clientName, createTransaction.getClientUniqueRef());

        final var transfer = service.transferTransaction(previousPrisonId, nomsId,
                createTransaction.getType(), createTransaction.getDescription(),
                createTransaction.getAmountInPounds(), LocalDate.now(),
                createTransaction.getClientTransactionId(), uniqueClientId);

        return new Transfer(transfer.getCurrentLocation(), new Transaction(transfer.getTransaction().getId()));
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Transaction Created", response = Transaction.class),
            @ApiResponse(code = 400, message = "One of: <ul><li>Insufficient Funds - The prisoner has insufficient funds in the required account to cover the cost of the debit transaction</li>" +
                    "<li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li>" +
                    "<li>Invalid transaction type - The transaction type has not been set up for the API for {prison_id}</li>" +
                    "<li>Finance Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Duplicate post - The unique_client_ref has been used before", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Post a financial transaction to NOMIS.", notes = "The valid prison_id and type combinations are defined in the Nomis transaction_operations table which is maintained by the Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. Only those prisons (Caseloads) and Transaction types associated with the NOMISAPI module are valid.<br/>" +
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
    public Transaction createTransaction(@RequestHeader(value = "X-Client-Name", required = false) @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") final String clientName, @Size(max = 3) @NotNull @PathVariable("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) final String nomsId,
                                         @javax.validation.Valid @NotNull @RequestBody @ApiParam(value = "Transaction Details", required = true) final CreateTransaction createTransaction) {

        final var uniqueClientId = getUniqueClientId(clientName, createTransaction.getClientUniqueRef());

        final var result = service.createTransaction(prisonId, nomsId,
                createTransaction.getType(), createTransaction.getDescription(),
                createTransaction.getAmountInPounds(), LocalDate.now(),
                createTransaction.getClientTransactionId(), uniqueClientId);

        return new Transaction(result);
    }

    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender Not Found - No offender matching the specified offender_id has been found on nomis.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Offender not in specified prison", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Get holds.", notes = "Gets every hold on an offender’s account or just the hold identified by the client_unique_ref")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/holds")
    public List<Hold> getHolds(@RequestHeader(value = "X-Client-Name", required = false) @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") final String clientName, @Size(max = 3) @NotNull @PathVariable("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) final String nomsId, @Pattern(regexp = CLIENT_UNIQUE_REF_PATTERN) @Size(max = 64) @RequestParam(value = "client_unique_ref", required = false) @ApiParam(name = "client_unique_ref", value = "Client unique reference") final String clientUniqueRef) {
        final var uniqueClientId = getUniqueClientId(clientName, clientUniqueRef);
        return service.getHolds(prisonId, nomsId, uniqueClientId, clientName);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Fetching live roll.")
    @GetMapping("/prison/{prison_id}/live_roll")
    public LiveRoll getLiveRoll(@Size(max = 3) @NotNull @PathVariable("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) final String prisonId) {
        return new LiveRoll(service.getLiveRoll(prisonId));
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Get the PSS detail by offender", notes = "Returns the PSS detail information for the specified offender including personal data, warnings, sentence details and location information.<br/>" +
            "<ul><li>The 'type' field is always OFFENDER_DETAILS_REQUEST</li><br/>" +
            "<li>The field 'offender_details_request' contains a JSON block of data containing the offender data.</li></ul>" +
            "The format of 'offender_details_request' is not specified here.")
    @GetMapping("/offenders/{noms_id}/pss_detail")
    public Event getOffenderPssDetail(@Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId) {
        return service.getOffenderPssDetail(nomsId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "One of: <ul><li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li><li>Invalid payment type</li>" +
                    "<li>Client reference more than 12 characters</li><li>Missing data in request</li>" +
                    "<li>Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested prison or offender could not be found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Duplicate post - after an error with a post this response will be given for subsequent duplicate attempts", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Store a payment for an offender account.", notes = "Pay events will be stored in a table on receipt by Nomis to be processed by a batch job scheduled to run after the last Nomis payroll batch job but before the advances and scheduled payments batch jobs.\n" +
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
    public PaymentResponse storePayment(@Size(max = 3) @NotNull @PathVariable("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) final String nomsId, @javax.validation.Valid @NotNull @RequestBody @ApiParam(value = "Transaction Details", required = true) final StorePaymentRequest payment) {
        return service.storePayment(prisonId, nomsId, payment.getType(), payment.getDescription(), payment.getAmountInPounds(), LocalDate.now(), payment.getClientTransactionId());
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found. Offender has no account at this prison.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison or offender was not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Retrieve an offender's financial account balances.", notes = "Returns balances for the offender’s three sub accounts (spends, savings and cash) at the specified prison.<br/>" +
            "All balance values are represented as pence values.")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/accounts")
    public AccountBalance getAccountBalance(@Size(max = 3) @NotNull @PathVariable("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "WLI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId) {
        return service.getAccountBalances(prisonId, nomsId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found. Offender has no account at this prison.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison, offender or accountType not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Retrieve an offender's financial transaction history for cash, spends or savings.", notes = "Transactions are returned in NOMIS ordee (Descending date followed by id).<br/>" +
            "All transaction amounts are represented as pence values.")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/accounts/{account_code}/transactions")
    @SuppressWarnings("RestParamTypeInspection")
    public AccountTransactions getAccountTransactions(@Size(max = 3) @NotNull @PathVariable("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "WLI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId, @NotNull @PathVariable("account_code") @ApiParam(name = "account_code", value = "Account code", example = "spends", required = true, allowableValues = "spends,cash,savings") final String accountCode, @RequestParam(value = "from_date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(name = "from_date", value = "Start date for transactions (defaults to today if not supplied)", example = "2019-04-01") final LocalDate fromDate, @RequestParam(value = "to_date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(name = "to_date", value = "To date for transactions (defaults to today if not supplied)", example = "2019-05-01") final LocalDate toDate) {
        final var transactions = service.getAccountTransactions(prisonId, nomsId, accountCode, fromDate, toDate);
        return new AccountTransactions(transactions);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found. Offender has no account at this prison.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison, offender or accountType not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Retrieve a single financial transaction using client unique ref.", notes = "All transaction amounts are represented as pence values.")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/transactions/{client_unique_ref}")
    public AccountTransaction getTransactionByClientUniqueRef(@RequestHeader(value = "X-Client-Name", required = false) @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") final String clientName, @Size(max = 3) @NotNull @PathVariable("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "WLI", required = true) final String prisonId, @Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @PathVariable("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId, @Pattern(regexp = CLIENT_UNIQUE_REF_PATTERN) @Size(max = 64) @PathVariable("client_unique_ref") @ApiParam(name = "client_unique_ref", value = "Client unique reference", required = true) final String clientUniqueRef) {
        final var uniqueClientId = getUniqueClientId(clientName, clientUniqueRef);

        return service.getTransactionByClientUniqueRef(prisonId, nomsId, uniqueClientId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Parameter exception (invalid date, time, format, type)", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Retrieve active offender", notes = "offender id will be returned if offender is found")
    @GetMapping("/lookup/active_offender")
    @SuppressWarnings("RestParamTypeInspection")
    public ActiveOffender getActiveOffender(@Pattern(regexp = NOMS_ID_REGEX_PATTERN) @NotNull @RequestParam("noms_id") @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) final String nomsId, @RequestParam(value = "date_of_birth", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @ApiParam(name = "date_of_birth", value = "date of birth", example = "2019-05-01") final LocalDate birthDate) {
        return service.getActiveOffender(nomsId, birthDate);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid start and end date range", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Fetch available_dates for offender", notes = "returns list of dates")
    @GetMapping("offenders/{offender_id}/visits/available_dates")
    @SuppressWarnings("RestParamTypeInspection")
    public AvailableDates getVisitAvailableDates(@PathVariable("offender_id") @NotNull @ApiParam(name = "offender_id", value = "Offender Id", example = "1234567", required = true) final Long offenderId, @RequestParam("start_date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @ApiParam(name = "start_date", value = "Start date", example = "2019-04-01", required = true) final LocalDate fromDate, @RequestParam("end_date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @ApiParam(name = "end_date", value = "To date", example = "2019-05-01", required = true) final LocalDate toDate) {
        return service.getVisitAvailableDates(offenderId, fromDate, toDate);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid start and end date range", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Fetch contacts list for offender", notes = "returns list of contacts")
    @GetMapping("offenders/{offender_id}/visits/contact_list")
    public ContactList getVisitContactList(@PathVariable("offender_id") @NotNull @ApiParam(name = "offender_id", value = "Offender Id", example = "1234567", required = true) final Long offenderId) {
        return service.getVisitContactList(offenderId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Dates requested must be in future", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Fetch unavailability reason for dates", notes = "returns list of reason if unavailable date")
    @GetMapping("offenders/{offender_id}/visits/unavailability")
    public SortedMap<String, UnavailabilityReason> getVisitUnavailability(@PathVariable("offender_id") @NotNull @ApiParam(name = "offender_id", value = "Offender Id", example = "1234567", required = true) final Long offenderId, @RequestParam("dates") @ApiParam(name = "dates", value = "dates", example = "2019-05-01,2019-05-02", required = true) final String dates) {
        return service.getVisitUnavailability(offenderId, dates);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid start and end date range", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison Not Found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Fetch visit slots with capacity", notes = "returns list slots with capacity details")
    @GetMapping("prison/{prison_id}/slots")
    @SuppressWarnings("RestParamTypeInspection")
    public VisitSlots getVisitSlotsWithCapacity(@Size(max = 3) @PathVariable("prison_id") @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) final String prisonId, @RequestParam("start_date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @ApiParam(name = "start_date", value = "Start date", example = "2019-04-01", required = true) final LocalDate fromDate, @RequestParam("end_date") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull @ApiParam(name = "end_date", value = "End date", example = "2019-05-01", required = true) final LocalDate toDate) {
        return service.getVisitSlotsWithCapacity(prisonId, fromDate, toDate);
    }
}
