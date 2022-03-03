package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Case Note Type Usage Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteUsageRequest {

    @ApiModelProperty(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate", position = 1, example = "2018-11-01")
    private LocalDate fromDate;

    @ApiModelProperty(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate", position = 2, example = "2018-12-01")
    private LocalDate toDate;

    @ApiModelProperty(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", position = 3, example = "2")
    @Builder.Default
    private Integer numMonths = 1;

    @Builder.Default
    @ApiModelProperty(required = true, value = "a list of offender numbers to search.", position = 4)
    private List<String> offenderNos = new ArrayList<>();

    @ApiModelProperty(value = "staff Id to use in search (optional).", position = 5, example = "223423")
    private Integer staffId;

    @ApiModelProperty(value = "Case note type.", position = 6, example = "KA")
    private String type;

    @ApiModelProperty(value = "Case note sub-type.", position = 7, example = "KS")
    private String subType;

    @ApiModelProperty(value = "Optional agency Id to filter by", position = 8, example = "MDI")
    private String agencyId;

}
