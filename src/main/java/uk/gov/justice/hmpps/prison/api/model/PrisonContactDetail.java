package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Contacts Details for agency
 **/
@ApiModel(description = "Contacts details for agency")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class PrisonContactDetail {
    @ApiModelProperty(required = true, position = 1, value = "Identifier of agency/prison.", example = "MDI")
    @NotBlank
    private String agencyId;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency description.", example = "MOORLAND (HMP & YOI)")
    private String description;

    @NotBlank
    @ApiModelProperty(required = true, value = "Formatted agency description.", example = "Moorland (HMP & YOI)")
    private String formattedDescription;

    @ApiModelProperty(required = true, position = 2, value = "Type of agency.", example = "INST")
    @NotBlank
    private String agencyType;

    @ApiModelProperty(required = true, position = 3, value = "Type of address.")
    @NotBlank
    private String addressType;

    @ApiModelProperty(required = true, position = 4, value = "The Prison name.")
    @NotBlank
    private String premise;

    @ApiModelProperty(required = true, position = 5, value = "Describes the geographic location.")
    @NotBlank
    private String locality;

    @ApiModelProperty(required = true, position = 6, value = "Address city.")
    @NotBlank
    private String city;

    @ApiModelProperty(required = true, position = 7, value = "Address country.")
    @NotBlank
    private String country;

    @ApiModelProperty(required = true, position = 8, value = "Address postcode.")
    @NotBlank
    private String postCode;

    @ApiModelProperty(required = true, position = 9, value = "List of Telephone details")
    @NotNull
    @Builder.Default
    private List<Telephone> phones = new ArrayList<>();

    @ApiModelProperty(required = false, position = 10, value = "List of Address details")
    @NotNull
    @Builder.Default
    private List<AddressDto> addresses = new ArrayList<>();
}
