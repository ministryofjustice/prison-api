package net.syscon.elite.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "An Offender's Address")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderAddress {

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

    @ApiModelProperty(required = true, value = "Primary Address")
    private Boolean primary;

    @ApiModelProperty(required = true, value = "No Fixed Address")
    private Boolean noFixedAddress;

    @ApiModelProperty(value = "Date Added")
    private LocalDate startDate;

    @ApiModelProperty(value = "The phone number associated with the address", example = "0114 2345345")
    private String phoneNo;

    @ApiModelProperty(value = "The phone number extension", example = "345")
    private String phoneExt;

    @ApiModelProperty(value = "The phone number type", example = "HOME")
    private String phoneType;
}
