package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@ApiModel(description = "Update Address Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestToUpdateAddress {

    @ApiModelProperty(value = "Address Type", notes= "Reference domain is ADDR_TYPE", example = "BUS", position = 2, required = true)
    @NotBlank
    private String addressType;

    @ApiModelProperty(value = "Flat", example = "3B", position = 3)
    private String flat;

    @ApiModelProperty(value = "Premise", example = "Liverpool Prison", position = 4)
    @NotBlank
    private String premise;

    @ApiModelProperty(value = "Street", example = "Slinn Street", position = 5)
    private String street;

    @ApiModelProperty(value = "Locality", example = "Brincliffe", position = 6)
    private String locality;

    @ApiModelProperty(value = "Town/City", notes = "Reference domain is CITY", example = "Liverpool", position = 7)
    private String town;

    @ApiModelProperty(value = "Postal Code", example = "LI1 5TH", position = 8)
    private String postalCode;

    @ApiModelProperty(value = "County", notes = "Reference domain is COUNTY", example = "HEREFORD", position = 9)
    private String county;

    @ApiModelProperty(value = "Country", notes = "Reference domain is COUNTRY", example = "ENG", position = 10)
    private String country;

    @ApiModelProperty(value = "Comment", example = "This is a comment text", position = 11)
    private String comment;

    @ApiModelProperty(required = true, value = "Primary Address", example = "Y", position = 12)
    private boolean primary;

    @ApiModelProperty(required = true, value = "No Fixed Address", example = "N", position = 13)
    private boolean noFixedAddress;

    @ApiModelProperty(value = "Date Added", example = "2005-05-12", position = 14)
    private LocalDate startDate;

    @ApiModelProperty(value = "Date ended", example = "2021-02-12", position = 15)
    private LocalDate endDate;
}
