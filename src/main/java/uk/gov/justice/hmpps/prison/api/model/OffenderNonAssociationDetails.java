package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@ApiModel(description = "Offender non-association details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class OffenderNonAssociationDetails {

    @ApiModelProperty(required = true, value = "The offenders number", position = 1, example = "G9109UD")
    private String offenderNo;

    @ApiModelProperty(required = true, value = "The offenders first name", position = 2, example = "Fred")
    private String firstName;

    @ApiModelProperty(required = true, value = "The offenders last name", position = 3, example = "Bloggs")
    private String lastName;

    @ApiModelProperty(required = true, value = "Description of the agency (e.g. prison) the offender is assigned to.", position = 4, example = "Moorland (HMP & YOI)")
    private String agencyDescription;


    @ApiModelProperty(required = true, value = "Description of living unit (e.g. cell) the offender is assigned to.", position = 5, example = "MDI-1-1-3")
    private String assignedLivingUnitDescription;

    @ApiModelProperty(value = "Offender non-association details", position = 6)
    @Builder.Default
    private Collection<OffenderNonAssociationDetail> nonAssociations = new ArrayList<>();

    @ApiModelProperty(required = true, value = "Id of living unit (e.g. cell) the offender is assigned to.", position = 7, example = "123")
    private Long assignedLivingUnitId;
}
