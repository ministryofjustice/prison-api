package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "Offender restriction")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenderRestriction {

    @Schema(required = true, description = "restriction id")
    private Long restrictionId;

    @Schema(description = "Restriction comment text")
    private String comment;

    @Schema(required = true, description = "code of restriction type")
    private String restrictionType;

    @Schema(required = true, description = "description of restriction type")
    @NotBlank
    private String restrictionTypeDescription;

    @Schema(required = true, description = "Date from which the restrictions applies", example="1980-01-01")
    private LocalDate startDate;

    @Schema(description = "Date restriction applies to, or indefinitely if null", example="1980-01-01")
    private LocalDate expiryDate;

    @Schema(required = true, description = "true if restriction is within the start date and optional expiry date range")
    private boolean active;
}
