package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;


@Schema(description = "Prisoner Movement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
public class OffenderMovement {
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
    @Schema(required = true, description = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = "ADM,CRT,REL,TAP,TRN")
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

    public OffenderMovement(@NotBlank String offenderNo, @NotNull Long bookingId, @NotNull LocalDate dateOfBirth, @NotBlank String firstName, String middleName, @NotBlank String lastName, @NotBlank String fromAgency, @NotBlank String fromAgencyDescription, @NotBlank String toAgency, @NotBlank String toAgencyDescription, @NotBlank String movementType, @NotBlank String movementTypeDescription, @NotBlank String movementReason, @NotBlank String movementReasonDescription, @NotBlank String directionCode, @NotNull LocalTime movementTime, @NotNull LocalDate movementDate) {
        this.offenderNo = offenderNo;
        this.bookingId = bookingId;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.fromAgency = fromAgency;
        this.fromAgencyDescription = fromAgencyDescription;
        this.toAgency = toAgency;
        this.toAgencyDescription = toAgencyDescription;
        this.movementType = movementType;
        this.movementTypeDescription = movementTypeDescription;
        this.movementReason = movementReason;
        this.movementReasonDescription = movementReasonDescription;
        this.directionCode = directionCode;
        this.movementTime = movementTime;
        this.movementDate = movementDate;
    }

    public OffenderMovement() {}
}
