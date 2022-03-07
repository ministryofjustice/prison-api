package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "List of visitors for a visit")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitWithVisitors {
    @ApiModelProperty(value = "List of visitors on visit", required = true)
    @JsonProperty("visitors")
    @NotEmpty
    private List<Visitor> visitors;

    @ApiModelProperty(value = "Visit Information", required = true)
    @JsonProperty("visitDetails")
    @NotNull
    private VisitDetails visitDetail;
}
