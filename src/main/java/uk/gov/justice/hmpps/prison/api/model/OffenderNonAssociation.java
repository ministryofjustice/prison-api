package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@ApiModel(description = "Offender non-association")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class OffenderNonAssociation {

    @ApiModelProperty(required = true, value = "Offender noms ID", position = 1, example = "G0135GA")
    private String offenderNomsId;

    @ApiModelProperty(required = true, value = "The non-association reason code", position = 2, example = "PER")
    private String reasonCode;

    @ApiModelProperty(required = true, value = "The non-association reason description", position = 3, example = "Perpetrator")
    private String reasonDescription;
}
