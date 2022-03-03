package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Case Note Type Staff Usage Request
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Note Type Staff Usage Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseNoteStaffUsageRequest {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    @Builder.Default
    private List<Integer> staffIds = new ArrayList<Integer>();

    private Integer numMonths;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String type;

    private String subType;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * a list of staff numbers to search.
     */
    @ApiModelProperty(required = true, value = "a list of staff numbers to search.")
    @JsonProperty("staffIds")
    public List<Integer> getStaffIds() {
        return staffIds;
    }

    public void setStaffIds(final List<Integer> staffIds) {
        this.staffIds = staffIds;
    }

    /**
     * Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month
     */
    @ApiModelProperty(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month")
    @JsonProperty("numMonths")
    public Integer getNumMonths() {
        return numMonths;
    }

    public void setNumMonths(final Integer numMonths) {
        this.numMonths = numMonths;
    }

    /**
     * Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate
     */
    @ApiModelProperty(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate")
    @JsonProperty("fromDate")
    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(final LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate
     */
    @ApiModelProperty(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate")
    @JsonProperty("toDate")
    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(final LocalDate toDate) {
        this.toDate = toDate;
    }

    /**
     * Case note type.
     */
    @ApiModelProperty(value = "Case note type.")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Case note sub-type.
     */
    @ApiModelProperty(value = "Case note sub-type.")
    @JsonProperty("subType")
    public String getSubType() {
        return subType;
    }

    public void setSubType(final String subType) {
        this.subType = subType;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class CaseNoteStaffUsageRequest {\n");

        sb.append("  staffIds: ").append(staffIds).append("\n");
        sb.append("  numMonths: ").append(numMonths).append("\n");
        sb.append("  fromDate: ").append(fromDate).append("\n");
        sb.append("  toDate: ").append(toDate).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  subType: ").append(subType).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
