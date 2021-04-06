package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;

public class RequestToUpdateAddress {

    @ApiModelProperty("Flat")
    private String flat;

    @ApiModelProperty("Premise")
    private String premise;

    @ApiModelProperty("Street")
    private String street;

    @ApiModelProperty("Town")
    private String town;

    @ApiModelProperty("Locality")
    private String locality;

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

    @ApiModelProperty(value = "Date ended")
    private LocalDate endDate;
}
