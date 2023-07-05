package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;


@Schema(description = "Adjudication Summary for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationSummary {

    @JsonProperty("bookingId")
    @Schema(requiredMode = REQUIRED, description = "Offender Booking Id")
    private Long bookingId;

    @Schema(requiredMode = REQUIRED, description = "Number of proven adjudications")
    @JsonProperty("adjudicationCount")
    private Integer adjudicationCount;

    @Schema(requiredMode = REQUIRED, description = "List of awards / sanctions")
    @JsonProperty("awards")
    @NotNull
    @Builder.Default
    private List<Award> awards = new ArrayList<>();
}
