package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Api(tags = {"/api"})
public interface NomisApiResource {

    String NOMS_ID_REGEX_PATTERN = "[a-zA-Z][0-9]{4}[a-zA-Z]{2}";

    @SuppressWarnings("RestParamTypeInspection")
    @GetMapping("/api/offenders/{offenderNo}/transaction-history")
    @ApiOperation(value = "Retrieve an offender's financial transaction history for cash, spends or savings.", notes = "Transactions are returned in NOMIS ordee (Descending date followed by id).<br/>" +
            "All transaction amounts are represented as pence values.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Not a digital prison.  Prison not found. Offender has no account at this prison.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Prison, offender or accountType not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    TransactionHistory getTransactionsHistory(
            @ApiParam(name = "prison_id", value = "Prison ID", example = "WLI", required = true) @RequestParam("prison_id") @NotNull @Size(max = 3) String prisonId,
            @ApiParam(name = "noms_id", value = "Offender Noms Id", example = "A1404AE", required = true) @RequestParam("noms_id") @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
            @ApiParam(name = "account_code", value = "Account code", example = "spends", required = true, allowableValues = "spends,cash,savings") @RequestParam("account_code") @NotNull String accountCode,
            @ApiParam(name = "from_date", value = "Start date for transactions (defaults to today if not supplied)", example = "2019-04-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "from_date", required = false) LocalDate fromDate,
            @ApiParam(name = "to_date", value = "To date for transactions (defaults to today if not supplied)", example = "2019-05-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "to_date", required = false) LocalDate toDate);
}
