package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
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
 * Case Note Type Usage Request
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Note Type Usage Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseNoteUsageRequest {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    @Builder.Default
    private List<String> offenderNos = new ArrayList<String>();

    private Integer staffId;

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
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * a list of offender numbers to search.
      */
    @ApiModelProperty(required = true, value = "a list of offender numbers to search.")
    @JsonProperty("offenderNos")
    public List<String> getOffenderNos() {
        return offenderNos;
    }

    public void setOffenderNos(List<String> offenderNos) {
        this.offenderNos = offenderNos;
    }

    /**
      * staff Id to use in search (optional).
      */
    @ApiModelProperty(value = "staff Id to use in search (optional).")
    @JsonProperty("staffId")
    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    /**
      * Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month
      */
    @ApiModelProperty(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month")
    @JsonProperty("numMonths")
    public Integer getNumMonths() {
        return numMonths;
    }

    public void setNumMonths(Integer numMonths) {
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

    public void setFromDate(LocalDate fromDate) {
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

    public void setToDate(LocalDate toDate) {
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

    public void setType(String type) {
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

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class CaseNoteUsageRequest {\n");
        
        sb.append("  offenderNos: ").append(offenderNos).append("\n");
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  numMonths: ").append(numMonths).append("\n");
        sb.append("  fromDate: ").append(fromDate).append("\n");
        sb.append("  toDate: ").append(toDate).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  subType: ").append(subType).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
