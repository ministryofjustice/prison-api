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
 * Adjudication Details for offender
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Adjudication Details for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AdjudicationDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotNull
    private Integer adjudicationCount;

    @NotNull
    @Builder.Default
    private List<Award> awards = new ArrayList<Award>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Offender Booking Id
      */
    @ApiModelProperty(required = true, value = "Offender Booking Id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
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

    public void setAdjudicationCount(Integer adjudicationCount) {
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

    public void setAwards(List<Award> awards) {
        this.awards = awards;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class AdjudicationDetail {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  adjudicationCount: ").append(adjudicationCount).append("\n");
        sb.append("  awards: ").append(awards).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
