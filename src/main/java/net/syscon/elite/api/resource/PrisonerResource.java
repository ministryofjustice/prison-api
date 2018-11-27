package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(tags = {"/prisoners"})
@SuppressWarnings("unused")
public interface PrisonerResource {

    @GET
    @Path("/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of offenders matching specified criteria.", notes = "<b>(BETA)</b> List of offenders matching specified criteria.", nickname="getPrisoners")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = PrisonerDetail.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetPrisonersResponse getPrisoners(@ApiParam(value = "If true the result set should include a row for every matched alias.  If the request includes some combination of firstName, lastName and dateOfBirth then this will be a subset of the OFFENDERS records for one or more offenders. Otherwise it will be every OFFENDERS record for each match on the other search criteria. Default is false.") @QueryParam("includeAliases") boolean includeAliases,
                                      @ApiParam(value = "The offender's NOMS number. NOMS numbers have the format:<b> ANNNNAA</b>") @QueryParam("offenderNo") String offenderNo,
                                      @ApiParam(value = "The offender's PNC (Police National Computer) number.") @QueryParam("pncNumber") String pncNumber,
                                      @ApiParam(value = "The offender's CRO (Criminal Records Office) number.") @QueryParam("croNumber") String croNumber,
                                      @ApiParam(value = "The first name of the offender.") @QueryParam("firstName") String firstName,
                                      @ApiParam(value = "The middle name(s) of the offender.") @QueryParam("middleNames") String middleNames,
                                      @ApiParam(value = "The last name of the offender.") @QueryParam("lastName") String lastName,
                                      @ApiParam(value = "The offender's date of birth. Cannot be used in conjunction with <i>dobFrom</i> or <i>dobTo</i>. Must be specified using YYYY-MM-DD format.") @QueryParam("dob") String dob,
                                      @ApiParam(value = "Start date for offender date of birth search. If <i>dobTo</i> is not specified, an implicit <i>dobTo</i> value of <i>dobFrom</i> + 10 years will be applied. If <i>dobTo</i> is specified, it will be adjusted, if necessary, to ensure it is not more than 10 years after <i>dobFrom</i>. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.") @QueryParam("dobFrom") String dobFrom,
                                      @ApiParam(value = "End date for offender date of birth search. If <i>dobFrom</i> is not specified, an implicit <i>dobFrom</i> value of <i>dobTo</i> - 10 years will be applied. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.") @QueryParam("dobTo") String dobTo,
                                      @ApiParam(value = "Offenders location filter (IN, OUT or ALL) - defaults to ALL.", defaultValue = "ALL") @QueryParam("location") String location,
                                      @ApiParam(value = "If <i>true</i>, the search will use partial, start-of-name matching of offender names (where provided). For example, if <i>lastName</i> criteria of 'AD' is specified, this will match an offender whose last name is 'ADAMS' but not an offender whose last name is 'HADAD'. This will typically increase the number of matching offenders found. This parameter can be used with any other search processing parameter (e.g. <i>prioritisedMatch</i> or <i>anyMatch</i>).") @QueryParam("partialNameMatch") boolean partialNameMatch,
                                      @ApiParam(value = "If <i>true</i>, search criteria prioritisation is used and searching/matching will stop as soon as one or more matching offenders are found. The criteria priority is:<br/><br/>1. <i>offenderNo</i><br/> 2. <i>pncNumber</i><br/>3. <i>croNumber</i><br/>4. <i>firstName</i>, <i>lastName</i>, <i>dob</i> <br/>5. <i>dobFrom</i>, <i>dobTo</i><br/><br/>As an example of how this works, if this parameter is set <i>true</i> and an <i>offenderNo</i> is specified and an offender having this offender number is found, searching will stop and that offender will be returned immediately. If no offender matching the specified <i>offenderNo</i> is found, the search will be repeated using the next priority criteria (<i>pncNumber</i>) and so on. Note that offender name and date of birth criteria have the same priority and will be used together to search for matching offenders.") @QueryParam("prioritisedMatch") boolean prioritisedMatch,
                                      @ApiParam(value = "If <i>true</i>, offenders that match any of the specified criteria will be returned. The default search behaviour is to only return offenders that match <i>all</i> of the specified criteria. If the <i>prioritisedMatch</i> parameter is also set <i>true</i>, this parameter will only impact the behaviour of searching using offender name and date of birth criteria.") @QueryParam("anyMatch") boolean anyMatch,
                                      @ApiParam(value = "Requested offset of first record in returned collection of prisoner records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                      @ApiParam(value = "Requested limit to number of prisoner records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>offenderNo, pncNumber, croNumber, firstName, lastName, dob</b>") @HeaderParam("Sort-Fields") String sortFields,
                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{offenderNo}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of offenders globally matching the offenderNo.", notes = "List of offenders globally matching the offenderNo.", nickname="getPrisonersOffenderNo")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PrisonerDetail.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetPrisonersOffenderNoResponse getPrisonersOffenderNo(@ApiParam(value = "The offenderNo to search for", required = true) @PathParam("offenderNo") String offenderNo);

    class GetPrisonersResponse extends ResponseDelegate {

        private GetPrisonersResponse(Response response) { super(response); }
        private GetPrisonersResponse(Response response, Object entity) { super(response, entity); }

        public static GetPrisonersResponse respond200WithApplicationJson(Page<PrisonerDetail> page) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetPrisonersResponse(responseBuilder.build(), page.getItems());
        }

        public static GetPrisonersResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonersResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonersResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonersResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonersResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonersResponse(responseBuilder.build(), entity);
        }
    }

    class GetPrisonersOffenderNoResponse extends ResponseDelegate {

        private GetPrisonersOffenderNoResponse(Response response) { super(response); }
        private GetPrisonersOffenderNoResponse(Response response, Object entity) { super(response, entity); }

        public static GetPrisonersOffenderNoResponse respond200WithApplicationJson(List<PrisonerDetail> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonersOffenderNoResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonersOffenderNoResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonersOffenderNoResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonersOffenderNoResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonersOffenderNoResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonersOffenderNoResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonersOffenderNoResponse(responseBuilder.build(), entity);
        }
    }
}
