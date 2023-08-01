package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Offender non-association")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class OffenderNonAssociation {

    @Schema(requiredMode = REQUIRED, description = "The offenders number", example = "G0135GA")
    private String offenderNo;

    @Schema(requiredMode = REQUIRED, description = "The offenders first name", example = "Joseph")
    private String firstName;

    @Schema(requiredMode = REQUIRED, description = "The offenders last name", example = "Bloggs")
    private String lastName;

    @Schema(requiredMode = REQUIRED, description = "The non-association reason code", example = "PER")
    private String reasonCode;

    @Schema(requiredMode = REQUIRED, description = "The non-association reason description", example = "Perpetrator")
    private String reasonDescription;

    @Schema(requiredMode = REQUIRED, description = "Description of the agency (e.g. prison) the offender is assigned to.", example = "Pentonville (PVI)")
    private String agencyDescription;

    @Schema(requiredMode = REQUIRED, description = "Prison ID", example = "PVI")
    private String agencyId;

    @Schema(requiredMode = REQUIRED, description = "Description of living unit (e.g. cell) the offender is assigned to.", example = "PVI-1-2-4")
    private String assignedLivingUnitDescription;

    @Deprecated
    @Schema(requiredMode = REQUIRED, description = "Id of living unit (e.g. cell) the offender is assigned to. Will be removed in new API", example = "123")
    private Long assignedLivingUnitId;
}
