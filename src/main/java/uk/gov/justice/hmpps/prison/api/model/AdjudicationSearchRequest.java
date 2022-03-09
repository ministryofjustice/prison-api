package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "Search for adjudications")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjudicationSearchRequest {

    @ApiModelProperty(required = true, value = "The list of adjudications ids that mask the results", example = "[1,2,3]")
    private List<Long> adjudicationIdsMask;

    @ApiModelProperty(value = "Agency Id", example = "MDI", position = 2)
    @NotNull
    private String agencyLocationId;
}


