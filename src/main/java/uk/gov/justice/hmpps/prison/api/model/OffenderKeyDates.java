package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "Offender Key Dates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderKeyDates {

    @ApiModelProperty(value = "CRD - calculated conditional release date for offender.", example = "2020-02-03")
    private LocalDate conditionalReleaseDate;

    @ApiModelProperty(value = "LED - date on which offender licence expires.", example = "2020-02-03")
    private LocalDate licenceExpiryDate;

    @ApiModelProperty(value = "SED - date on which sentence expires.", example = "2020-02-03")
    private LocalDate sentenceExpiryDate;
}
