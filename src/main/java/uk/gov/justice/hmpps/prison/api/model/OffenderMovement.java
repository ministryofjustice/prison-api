package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;


@Schema(description = "Prisoner Movement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderMovement {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotBlank
    @Schema(required = true, description = "Display Prisoner Number (UK is NOMS ID)")
    private String offenderNo;

    @NotNull
    private Long bookingId;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Schema(required = true, description = "Agency travelling from")
    private String fromAgency;

    @NotBlank
    @Schema(required = true, description = "Description for Agency travelling from")
    private String fromAgencyDescription;

    @NotBlank
    @Schema(required = true, description = "Agency travelling to")
    private String toAgency;

    @NotBlank
    @Schema(required = true, description = "Description for Agency travelling to")
    private String toAgencyDescription;

    @NotBlank
    @Schema(required = true, description = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = {"ADM","CRT","REL","TAP","TRN"})
    private String movementType;

    @NotBlank
    @Schema(required = true, description = "Description of the movement type")
    private String movementTypeDescription;

    @NotBlank
    @Schema(required = true, description = "Reason code for the movement")
    private String movementReason;

    @NotBlank
    @Schema(required = true, description = "Description of the movement reason")
    private String movementReasonDescription;

    @NotBlank
    @Schema(required = true, description = "IN or OUT")
    private String directionCode;

    @NotNull
    @Schema(required = true, description = "Movement time")
    private LocalTime movementTime;

    @NotNull
    @Schema(required = true, description = "Movement date")
    private LocalDate movementDate;

}
