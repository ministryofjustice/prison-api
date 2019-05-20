package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adjudication Summary for offender
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Adjudication Summary for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AdjudicationSummary {
    
    @NotNull
    private Long bookingId;

    @NotNull
    private Integer adjudicationCount;

    @NotNull
    @Builder.Default
    private List<Award> awards = new ArrayList<>();

    /**
      * Offender Booking Id
      */
    @ApiModelProperty(required = true, value = "Offender Booking Id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Number of proven adjudications
      */
    @ApiModelProperty(required = true, value = "Number of proven adjudications")
    @JsonProperty("adjudicationCount")
    public Integer getAdjudicationCount() {
        return adjudicationCount;
    }

    public void setAdjudicationCount(final Integer adjudicationCount) {
        this.adjudicationCount = adjudicationCount;
    }

    /**
      * List of awards / sanctions
      */
    @ApiModelProperty(required = true, value = "List of awards / sanctions")
    @JsonProperty("awards")
    public List<Award> getAwards() {
        return awards;
    }

    public void setAwards(final List<Award> awards) {
        this.awards = awards;
    }

    @Override
    public String toString()  {
        final var sb = new StringBuilder();

        sb.append("class AdjudicationSummary {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  adjudicationCount: ").append(adjudicationCount).append("\n");
        sb.append("  awards: ").append(awards).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
