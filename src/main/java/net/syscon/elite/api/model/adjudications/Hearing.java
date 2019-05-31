package net.syscon.elite.api.model.adjudications;

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

    @ApiModelProperty("OIC Hearing ID")
    private Long oicHearingId;

    @ApiModelProperty("Hearing Type")
    private String hearingType;

    @ApiModelProperty("Hearing Time")
    private LocalDateTime hearingTime;

    @ApiModelProperty("Establishment")
    private String establishment;

    @ApiModelProperty("Hearing Location")
    private String location;

    @JsonIgnore
    private Long internalLocationId;

    @ApiModelProperty("Adjudicator First name")
    private String heardByFirstName;

    @ApiModelProperty("Adjudicator Last name")
    private String heardByLastName;

    @ApiModelProperty("Other Representatives")
    private String otherRepresentatives;

    @ApiModelProperty("Comment")
    private String comment;

    @Singular
    @ApiModelProperty("Hearing Results")
    private List<HearingResult> results;
}
