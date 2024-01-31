package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Prisoner Custody Status
 **/
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Prisoner Custody Status")
@Builder(toBuilder = true)
@Data
public class Movement {

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Display Prisoner Number (UK is NOMS ID)")
    private String offenderNo;

    @NotNull
    @Schema(requiredMode = REQUIRED, description = "Timestamp when the external movement record was created")
    private LocalDateTime createDateTime;

    @Nullable
    @Schema(requiredMode = NOT_REQUIRED, description = "Agency travelling from")
    private String fromAgency;

    @Nullable
    @Schema(requiredMode = NOT_REQUIRED, description = "Description of the agency travelling from")
    private String fromAgencyDescription;

    @Nullable
    @Schema(requiredMode = NOT_REQUIRED, description = "Agency travelling to")
    private String toAgency;

    @Nullable
    @Schema(requiredMode = NOT_REQUIRED, description = "Description of the agency travelling to")
    private String toAgencyDescription;

    @Schema(description = "City offender was received from")
    private String fromCity;

    @Schema(description = "City offender was sent to")
    private String toCity;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = {"ADM","CRT","REL","TAP","TRN"})
    private String movementType;

    @Schema(requiredMode = REQUIRED, description = "Description of the movement type")
    private String movementTypeDescription;

    @Schema(requiredMode = REQUIRED, description = "IN or OUT")
    @NotBlank
    private String directionCode;

    @Schema(requiredMode = REQUIRED, description = "Movement date")
    private LocalDate movementDate;

    @Schema(requiredMode = REQUIRED, description = "Movement time")
    private LocalTime movementTime;

    @Schema(requiredMode = REQUIRED, description = "Description of movement reason")
    private String movementReason;

    @Schema(requiredMode = REQUIRED, description = "Code of movement reason")
    private String movementReasonCode;

    @Schema(description = "Comment")
    private String commentText;

    public Movement(@NotBlank String offenderNo, @NotNull LocalDateTime createDateTime, @NotBlank String fromAgency, String fromAgencyDescription, @NotBlank String toAgency, String toAgencyDescription, String fromCity, String toCity, @NotBlank String movementType, String movementTypeDescription, @NotBlank String directionCode, LocalDate movementDate, LocalTime movementTime, String movementReason, String movementReasonCode, String commentText) {
        this.offenderNo = offenderNo;
        this.createDateTime = createDateTime;
        this.fromAgency = fromAgency;
        this.fromAgencyDescription = fromAgencyDescription;
        this.toAgency = toAgency;
        this.toAgencyDescription = toAgencyDescription;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.movementType = movementType;
        this.movementTypeDescription = movementTypeDescription;
        this.directionCode = directionCode;
        this.movementDate = movementDate;
        this.movementTime = movementTime;
        this.movementReason = movementReason;
        this.movementReasonCode = movementReasonCode;
        this.commentText = commentText;
    }

    public Movement() {}
}
