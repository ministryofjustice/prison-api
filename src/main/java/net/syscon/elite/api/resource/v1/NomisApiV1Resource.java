package net.syscon.elite.api.resource.v1;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.v1.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@Api(tags = {"/v1"})
public interface NomisApiV1Resource {

    String NOMS_ID_REGEX_PATTERN = "[a-zA-Z][0-9]{4}[a-zA-Z]{2}";
    String CLIENT_UNIQUE_REF_PATTERN = "[a-zA-Z0-9-_]+";

    @GetMapping("/offenders/{noms_id}")
    @ApiOperation(value = "Returns general offender information.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Offender.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Offender getOffender(@ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);


    @GetMapping("/offenders/{noms_id}/image")


    @ApiOperation(value = "Get Current Photograph of the offender",
            notes = "Returns a 480wx600h JPEG photograph of the offender. The data is base64 encoded within the image key.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Image.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Image getOffenderImage(@ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);


    @GetMapping("/offenders/{noms_id}/location")


    @ApiOperation(value = "Current Location of the offender",
            notes = "The levels shows the type of each level of the location address as defined on the Agency Details tab in Maintain Agency Locations screen (OUMAGLOC).<br/><br/>Since the offender's location can change often and is fairly sensitive (and therefore should not automatically be exposed to all services), this information is not included in the general offender information call.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Location getLatestBookingLocation(@ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

    @GetMapping("/offenders/{noms_id}/charges")


    @ApiOperation(value = "Legal cases for each booking and charges within each legal case.",
            notes = "Returns all the bookings, the legal cases for each booking and charges within each legal case.<br/>" +
                    "The ordering is as follows:<ul>" +
                    "<li><strong>bookings</strong>: Current or latest booking first, others in descending order of booking date</li>" +
                    "<li><strong>legal_cases</strong>: Active cases followed by inactive cases, further ordered by begin_date, latest first</li>" +
                    "<li><strong>charges</strong>: Most serious active charge first, then remaining active charges, followed by inactive charges</li></ul>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Bookings.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Bookings getBookings(@ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

    @GetMapping("/offenders/{noms_id}/alerts")


    @ApiOperation(value = "Fetch alerts by offender",
            notes = "Returns all active alerts for the specified offender or those that meet the optional criteria. Active alerts are listed first, followed by inactive alerts, both sorted by ascending order of alert date.<br/>" +
                    "<ul><li>if alert_type is specified then only alerts of that type are returned</li>" +
                    "<li>if modified_since is specified then only those alerts created or modified on or after the specified date time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123</li>" +
                    "<li>If include_inactive=true is specified then inactive alerts are also returned.</li></ul>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Alerts.class),
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Alerts getAlerts(@ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1583AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
                     @ApiParam(name = "alert_type", value = "Alert Type, if alert_type is specified then only alerts of that type are returned", example = "H") @RequestParam("alert_type") String alertType,
                     @ApiParam(name = "modified_since", value = "Modified Since - if modified_since is specified then only those alerts created or modified on or after the specified date time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123", example = "2017-10-07T12:23:45.678") @RequestParam("modified_since") String modifiedSince,
                     @ApiParam(name = "include_inactive", value = "Include Inactive alerts, If include_inactive=true is specified then inactive alerts are also returned.", example = "true", defaultValue = "false") @RequestParam(value = "include_inactive", defaultValue = "false") boolean includeInactive);

    @GetMapping("/offenders/events")


    @ApiOperation(value = "Fetch events",
            notes = "Returns all events that required to update the prisoner self service application. Currently these are:" +
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Events.class),
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Events getOffenderEvents(
            @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI") @RequestParam("prison_id") @Length(max = 3) String prisonId,
            @ApiParam(name = "offender_id", value = "Offender Noms Id", example = "A1417AE") @RequestParam("offender_id") String offenderIdentifier,
            @ApiParam(name = "event_type", value = "Event Type", example = "H") @RequestParam("event_type") String eventType,
            @ApiParam(name = "from_datetime", value = "From Date Time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123", example = "2017-10-07T12:23:45.678") @RequestParam("from_datetime") String fromDateTime,
            @ApiParam(name = "limit", value = "Number of events to return", example = "100") @RequestParam("limit") Long limit);

    @PostMapping("/prison/{previous_prison_id}/offenders/{noms_id}/transfer_transactions")


    @ApiOperation(value = "Record transaction at previous Prison.",
            notes = "<p>Post a financial transaction to Nomis to a prison that the offender is no longer at.</p>" +
                    "<p>The valid prison_id and type combinations are defined in the Nomis transaction_operations table which is maintained by the Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. Only those prisons (Caseloads) and Transaction types associated with the NOMISAPI module are valid. Only Transaction types with a usage of R (Receipt) are valid." +
                    "Transaction Types are maintained via the Maintain Transaction Types screen (OCMTRANS).</p>" +
                    "<p>Transaction is posted to the specified prison.  if the account has been closed at this prison then it is re-opened first.</p>" +
                    "<p>If the offender has been transferred to another prison then the funds are transferred to this prison.</p>" +
                    "<p>If the account was previously closed then it will be closed again.</p>" +
                    "<p>If the offender has been released then the funds are transferred to NACRO. Based on the Nomis Clear Inactive accounts screen (OTDCLINA).</p>")
    @ResponseStatus(value = HttpStatus.OK, reason = "Transaction Created")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Transaction Created", response = Transfer.class),
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
    Transfer transferTransaction(
            @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") @RequestHeader("X-Client-Name") String clientName,
            @ApiParam(name = "previous_prison_id", value = "Prison ID", example = "BMI", required = true) @PathVariable("previous_prison_id") @NotNull @Length(max = 3) String previousPrisonId,
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
            @ApiParam(value = "Transaction Details", required = true) @NotNull @Valid CreateTransaction createTransaction);


    @PostMapping("/prison/{prison_id}/offenders/{noms_id}/transactions")
    @ApiOperation(value = "Post a financial transaction to NOMIS.",
            notes = "The valid prison_id and type combinations are defined in the Nomis transaction_operations table which is maintained by the Maintain Transaction Operations screen (OCMTROPS), from the Financials Maintenance menu. Only those prisons (Caseloads) and Transaction types associated with the NOMISAPI module are valid.<br/>" +
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
    @ResponseStatus(value = HttpStatus.OK, reason = "Transaction Created")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Transaction Created", response = Transaction.class),
            @ApiResponse(code = 400, message = "One of: <ul><li>Insufficient Funds - The prisoner has insufficient funds in the required account to cover the cost of the debit transaction</li>" +
                    "<li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li>" +
                    "<li>Invalid transaction type - The transaction type has not been set up for the API for {prison_id}</li>" +
                    "<li>Finance Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Duplicate post - The unique_client_ref has been used before", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Transaction createTransaction(
            @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") @RequestHeader("X-Client-Name") String clientName,
            @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) @PathVariable("prison_id") @NotNull @Length(max = 3) String prisonId,
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
            @ApiParam(value = "Transaction Details", required = true) @NotNull @Valid CreateTransaction createTransaction);

    @GetMapping("/offenders/{noms_id}/pss_detail")


    @ApiOperation(value = "Get the PSS detail by offender",
            notes = "Returns the PSS detail information for the specified offender including personal data, warnings, sentence details and location information.<br/>" +
                    "<ul><li>The 'type' field is always OFFENDER_DETAILS_REQUEST</li><br/>" +
                    "<li>The field 'offender_details_request' contains a JSON block of data containing the offender data.</li></ul>" +
                    "The format of 'offender_details_request' is not specified here.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Event.class),
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Event getOffenderPssDetail(@ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/holds")


    @ApiOperation(value = "Get holds.",
            notes = "Gets every hold on an offender’s account or just the hold identified by the client_unique_ref")
    @ResponseStatus(value = HttpStatus.OK, reason = "OK")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Transaction Created", response = Hold.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Offender Not Found - No offender matching the specified offender_id has been found on nomis.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Offender not in specified prison", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<Hold> getHolds(
            @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") @RequestHeader("X-Client-Name") String clientName,
            @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) @PathVariable("prison_id") @NotNull @Length(max = 3) String prisonId,
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
            @ApiParam(name = "client_unique_ref", value = "Client unique reference") @RequestParam("client_unique_ref") @Length(max = 64) @Pattern(regexp = CLIENT_UNIQUE_REF_PATTERN) final String clientUniqueRef);

    @GetMapping("/prison/{prison_id}/live_roll")


    @ApiOperation(value = "Fetching live roll.")
    @ResponseStatus(value = HttpStatus.OK, reason = "OK")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Live roll returned for this prison.", response = LiveRoll.class),
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    LiveRoll getLiveRoll(
            @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) @PathVariable("prison_id") @NotNull @Length(max = 3) String prisonId);


    @PostMapping("/prison/{prison_id}/offenders/{noms_id}/payment")


    @ApiOperation(value = "Store a payment for an offender account.",
            notes = "Pay events will be stored in a table on receipt by Nomis to be processed by a batch job scheduled to run after the last Nomis payroll batch job but before the advances and scheduled payments batch jobs.\n" +
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
    @ResponseStatus(value = HttpStatus.OK, reason = "Payment accepted")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Payment accepted", response = Transaction.class),
            @ApiResponse(code = 400, message = "One of: <ul><li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li><li>Invalid payment type</li>" +
                    "<li>Client reference more than 12 characters</li><li>Missing data in request</li>" +
                    "<li>Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested prison or offender could not be found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Duplicate post - after an error with a post this response will be given for subsequent duplicate attempts", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    PaymentResponse storePayment(
            @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) @PathVariable("prison_id") @NotNull @Length(max = 3) String prisonId,
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
            @ApiParam(value = "Transaction Details", required = true) @NotNull @Valid StorePaymentRequest storePaymentRequest);


    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/accounts")


    @ApiOperation(value = "Retrieve an offender's financial account balances.", notes = "Returns balances for the offender’s three sub accounts (spends, savings and cash) at the specified prison.<br/>" +
            "All balance values are represented as pence values.")
    @ResponseStatus(value = HttpStatus.OK, reason = "OK")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Account balances returned for this offender and prison.", response = AccountBalance.class),
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found. Offender has no account at this prison.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison or offender was not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    AccountBalance getAccountBalance(
            @ApiParam(name = "prison_id", value = "Prison ID", example = "WLI", required = true) @PathVariable("prison_id") @NotNull @Length(max = 3) String prisonId,
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);


    @SuppressWarnings("RestParamTypeInspection")
    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/accounts/{account_code}/transactions")
    @ApiOperation(value = "Retrieve an offender's financial transaction history for cash, spends or savings.", notes = "Transactions are returned in NOMIS ordee (Descending date followed by id).<br/>" +
            "All transaction amounts are represented as pence values.")
    @ResponseStatus(value = HttpStatus.OK, reason = "OK")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Account transactions returned", response = AccountTransactions.class),
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found. Offender has no account at this prison.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison, offender or accountType not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    AccountTransactions getAccountTransactions(
            @ApiParam(name = "prison_id", value = "Prison ID", example = "WLI", required = true) @PathVariable("prison_id") @NotNull @Length(max = 3) String prisonId,
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
            @ApiParam(name = "account_code", value = "Account code", example = "spends", required = true, allowableValues = "spends,cash,savings") @PathVariable("account_code") @NotNull String accountCode,
            @ApiParam(name = "from_date", value = "Start date for transactions (defaults to today if not supplied)", example = "2019-04-01") @RequestParam("from_date") LocalDate fromDate,
            @ApiParam(name = "to_date", value = "To date for transactions (defaults to today if not supplied)", example = "2019-05-01") @RequestParam("to_date") LocalDate toDate);

    @GetMapping("/prison/{prison_id}/offenders/{noms_id}/transactions/{client_unique_ref}")
    @ApiOperation(value = "Retrieve a single financial transaction using client unique ref.", notes = "All transaction amounts are represented as pence values.")
    @ResponseStatus(value = HttpStatus.OK, reason = "OK")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Account transaction returned", response = AccountTransaction.class),
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found. Offender has no account at this prison.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison, offender or accountType not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    AccountTransaction getTransactionByClientUniqueRef(
            @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") @RequestHeader("X-Client-Name") String clientName,
            @ApiParam(name = "prison_id", value = "Prison ID", example = "WLI", required = true) @PathVariable("prison_id") @NotNull @Length(max = 3) String prisonId,
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) @PathVariable("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
            @ApiParam(name = "client_unique_ref", value = "Client unique reference", required = true) @PathVariable("client_unique_ref") @Length(max = 64) @Pattern(regexp = CLIENT_UNIQUE_REF_PATTERN) final String clientUniqueRef);

    @SuppressWarnings("RestParamTypeInspection")
    @GetMapping("/lookup/active_offender")
    @ApiOperation(value = "Retrieve active offender", notes = "offender id will be returned if offender is found")
    @ResponseStatus(value = HttpStatus.OK, reason = "OK")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Active Offender ID returned", response = ActiveOffender.class),
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Parameter exception (invalid date, time, format, type)", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ActiveOffender getActiveOffender(
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) @RequestParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
            @ApiParam(name = "date_of_birth", value = "date of birth", example = "2019-05-01") @NotNull @RequestParam("date_of_birth") LocalDate birthDate);

    @SuppressWarnings("RestParamTypeInspection")
    @GetMapping("offenders/{offender_id}/visits/available_dates")
    @ApiOperation(value = "Fetch available_dates for offender",
            notes = "returns list of dates")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AvailableDates.class),
            @ApiResponse(code = 400, message = "Invalid start and end date range", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    AvailableDates getVisitAvailableDates(
            @ApiParam(name = "offender_id", value = "Offender Id", example = "1234567", required = true) @NotNull @PathVariable("offender_id") Long offenderId,
            @ApiParam(name = "start_date", value = "Start date", example = "2019-04-01", required = true) @NotNull @RequestParam("start_date") LocalDate fromDate,
            @ApiParam(name = "end_date", value = "To date", example = "2019-05-01", required = true) @NotNull @RequestParam("end_date") LocalDate toDate);

    @GetMapping("offenders/{offender_id}/visits/contact_list")
    @ApiOperation(value = "Fetch contacts list for offender",
            notes = "returns list of contacts")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AvailableDates.class),
            @ApiResponse(code = 400, message = "Invalid start and end date range", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ContactList getVisitContactList(
            @ApiParam(name = "offender_id", value = "Offender Id", example = "1234567", required = true) @NotNull @PathVariable("offender_id") Long offenderId);

    @GetMapping("offenders/{offender_id}/visits/unavailability")
    @ApiOperation(value = "Fetch unavailability reason for dates",
            notes = "returns list of reason if unavailable date")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class),
            @ApiResponse(code = 400, message = "Dates requested must be in future", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    SortedMap<String, UnavailabilityReason> getVisitUnavailability(
            @ApiParam(name = "offender_id", value = "Offender Id", example = "1234567", required = true) @NotNull @PathVariable("offender_id") Long offenderId,
            @ApiParam(name = "dates", value = "dates", example = "2019-05-01,2019-05-02", required = true) @RequestParam("dates") String dates);

    @SuppressWarnings("RestParamTypeInspection")
    @GetMapping("prison/{prison_id}/slots")
    @ApiOperation(value = "Fetch visit slots with capacity",
            notes = "returns list slots with capacity details")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AvailableDates.class),
            @ApiResponse(code = 400, message = "Invalid start and end date range", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison Not Found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    VisitSlots getVisitSlotsWithCapacity(
            @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI") @PathVariable("prison_id") @Length(max = 3) String prisonId,
            @ApiParam(name = "start_date", value = "Start date", example = "2019-04-01", required = true) @NotNull @RequestParam("start_date") LocalDate fromDate,
            @ApiParam(name = "end_date", value = "To date", example = "2019-05-01", required = true) @NotNull @RequestParam("end_date") LocalDate toDate);
}
