package uk.gov.justice.hmpps.prison.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@ApiModel(description = "Details required for IEP review for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OffenderIepReview {
    @ApiModelProperty(value = "Booking ID of offender", required = true, example = "1111111")
    private long bookingId;

    @ApiModelProperty(value = "Number for case notes of type POS and subtype IEP_ENC", required = true, example = "3")
    private int positiveIeps;

    @ApiModelProperty(value = "Number for case notes of type NEG and subtype IEP_WARN", required = true, example = "3")
    private int negativeIeps;

    @ApiModelProperty(value = "Number of proven adjudications", required = true, example = "3")
    private int provenAdjudications;

    @ApiModelProperty(value = "Date of last IEP review", example = "2017-03-17T08:02:00")
    private LocalDateTime lastReviewTime;

    @ApiModelProperty(value = "The current IEP level for offender", example = "Basic")
    private String currentLevel;

    @ApiModelProperty(value = "Offender first name", required = true, example = "John")
    private String firstName;

    @ApiModelProperty(value = "Offender middle name", example = "James")
    private String middleName;

    @ApiModelProperty(value = "Offender last name", required = true, example = "Smith")
    private String lastName;

    @ApiModelProperty(value = "The current cell location of Offender", required = true, example = "LEI-A-3-003")
    private String cellLocation;

    @ApiModelProperty(value = "Offender Number", required = true, example = "G1401GN")
    private String offenderNo;

}
