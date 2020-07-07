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

    @ApiModelProperty(required = true, value = "The offenders noms ID", position = 1, example = "G0135GA")
    private String offenderNomsId;

    @ApiModelProperty(required = true, value = "The offenders first name", position = 2, example = "Joseph")
    private String firstName;

    @ApiModelProperty(required = true, value = "The offenders last name", position = 3, example = "Bloggs")
    private String lastName;

    @ApiModelProperty(required = true, value = "The non-association reason code", position = 4, example = "PER")
    private String reasonCode;

    @ApiModelProperty(required = true, value = "The non-association reason description", position = 5, example = "Perpetrator")
    private String reasonDescription;
}
