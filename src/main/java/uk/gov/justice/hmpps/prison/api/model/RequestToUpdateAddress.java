package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "Update Address Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestToUpdateAddress {

    @Schema(description = "Address Type. Note: Reference domain is ADDR_TYPE", example = "BUS", required = true)
    @NotBlank
    private String addressType;

    @Schema(description = "Flat", example = "3B")
    private String flat;

    @Schema(description = "Premise", example = "Liverpool Prison")
    @NotBlank
    private String premise;

    @Schema(description = "Street", example = "Slinn Street")
    private String street;

    @Schema(description = "Locality", example = "Brincliffe")
    private String locality;

    @Schema(description = "Town/City. Note: Reference domain is CITY", example = "Liverpool")
    private String town;

    @Schema(description = "Postal Code", example = "LI1 5TH")
    private String postalCode;

    @Schema(description = "County. Note: Reference domain is COUNTY", example = "HEREFORD")
    private String county;

    @Schema(description = "Country. Note: Reference domain is COUNTRY", example = "ENG")
    private String country;

    @Schema(description = "Comment", example = "This is a comment text")
    private String comment;

    @Schema(required = true, description = "Primary Address", example = "Y")
    private boolean primary;

    @Schema(required = true, description = "No Fixed Address", example = "N")
    private boolean noFixedAddress;

    @Schema(description = "Date Added", example = "2005-05-12")
    private LocalDate startDate;

    @Schema(description = "Date ended", example = "2021-02-12")
    private LocalDate endDate;
}
