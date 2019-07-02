package net.syscon.elite.api.resource.v1;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.v1.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Api(tags = {"/v1"})
public interface NomisApiV1Resource {

    String NOMS_ID_REGEX_PATTERN = "[a-zA-Z][0-9]{4}[a-zA-Z]{2}";

    @GET
    @Path("/offenders/{noms_id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Returns general offender information.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Offender.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Offender getOffender(@ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @PathParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);


    @GET
    @Path("/offenders/{noms_id}/image")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Get Current Photograph of the offender",
            notes = "Returns a 480wx600h JPEG photograph of the offender. The data is base64 encoded within the image key.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Image.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Image getOffenderImage(@ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @PathParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);


    @GET
    @Path("/offenders/{noms_id}/location")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Current Location of the offender",
            notes = "The levels shows the type of each level of the location address as defined on the Agency Details tab in Maintain Agency Locations screen (OUMAGLOC).<br/><br/>Since the offender's location can change often and is fairly sensitive (and therefore should not automatically be exposed to all services), this information is not included in the general offender information call.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Location getLatestBookingLocation(@ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @PathParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

    @GET
    @Path("/offenders/{noms_id}/charges")
    @Consumes({"application/json"})
    @Produces({"application/json"})
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
    Bookings getBookings(@ApiParam(name = "noms_id", value = "Offender Noms ID", example = "A1417AE", required = true) @PathParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

    @GET
    @Path("/offenders/{noms_id}/alerts")
    @Consumes({"application/json"})
    @Produces({"application/json"})
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
    Alerts getAlerts(@ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1583AE", required = true) @PathParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
                       @ApiParam(name = "alert_type", value = "Alert Type, if alert_type is specified then only alerts of that type are returned", example = "H") @QueryParam("alert_type") String alertType,
                       @ApiParam(name = "modified_since", value = "Modified Since - if modified_since is specified then only those alerts created or modified on or after the specified date time. The following formats are supported: 2018-01-10, 2018-01-10 03:34, 2018-01-10 03:34:12, 2018-01-10 03:34:12.123", example = "2017-10-07T12:23:45.678") @QueryParam("modified_since") String modifiedSince,
                       @ApiParam(name = "include_inactive", value = "Include Inactive alerts, If include_inactive=true is specified then inactive alerts are also returned.", example = "true", defaultValue = "false") @QueryParam("include_inactive") @DefaultValue("false") boolean includeInactive);


    @POST
    @Path("/prison/{prison_id}/offenders/{noms_id}/transactions")
    @Consumes({"application/json"})
    @Produces({"application/json"})
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
    @ResponseStatus(value = HttpStatus.CREATED, reason = "Transaction Created")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Transaction Created", response = TransactionResponse.class),
            @ApiResponse(code = 400, message = "One of: <ul><li>Insufficient Funds - The prisoner has insufficient funds in the required account to cover the cost of the debit transaction</li>" +
                    "<li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li>" +
                    "<li>Invalid transaction type - The transaction type has not been set up for the API for {prison_id}</li>" +
                    "<li>Finance Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Duplicate post - The unique_client_ref has been used before", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Response createTransaction(
                                    @ApiParam(name = "X-Client-Name", value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") @HeaderParam("X-Client-Name") String clientName,
                                    @ApiParam(name = "prison_id", value = "Prison ID", example = "BMI", required = true) @PathParam("prison_id") @NotNull @Length(max=3) String prisonId,
                                    @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1417AE", required = true) @PathParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
                                    @ApiParam(value = "Transaction Details", required = true) @NotNull @Valid CreateTransaction createTransaction);

    @GET
    @Path("/offenders/{noms_id}/pss_detail")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Fetch PSS detail by offender",
            notes = "Returns the PSS detail for the specified offender. The response structure is :<br/> " +
                    "<br/>" +
                    "<table>" +
                    "<tr><th>Attribute Name</th><th>Value</th><th>Notes</th></tr>" +
                    "<tr><td>type</td><td>offender_detail_request</td><td>Always set to this value</td></tr>" +
                    "<tr><td>id</td><td>0</td><td>Always zero when an object is present</td></tr>" +
                    "<tr><td>timestamp</td><td>2019-04-23 14:23.000</td><td>Set to the date and time of the request</td></tr>" +
                    "<tr><td>prison_id</td><td>MDI</td><td>The agency location code for the establishment</td></tr>" +
                    "<tr><td>noms_id</td><td>A1417AE</td><td>The unique identifier for this offender in NOMIS</td></tr>" +
                    "<tr><td>offender_details_request</td><td>JSON object</td><td>Details of the offender, their sentence, location, warnings/alerts, IEP level and case notes. ** These are generated directly from the Nomis DB and not within the API.</td></tr>" +
                    "</table>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderPssDetailEvent.class),
            @ApiResponse(code = 400, message = "Invalid Noms ID", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    OffenderPssDetailEvent getOffenderPssDetail(@ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) @PathParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

}
