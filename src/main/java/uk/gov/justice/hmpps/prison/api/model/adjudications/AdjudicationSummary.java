package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@Schema(description = "Adjudication Summary for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationSummary {

    @JsonProperty("bookingId")
    @Schema(required = true, description = "Offender Booking Id")
    private Long bookingId;

    @Schema(required = true, description = "Number of proven adjudications")
    @JsonProperty("adjudicationCount")
    private Integer adjudicationCount;

    @Schema(required = true, description = "List of awards / sanctions")
    @JsonProperty("awards")
    @NotNull
    @Builder.Default
    private List<Award> awards = new ArrayList<>();
}
