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

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ApiModel(description = "A result from a hearing")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HearingResult {

    @ApiModelProperty("OIC Offence Code")
    private String oicOffenceCode;

    @ApiModelProperty("Offence Type")
    private String offenceType;

    @ApiModelProperty("Offence Description")
    private String offenceDescription;

    @ApiModelProperty("Plea")
    private String plea;

    @ApiModelProperty("Finding")
    private String finding;

    @Singular
    @ApiModelProperty
    private List<Sanction> sanctions;

    @JsonIgnore
    private long oicHearingId;

    @JsonIgnore
    private Long resultSeq;
}
