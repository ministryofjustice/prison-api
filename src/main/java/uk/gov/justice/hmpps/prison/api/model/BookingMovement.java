package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Movement")
@Data
public class BookingMovement {

    @Schema(description = "Sequence number")
    private Integer sequence;

    @Nullable
    @Schema(requiredMode = NOT_REQUIRED, description = "Agency travelling from")
    private String fromAgency;

    @Nullable
    @Schema(requiredMode = NOT_REQUIRED, description = "Agency travelling to")
    private String toAgency;

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = {"ADM", "CRT", "REL", "TAP", "TRN"})
    private String movementType;

    @Schema(requiredMode = REQUIRED, description = "IN or OUT")
    @NotBlank
    private String directionCode;

    @Schema(requiredMode = REQUIRED, description = "Movement timestamp")
    private LocalDateTime movementDateTime;

    @Schema(requiredMode = REQUIRED, description = "Code of movement reason")
    private String movementReasonCode;

    public BookingMovement(
        Integer sequence,
        @NotBlank String fromAgency,
        @NotBlank String toAgency,
        @NotBlank String movementType,
        @NotBlank String directionCode,
        LocalDateTime movementTime,
        String movementReasonCode
    ) {
        this.fromAgency = fromAgency;
        this.toAgency = toAgency;
        this.movementType = movementType;
        this.directionCode = directionCode;
        this.movementDateTime = movementTime;
        this.movementReasonCode = movementReasonCode;
        this.sequence = sequence;
    }
}
