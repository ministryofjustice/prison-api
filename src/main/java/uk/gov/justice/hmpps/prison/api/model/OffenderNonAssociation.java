package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Schema(description = "Offender non-association")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class OffenderNonAssociation {

    @Schema(required = true, description = "The offenders number", example = "G0135GA")
    private String offenderNo;

    @Schema(required = true, description = "The offenders first name", example = "Joseph")
    private String firstName;

    @Schema(required = true, description = "The offenders last name", example = "Bloggs")
    private String lastName;

    @Schema(required = true, description = "The non-association reason code", example = "PER")
    private String reasonCode;

    @Schema(required = true, description = "The non-association reason description", example = "Perpetrator")
    private String reasonDescription;

    @Schema(required = true, description = "Description of the agency (e.g. prison) the offender is assigned to.", example = "Pentonville (PVI)")
    private String agencyDescription;

    @Schema(required = true, description = "Description of living unit (e.g. cell) the offender is assigned to.", example = "PVI-1-2-4")
    private String assignedLivingUnitDescription;

    @Schema(required = true, description = "Id of living unit (e.g. cell) the offender is assigned to.", example = "123")
    private Long assignedLivingUnitId;
}
