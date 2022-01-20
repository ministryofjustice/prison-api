package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@ApiModel(description = "Adjudication award / sanction")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Award {

    @ApiModelProperty(required = true, value = "Id of booking")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Type of award")
    private String sanctionCode;

    @ApiModelProperty(value = "Award type description")
    private String sanctionCodeDescription;

    @ApiModelProperty(value = "Number of months duration")
    private Integer months;

    @ApiModelProperty(value = "Number of days duration")
    private Integer days;

    @ApiModelProperty(value = "Compensation amount")
    private BigDecimal limit;

    @ApiModelProperty(value = "Optional details")
    private String comment;

    @ApiModelProperty(required = true, value = "Start of sanction")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "Award status (ref domain OIC_SANCT_ST)")
    private String status;

    @ApiModelProperty(value = "Award status description")
    private String statusDescription;

    @ApiModelProperty(required = true, value = "Id of hearing")
    private Long hearingId;

    @ApiModelProperty(required = true, value = "hearing record sequence number")
    private Integer hearingSequence;
}
