package net.syscon.elite.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import net.syscon.elite.api.model.Agency;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class AdjudicationSearchResponse {

    @ApiModelProperty("Search results")
    private List<Adjudication> results;

    @ApiModelProperty("A complete list of the type of offences that this offender has had adjudications for")
    private List<AdjudicationOffence> offences;

    @ApiModelProperty("Complete list of agencies where this offender has had adjudications")
    private List<Agency> agencies;
}
