package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Search for adjudications")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjudicationSearchRequest {

    @Schema(required = true, description = "The list of adjudications ids that mask the results", example = "[1,2,3]")
    private List<Long> adjudicationIdsMask;

    @Schema(description = "Agency Id", example = "MDI")
    @NotNull
    private String agencyLocationId;
}


