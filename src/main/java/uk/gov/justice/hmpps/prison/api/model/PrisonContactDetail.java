package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Contacts Details for agency
 **/
@Schema(description = "Contacts details for agency")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class PrisonContactDetail {
    @Schema(requiredMode = REQUIRED, description = "Identifier of agency/prison.", example = "MDI")
    @NotBlank
    private String agencyId;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Agency description.", example = "MOORLAND (HMP & YOI)")
    private String description;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Formatted agency description.", example = "Moorland (HMP & YOI)")
    private String formattedDescription;

    @Schema(requiredMode = REQUIRED, description = "Type of agency.", example = "INST")
    @NotBlank
    private String agencyType;

    @Schema(requiredMode = REQUIRED, description = "Type of address.")
    @NotBlank
    private String addressType;

    @Schema(requiredMode = REQUIRED, description = "The Prison name.")
    @NotBlank
    private String premise;

    @Schema(requiredMode = REQUIRED, description = "Describes the geographic location.")
    @NotBlank
    private String locality;

    @Schema(requiredMode = REQUIRED, description = "Address city.")
    @NotBlank
    private String city;

    @Schema(requiredMode = REQUIRED, description = "Address country.")
    @NotBlank
    private String country;

    @Schema(requiredMode = REQUIRED, description = "Address postcode.")
    @NotBlank
    private String postCode;

    @Schema(requiredMode = REQUIRED, description = "List of Telephone details")
    @NotNull
    @Builder.Default
    private List<Telephone> phones = new ArrayList<>();

    @Schema(description = "List of Address details")
    @NotNull
    @Builder.Default
    private List<AddressDto> addresses = new ArrayList<>();
}
