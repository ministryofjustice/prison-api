package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Offender Employment")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderEmploymentResponse {

    @ApiModelProperty(value = "Offender booking id.", example = "14")
    private Long bookingId;

    @ApiModelProperty(value = "Start date of employment")
    private LocalDate startDate;

    @ApiModelProperty(value = "End date of employment")
    private LocalDate endDate;

    @ApiModelProperty(value = "The employment post type condition")
    private String postType;

    @ApiModelProperty(value = "The obfuscated name of the employer")
    private String employerName;

    @ApiModelProperty(value = "The obfuscated name of the supervisor in the employment")
    private String supervisorName;

    @ApiModelProperty(value = "Position held on job")
    private String position;

    @ApiModelProperty(value = "The reason for leaving job")
    private String terminationReason;

    @ApiModelProperty(value = "Amount the offender was earning")
    private Double wage;

    @ApiModelProperty(value = "The frequency of wage payments")
    private String wagePeriod;

    @ApiModelProperty(value = "The occupation name of the offender")
    private String occupation;

    @ApiModelProperty(value = "A comment about the employment")
    private String comment;

    @ApiModelProperty(value = "The employment schedule")
    private String schedule;

    @ApiModelProperty(value = "The hours worked per week")
    private Integer hoursWeek;

    @ApiModelProperty(value = "Whether the employer is aware of the offender's charges")
    private Boolean isEmployerAware;

    @ApiModelProperty(value = "Whether the employer can be contacted or not")
    private Boolean isEmployerContactable;

    @NotNull
    @ApiModelProperty(value = "A list of addresses associated with the employment")
    private List<AddressDto> addresses = new ArrayList<>();
}
