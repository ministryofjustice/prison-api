package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Adjudication Summary for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationSummary {

    @JsonProperty("bookingId")
    @ApiModelProperty(required = true, value = "Offender Booking Id")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Number of proven adjudications")
    @JsonProperty("adjudicationCount")
    private Integer adjudicationCount;

    @ApiModelProperty(required = true, value = "List of awards / sanctions")
    @JsonProperty("awards")
    @NotNull
    @Builder.Default
    private List<Award> awards = new ArrayList<>();
}
