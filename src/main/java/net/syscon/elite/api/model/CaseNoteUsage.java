package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Case Note Type Usage
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Note Type Usage")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseNoteUsage {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String caseNoteType;

    @NotBlank
    private String caseNoteSubType;

    @NotBlank
    private String offenderNo;

    @NotNull
    private Integer numCaseNotes;

    @NotNull
    private LocalDateTime latestCaseNote;

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
      * Case Note Type
      */
    @ApiModelProperty(required = true, value = "Case Note Type")
    @JsonProperty("caseNoteType")
    public String getCaseNoteType() {
        return caseNoteType;
    }

    public void setCaseNoteType(String caseNoteType) {
        this.caseNoteType = caseNoteType;
    }

    /**
      * Case Note Sub Type
      */
    @ApiModelProperty(required = true, value = "Case Note Sub Type")
    @JsonProperty("caseNoteSubType")
    public String getCaseNoteSubType() {
        return caseNoteSubType;
    }

    public void setCaseNoteSubType(String caseNoteSubType) {
        this.caseNoteSubType = caseNoteSubType;
    }

    /**
      * Offender No
      */
    @ApiModelProperty(required = true, value = "Offender No")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * Number of case notes of this type/subtype
      */
    @ApiModelProperty(required = true, value = "Number of case notes of this type/subtype")
    @JsonProperty("numCaseNotes")
    public Integer getNumCaseNotes() {
        return numCaseNotes;
    }

    public void setNumCaseNotes(Integer numCaseNotes) {
        this.numCaseNotes = numCaseNotes;
    }

    /**
      * Last case note of this type
      */
    @ApiModelProperty(required = true, value = "Last case note of this type")
    @JsonProperty("latestCaseNote")
    public LocalDateTime getLatestCaseNote() {
        return latestCaseNote;
    }

    public void setLatestCaseNote(LocalDateTime latestCaseNote) {
        this.latestCaseNote = latestCaseNote;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class CaseNoteUsage {\n");
        
        sb.append("  caseNoteType: ").append(caseNoteType).append("\n");
        sb.append("  caseNoteSubType: ").append(caseNoteSubType).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  numCaseNotes: ").append(numCaseNotes).append("\n");
        sb.append("  latestCaseNote: ").append(latestCaseNote).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
