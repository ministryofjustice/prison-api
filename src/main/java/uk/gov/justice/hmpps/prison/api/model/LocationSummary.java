package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Location Summary Details
 **/
@SuppressWarnings("unused")
@Schema(description = "Location Summary Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class LocationSummary {
    @Schema(description = "Location identifier.")
    @NotNull
    private Long locationId;

    @Schema(description = "User-friendly location description.")
    private String userDescription;

    @Schema(description = "Location description.")
    @NotBlank
    private String description;

    @Schema(description = "Identifier of Agency this location is associated with.")
    @NotNull
    @JsonIgnore
    private String agencyId;

    public LocationSummary(@NotNull Long locationId, String userDescription, @NotBlank String description, @NotNull String agencyId) {
        this.locationId = locationId;
        this.userDescription = userDescription;
        this.description = description;
        this.agencyId = agencyId;
    }

    public LocationSummary() {
    }
}
