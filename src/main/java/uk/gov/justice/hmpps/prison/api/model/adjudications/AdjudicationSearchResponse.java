package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.model.Agency;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class AdjudicationSearchResponse {

    @Schema(description = "Search results")
    private List<Adjudication> results;

    @Schema(description = "A complete list of the type of offences that this offender has had adjudications for")
    private List<AdjudicationOffence> offences;

    @Schema(description = "Complete list of agencies where this offender has had adjudications")
    private List<Agency> agencies;
}
