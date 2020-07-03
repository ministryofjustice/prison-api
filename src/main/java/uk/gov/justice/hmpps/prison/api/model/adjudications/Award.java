package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @ApiModelProperty(required = true, value = "Type of award")
    @JsonProperty("sanctionCode")
    private String sanctionCode;

    @ApiModelProperty(value = "Award type description")
    @JsonProperty("sanctionCodeDescription")
    private String sanctionCodeDescription;

    @ApiModelProperty(value = "Number of months duration")
    @JsonProperty("months")
    private Integer months;

    @ApiModelProperty(value = "Number of days duration")
    @JsonProperty("days")
    private Integer days;

    @ApiModelProperty(value = "Compensation amount")
    @JsonProperty("limit")
    private BigDecimal limit;

    @ApiModelProperty(value = "Optional details")
    @JsonProperty("comment")
    private String comment;

    @ApiModelProperty(required = true, value = "Start of sanction")
    @JsonProperty("effectiveDate")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "Award status (ref domain OIC_SANCT_ST)")
    @JsonProperty("status")
    private String status;

    @ApiModelProperty(value = "Award status description")
    @JsonProperty("statusDescription")
    private String statusDescription;

    @ApiModelProperty(required = true, value = "Id of hearing")
    @JsonProperty("hearingId")
    private Long hearingId;

    @ApiModelProperty(required = true, value = "hearing record sequence number")
    @JsonProperty("hearingSequence")
    private Integer hearingSequence;
}
