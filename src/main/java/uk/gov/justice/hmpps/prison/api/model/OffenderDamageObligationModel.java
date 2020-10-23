package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@ApiModel(description = "Damage obligation for an offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderDamageObligationModel {

    @ApiModelProperty(value = "Identifier of damage obligation", example = "1", position = 1)
    private Long id;

    @ApiModelProperty(value = "Offender number", example = "G4346UT", position = 2)
    private String offenderNo;

    @ApiModelProperty(value = "Reference number", example = "012388", position = 3)
    private String referenceNumber;

    @ApiModelProperty(value = "The start date time when the damage obligation started", example = "2020-12-10T21:00:00", position = 4)
    private LocalDateTime startDateTime;

    @ApiModelProperty(value = "The end date time when the damage obligation ended", example = "2020-12-11T21:00:00", position = 5)
    private LocalDateTime endDateTime;

    @ApiModelProperty(value = "Prison the damages occurred", example = "MDI", position = 6)
    private String prisonId;

    @ApiModelProperty(value = "Amount left to pay", example = "50.0", position = 7)
    private BigDecimal amountToPay;

    @ApiModelProperty(value = "Amount paid", example = "10.0", position = 8)
    private BigDecimal amountPaid;

    @ApiModelProperty(value = "Status", example = "ACTIVE", position = 9)
    private String status;

    @ApiModelProperty(value = "Comment", example = "Damages to canteen furniture", position = 10)
    private String comment;

    @ApiModelProperty(value = "Currency of these amounts.", example = "GBP", position = 11)
    @NotBlank
    private String currency;
}

