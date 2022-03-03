package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Case Note Type Usage Request
 **/
@SuppressWarnings("unused")
@Schema(description = "Case Note Type Usage Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteUsageRequest {

    @Schema(description = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate", example = "2018-11-01")
    private LocalDate fromDate;

    @Schema(description = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate", example = "2018-12-01")
    private LocalDate toDate;

    @Schema(description = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", example = "2")
    @Builder.Default
    private Integer numMonths = 1;

    @Builder.Default
    @Schema(required = true, description = "a list of offender numbers to search.")
    private List<String> offenderNos = new ArrayList<>();

    @Schema(description = "staff Id to use in search (optional).", example = "223423")
    private Integer staffId;

    @Schema(description = "Case note type.", example = "KA")
    private String type;

    @Schema(description = "Case note sub-type.", example = "KS")
    private String subType;

    @Schema(description = "Optional agency Id to filter by", example = "MDI")
    private String agencyId;

}
