package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    @Schema(required = true, description = "Display Prisoner Number (UK is NOMS ID)")
    private String offenderNo;

    @NotNull
    @Schema(required = true, description = "Timestamp when the external movement record was created")
    private LocalDateTime createDateTime;

    @NotBlank
    @Schema(required = true, description = "Agency travelling from")
    private String fromAgency;

    @Schema(required = true, description = "Description of the agency travelling from")
    private String fromAgencyDescription;

    @NotBlank
    @Schema(required = true, description = "Agency travelling to")
    private String toAgency;

    @Schema(required = true, description = "Description of the agency travelling to")
    private String toAgencyDescription;

    @Schema(description = "City offender was received from")
    private String fromCity;

    @Schema(description = "City offender was sent to")
    private String toCity;

    @NotBlank
    @Schema(required = true, description = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = {"ADM","CRT","REL","TAP","TRN"})
    private String movementType;

    @Schema(required = true, description = "Description of the movement type")
    private String movementTypeDescription;

    @Schema(required = true, description = "IN or OUT")
    @NotBlank
    private String directionCode;

    @Schema(required = true, description = "Movement date")
    private LocalDate movementDate;

    @Schema(required = true, description = "Movement time")
    private LocalTime movementTime;

    @Schema(required = true, description = "Description of movement reason")
    private String movementReason;

    @Schema(description = "Comment")
    private String commentText;

    public Movement(@NotBlank String offenderNo, @NotNull LocalDateTime createDateTime, @NotBlank String fromAgency, String fromAgencyDescription, @NotBlank String toAgency, String toAgencyDescription, String fromCity, String toCity, @NotBlank String movementType, String movementTypeDescription, @NotBlank String directionCode, LocalDate movementDate, LocalTime movementTime, String movementReason, String commentText) {
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
        this.commentText = commentText;
    }

    public Movement() {}
}
