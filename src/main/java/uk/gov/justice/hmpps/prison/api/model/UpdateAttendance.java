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
import jakarta.validation.constraints.Size;

/**
 * Attendance details
 **/
@SuppressWarnings("unused")
@Schema(description = "Attendance details.  This is used to update the attendance details of an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class UpdateAttendance {

    @Schema(required = true, description = "Attendance outcome, possible values are the codes in the 'PS_PA_OC' reference domain.", example = "ATT", allowableValues = {"ABS","ACCAB","ATT","CANC","NREQ","SUS","UNACAB","REST"})
    @Size(max = 12)
    @NotBlank
    private String eventOutcome;

    @Schema(description = "Possible values are the codes in the 'PERFORMANCE' reference domain, mandatory for eventOutcome 'ATT'.", example = "ACCEPT", allowableValues = {"ACCEPT","GOOD","POOR","STANDARD","UNACCEPT"})
    @Size(max = 12)
    private String performance;

    @Schema(description = "Free text comment, maximum length 240 characters.", example = "Turned up very late")
    @Size(max = 240)
    private String outcomeComment;

}
