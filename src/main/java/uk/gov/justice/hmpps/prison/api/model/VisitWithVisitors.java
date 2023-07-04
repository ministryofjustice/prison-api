package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "List of visitors for a visit")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitWithVisitors {
    @Schema(description = "List of visitors on visit", requiredMode = REQUIRED)
    @JsonProperty("visitors")
    @NotEmpty
    private List<Visitor> visitors;

    @Schema(description = "Visit Information", requiredMode = REQUIRED)
    @JsonProperty("visitDetails")
    @NotNull
    private VisitDetails visitDetail;
}
