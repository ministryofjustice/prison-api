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

@Schema(description = "Offender non-association details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class OffenderNonAssociationDetails {

    @Schema(required = true, description = "The offenders number", example = "G9109UD")
    private String offenderNo;

    @Schema(required = true, description = "The offenders first name", example = "Fred")
    private String firstName;

    @Schema(required = true, description = "The offenders last name", example = "Bloggs")
    private String lastName;

    @Schema(required = true, description = "Description of the agency (e.g. prison) the offender is assigned to.", example = "Moorland (HMP & YOI)")
    private String agencyDescription;


    @Schema(required = true, description = "Description of living unit (e.g. cell) the offender is assigned to.", example = "MDI-1-1-3")
    private String assignedLivingUnitDescription;

    @Schema(description = "Offender non-association details")
    @Builder.Default
    private Collection<OffenderNonAssociationDetail> nonAssociations = new ArrayList<>();

    @Schema(required = true, description = "Id of living unit (e.g. cell) the offender is assigned to.", example = "123")
    private Long assignedLivingUnitId;
}
