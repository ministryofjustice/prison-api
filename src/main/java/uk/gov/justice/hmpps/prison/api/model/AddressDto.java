package uk.gov.justice.hmpps.prison.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "An Address")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDto {

    @ApiModelProperty("Address Id")
    private Long addressId;

    @ApiModelProperty("Address Type")
    private String addressType;

    @ApiModelProperty("Flat")
    private String flat;

    @ApiModelProperty("Premise")
    private String premise;

    @ApiModelProperty("Street")
    private String street;

    @ApiModelProperty("Town")
    private String town;

    @ApiModelProperty("Postal Code")
    private String postalCode;

    @ApiModelProperty("County")
    private String county;

    @ApiModelProperty("Country")
    private String country;

    @ApiModelProperty("Comment")
    private String comment;

    @ApiModelProperty("Locality")
    private String locality;

    @ApiModelProperty(required = true, value = "Primary Address")
    private Boolean primary;

    @ApiModelProperty(required = true, value = "No Fixed Address")
    private Boolean noFixedAddress;

    @ApiModelProperty(value = "Date Added")
    private LocalDate startDate;

    @ApiModelProperty(value = "Date ended")
    private LocalDate endDate;

    @ApiModelProperty(value = "The phone number associated with the address")
    private List<Telephone> phones;

    @ApiModelProperty(value = "The address usages/types")
    private List<AddressUsageDto> addressUsages;
}
