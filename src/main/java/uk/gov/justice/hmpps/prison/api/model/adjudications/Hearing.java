package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ApiModel(description = "An Adjudication Hearing")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Hearing {

    @ApiModelProperty(value = "OIC Hearing ID", example = "1985937")
    private Long oicHearingId;

    @ApiModelProperty(value = "Hearing Type", example = "Governor's Hearing Adult")
    private String hearingType;

    @ApiModelProperty(value = "Hearing Time", example = "2017-03-17T08:30:00")
    private LocalDateTime hearingTime;

    @ApiModelProperty(value = "Establishment", example = "Moorland (HMP & YOI)")
    private String establishment;

    @ApiModelProperty(value = "Hearing Location", example = "Adjudication Room")
    private String location;

    @JsonIgnore
    private Long internalLocationId;

    @ApiModelProperty(value = "Adjudicator First name", example = "Bob")
    private String heardByFirstName;

    @ApiModelProperty(value = "Adjudicator Last name", example = "Smith")
    private String heardByLastName;

    @ApiModelProperty(value = "Other Representatives", example = "Councillor Adams")
    private String otherRepresentatives;

    @ApiModelProperty(value = "Comment", example = "The defendant conducted themselves in a manner...")
    private String comment;

    @Singular
    @ApiModelProperty("Hearing Results")
    private List<HearingResult> results;
}
