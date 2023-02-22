package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Schema(description = "Damage obligation for an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderDamageObligationModel {

    @Schema(description = "Identifier of damage obligation", example = "1")
    private Long id;

    @Schema(description = "Offender number", example = "G4346UT")
    private String offenderNo;

    @Schema(description = "Reference number", example = "841177/1, A841821/1, 842371")
    private String referenceNumber;

    @Schema(description = "The start date time when the damage obligation started", example = "2020-12-10T21:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "The end date time when the damage obligation ended", example = "2020-12-11T21:00:00")
    private LocalDateTime endDateTime;

    @Schema(description = "Prison the damages occurred", example = "MDI")
    private String prisonId;

    @Schema(description = "Original amount to pay", example = "50.0")
    private BigDecimal amountToPay;

    @Schema(description = "Amount paid", example = "10.0")
    private BigDecimal amountPaid;

    @Schema(description = "Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Comment", example = "Damages to canteen furniture")
    private String comment;

    @Schema(description = "Currency of these amounts.", example = "GBP")
    @NotBlank
    private String currency;
}

