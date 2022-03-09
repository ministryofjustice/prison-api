package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.List;

@ApiModel(description = "Prisoners time in prison summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class PrisonerInPrisonSummary {
    @Id
    @ApiModelProperty(value = "Prisoner Identifier", example = "A1234AA", required = true, position = 1)
    private String prisonerNumber;

    @ApiModelProperty(value = "List of date when prisoner was in prison", position = 2)
    private List<PrisonPeriod> prisonPeriod;

}
