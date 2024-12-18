package uk.gov.justice.hmpps.prison.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "An Address")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDto {

    @Schema(description = "Address Id", example = "543524")
    private Long addressId;

    @Schema(description = "Address Type. Note: Reference domain is ADDR_TYPE", example = "BUS")
    private String addressType;

    @Schema(description = "Flat", example = "3B")
    private String flat;

    @Schema(description = "Premise", example = "Liverpool Prison")
    private String premise;

    @Schema(description = "Street", example = "Slinn Street")
    private String street;

    @Schema(description = "Locality", example = "Brincliffe")
    private String locality;

    @Schema(description = "Town/City description. Note: Reference domain is CITY", example = "Liverpool")
    private String town;

    @Schema(description = "Town/City code. Note: Reference domain is CITY", example = "17743")
    private String townCode;

    @Schema(description = "Postal Code", example = "LI1 5TH")
    private String postalCode;

    @Schema(description = "County description. Note: Reference domain is COUNTY", example = "Herefordshire")
    private String county;

    @Schema(description = "County code. Note: Reference domain is COUNTY", example = "HEREFORD")
    private String countyCode;

    @Schema(description = "Country description. Note: Reference domain is COUNTRY", example = "England")
    private String country;

    @Schema(description = "Country code. Note: Reference domain is COUNTRY", example = "ENG")
    private String countryCode;

    @Schema(description = "Comment", example = "This is a comment text")
    private String comment;

    @Schema(requiredMode = REQUIRED, description = "Primary Address", example = "Y")
    private Boolean primary;

    @Schema(requiredMode = REQUIRED, description = "Mail Address", example = "Y")
    private Boolean mail;

    @Schema(requiredMode = REQUIRED, description = "No Fixed Address", example = "N")
    private Boolean noFixedAddress;

    @Schema(description = "Date Added", example = "2005-05-12")
    private LocalDate startDate;

    @Schema(description = "Date ended", example = "2021-02-12")
    private LocalDate endDate;

    @Schema(description = "The phone number associated with the address")
    private List<Telephone> phones;

    @Schema(description = "The address usages/types")
    private List<AddressUsageDto> addressUsages;
}
