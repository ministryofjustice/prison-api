package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Search for adjudications")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjudicationSearchRequest {

    @Schema(requiredMode = REQUIRED, description = "The list of adjudications ids that mask the results", example = "[1,2,3]")
    private List<Long> adjudicationIdsMask;

    @Schema(description = "Agency Id", example = "MDI")
    @NotNull
    private String agencyLocationId;
}


