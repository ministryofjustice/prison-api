package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
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

    @ApiModelProperty(value = "End date if range the offence was believed to have taken place", example = "2018-03-10", position = 3)
    private LocalDate offenceRangeDate;

    @ApiModelProperty(required = true, value = "Description associated with the offence code", example = "Commit an act / series of acts with intent to pervert the course of public justice", position = 4)
    @NotBlank
    private String offenceDescription;

    @ApiModelProperty(required = true, value = "Reference Code", example = "RR84070", position = 5)
    @NotBlank
    private String offenceCode;

    @ApiModelProperty(required = true, value = "Statute code", example = "RR84", position = 6)
    @NotBlank
    private String statuteCode;

    @ApiModelProperty(required = true, value = "Identifies the main offence per booking", position = 7)
    private Boolean mostSerious;

    @ApiModelProperty(value = "Primary result code ", position = 8)
    private String primaryResultCode;

    @ApiModelProperty(value = "Secondary result code", position = 9)
    private String secondaryResultCode;

    @ApiModelProperty(value = "Description for Primary result", position = 10)
    private String primaryResultDescription;

    @ApiModelProperty(value = "Description for Secondary result", position = 11)
    private String secondaryResultDescription;

    @ApiModelProperty(value = "Conviction flag for Primary result ", position = 12)
    private Boolean primaryResultConviction;

    @ApiModelProperty(value = "Conviction flag for Secondary result ", position = 13)
    private Boolean secondaryResultConviction;

    @ApiModelProperty(value = "Latest court date associated with the offence", example = "2018-02-10", position = 14)
    private LocalDate courtDate;

    @ApiModelProperty(value = "Court case id", example = "100", position = 15)
    private Long caseId;
}
