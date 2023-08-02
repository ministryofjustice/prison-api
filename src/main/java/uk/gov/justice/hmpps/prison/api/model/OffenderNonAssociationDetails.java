package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Offender non-association details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class OffenderNonAssociationDetails {

    @Schema(requiredMode = REQUIRED, description = "The offenders number", example = "G9109UD")
    private String offenderNo;

    @Schema(requiredMode = REQUIRED, description = "The offenders first name", example = "Fred")
    private String firstName;

    @Schema(requiredMode = REQUIRED, description = "The offenders last name", example = "Bloggs")
    private String lastName;

    @Schema(requiredMode = REQUIRED, description = "Description of the agency (e.g. prison) the offender is assigned to.", example = "Moorland (HMP & YOI)")
    private String agencyDescription;

    @Schema(requiredMode = REQUIRED, description = "Prison ID", example = "MDI")
    private String agencyId;

    @Schema(requiredMode = REQUIRED, description = "Description of living unit (e.g. cell) the offender is assigned to.", example = "MDI-1-1-3")
    private String assignedLivingUnitDescription;

    @Schema(description = "Offender non-association details")
    @Builder.Default
    private Collection<OffenderNonAssociationDetail> nonAssociations = new ArrayList<>();

    @Deprecated
    @Schema(requiredMode = REQUIRED, description = "Id of living unit (e.g. cell) the offender is assigned to., will be removed in new API", example = "123")
    private Long assignedLivingUnitId;
}
