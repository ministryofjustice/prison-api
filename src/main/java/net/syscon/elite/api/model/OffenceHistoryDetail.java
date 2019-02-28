package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Offence History Item")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenceHistoryDetail {

    @ApiModelProperty(required = true, value = "Prisoner booking id", example = "1123456", position = 1)
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Date the offence took place", example = "2018-02-10", position = 2)
    @NotNull
    private LocalDate offenceDate;

    @ApiModelProperty(required = false, value = "End date if range the offence was believed to have taken place", example = "2018-03-10", position = 3)
    private LocalDate offenceRangeDate;

    @ApiModelProperty(required = true, value = "Description associated with the offence code", example = "Commit an act / series of acts with intent to pervert the course of public justice", position = 4)
    @NotBlank
    private String offenceDescription;

    @ApiModelProperty(required = true, value = "Identifies the main offence per booking", position = 5)
    private Boolean mostSerious;
}
