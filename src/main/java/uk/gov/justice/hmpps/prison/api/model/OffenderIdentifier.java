package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Offender Identifier
 **/
@SuppressWarnings("unused")
@Schema(description = "Offender Identifier")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderIdentifier {
    @NotBlank
    @Schema(required = true, description = "Type of offender identifier", example = "PNC")
    private String type;

    @NotBlank
    @Schema(required = true, description = "The value of the offender identifier", example = "1231/XX/121")
    private String value;

    @Schema(description = "The offender number for this identifier", example = "A1234AB")
    private String offenderNo;

    @Schema(description = "The booking ID for this identifier", example = "1231223")
    private Long bookingId;

    @Schema(description = "Issuing Authority Information", example = "Important Auth")
    private String issuedAuthorityText;

    @Schema(description = "Date of issue", example = "2018-01-21")
    private LocalDate issuedDate;

    @Schema(description = "Related caseload type", example = "GENERAL")
    private String caseloadType;
}
