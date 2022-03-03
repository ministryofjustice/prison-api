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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Offender Education")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Education {

    @NotNull
    @ApiModelProperty(value = "Offender booking id.", example = "14", required = true)
    private Long bookingId;

    @NotNull
    @ApiModelProperty(value = "Start date of education", example = "2018-02-11")
    private LocalDate startDate;

    @ApiModelProperty(value = "End date of education", example = "2020-02-11")
    private LocalDate endDate;

    @ApiModelProperty(value = "The area of study for the offender while in school.", example = "General Studies")
    private String studyArea;

    @ApiModelProperty(value = "The highest level attained for the educational period.", example = "Degree Level or Higher")
    private String educationLevel;

    @ApiModelProperty(value = "The number of educational years completed.", example = "2")
    private Integer numberOfYears;

    @ApiModelProperty(value = "Year of graduation.", example = "2021")
    private String graduationYear;

    @ApiModelProperty(value = "Comment relating to education.", example = "The education is going well")
    private String comment;

    @ApiModelProperty(value = "Name of school attended.", example = "School of economics")
    private String school;

    @ApiModelProperty(value = "Whether this is special education", example = "false", required = true)
    private Boolean isSpecialEducation;

    @ApiModelProperty(value = "The education schedule", example = "Full Time", required = true)
    private String schedule;

    @NotNull
    @Builder.Default
    @ApiModelProperty(value = "A list of addresses associated with the education", required = true)
    private List<AddressDto> addresses = new ArrayList<>();
}
