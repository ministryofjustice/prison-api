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

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ApiModel(description = "A result from a hearing")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HearingResult {

    @ApiModelProperty(value = "OIC Offence Code", example = "51:22")
    private String oicOffenceCode;

    @ApiModelProperty(value = "Offence Type", example = "Prison Rule 51")
    private String offenceType;

    @ApiModelProperty(value = "Offence Description", example = "Disobeys any lawful order")
    private String offenceDescription;

    @ApiModelProperty(value = "Plea", example = "Guilty")
    private String plea;

    @ApiModelProperty(value = "Finding", example = "Charge Proved")
    private String finding;

    @Singular
    @ApiModelProperty
    private List<Sanction> sanctions;

    @JsonIgnore
    private long oicHearingId;

    @JsonIgnore
    private Long resultSeq;
}
