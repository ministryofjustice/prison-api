package net.syscon.elite.api.resource.v1;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.api.support.ResponseDelegate;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Api(tags = {"/v1"})
public interface NomisApiV1Resource {

    String NOMS_ID_REGEX_PATTERN = "[a-zA-Z][0-9]{4}[a-zA-Z]{2}";

    @GET
    @Path("/offenders/{nomsId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Returns general offender information.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Offender.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    OffenderResponse getOffender(@ApiParam(value = "nomsId", example = "A1417AE", required = true) @PathParam("nomsId") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

    class OffenderResponse extends ResponseDelegate {
        public OffenderResponse(final Response response, final Offender location) {
            super(response, location);
        }
    }

    @GET
    @Path("/offenders/{nomsId}/image")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Get Current Photograph of the offender",
            notes = "Returns a 480wx600h JPEG photograph of the offender. The data is base64 encoded within the image key.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Image.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    OffenderImageResponse getOffenderImage(@ApiParam(value = "nomsId", example = "A1417AE", required = true) @PathParam("nomsId") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

    class OffenderImageResponse extends ResponseDelegate {
        public OffenderImageResponse(final Response response, final Image image) {
            super(response, image);
        }
    }

    @GET
    @Path("/offenders/{nomsId}/location")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Current Location of the offender",
            notes = "The levels shows the type of each level of the location address as defined on the Agency Details tab in Maintain Agency Locations screen (OUMAGLOC).<br/><br/>Since the offender's location can change often and is fairly sensitive (and therefore should not automatically be exposed to all services), this information is not included in the general offender information call.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Location.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    LatestBookingLocationResponse getLatestBookingLocation(@ApiParam(value = "nomsId", example = "A1417AE", required = true) @PathParam("nomsId") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId);

    class LatestBookingLocationResponse extends ResponseDelegate {
        public LatestBookingLocationResponse(final Response response, final Location location) {
            super(response, location);
        }
    }

    @POST
    @Path("/prison/{prison_id}/offenders/{noms_id}/transactions")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Post a financial transaction to Nomis.",
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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TransactionResponse.class),
            @ApiResponse(code = 400, message = "One of: <ul><li>Insufficient Funds - The prisoner has insufficient funds in the required account to cover the cost of the debit transaction</li>" +
                    "<li>Offender not in specified prison - prisoner identified by {noms_id} is not in prison {prison_id}</li>" +
                    "<li>Invalid transaction type - The transaction type has not been set up for the API for {prison_id}</li>" +
                    "<li>Finance Exception - An unexpected error has occurred. Details will have been logged in the nomis_api_logs table on the Nomis database.</li></ul>", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "Duplicate post - The unique_client_ref has been used before", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})

    CreateTransactionResponse createTransaction(
                                    @ApiParam(value = "If present then the value is prepended to the client_unique_ref separated by a dash. When this API is invoked via the Nomis gateway this will already have been created by the gateway.") @HeaderParam(" X-Client-Name") String clientName,
                                    @ApiParam(value = "prison_id", example = "BMI", required = true) @PathParam("prison_id") @NotNull @Length(max=3) String prisonId,
                                    @ApiParam(value = "noms_id", example = "A1417AE", required = true) @PathParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
                                    @ApiParam(value = "", required = true) @NotNull @Valid CreateTransaction createTransaction);

    class CreateTransactionResponse extends ResponseDelegate {
        public CreateTransactionResponse(final Response response, final TransactionResponse txResp) {
            super(response, txResp);
        }
    }
}
