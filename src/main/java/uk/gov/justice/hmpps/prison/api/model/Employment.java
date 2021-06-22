package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Offender Employment")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Employment {

    @NotNull
    @ApiModelProperty(value = "Offender booking id.", example = "14", required = true)
    private Long bookingId;

    @NotNull
    @ApiModelProperty(value = "Start date of employment", example = "2018-02-11", required = true)
    private LocalDate startDate;

    @ApiModelProperty(value = "End date of employment", example = "2018-05-11")
    private LocalDate endDate;

    @ApiModelProperty(value = "The employment post type condition", example = "Full Time")
    private String postType;

    @ApiModelProperty(value = "The name of the employer", example = "Greggs")
    private String employerName;

    @ApiModelProperty(value = "The name of the supervisor in the employment", example = "John Smith")
    private String supervisorName;

    @ApiModelProperty(value = "Position held on job", example = "Supervisor")
    private String position;

    @ApiModelProperty(value = "The reason for leaving job", example = "End of contract")
    private String terminationReason;

    @ApiModelProperty(value = "Amount the offender was earning", example = "10.0")
    private BigDecimal wage;

    @ApiModelProperty(value = "The frequency of wage payments", example = "Hourly")
    private String wagePeriod;

    @ApiModelProperty(value = "The occupation name of the offender", example = "builder")
    private String occupation;

    @ApiModelProperty(value = "A comment about the employment", example = "The employment is going well")
    private String comment;

    @ApiModelProperty(value = "The employment schedule", example = "Fortnightly")
    private String schedule;

    @ApiModelProperty(value = "The hours worked per week", example = "32")
    private Integer hoursWeek;

    @NotNull
    @ApiModelProperty(value = "Whether the employer is aware of the offender's charges", example = "true", required = true)
    private Boolean isEmployerAware;

    @NotNull
    @ApiModelProperty(value = "Whether the employer can be contacted or not", example = "false", required = true)
    private Boolean isEmployerContactable;

    @NotNull
    @Builder.Default
    @ApiModelProperty(value = "A list of addresses associated with the employment", required = true)
    private List<AddressDto> addresses = new ArrayList<>();
}
