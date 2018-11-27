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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Case Note
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Note")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseNote {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long caseNoteId;

    @NotNull
    private Long bookingId;

    @NotBlank
    private String type;

    private String typeDescription;

    @NotBlank
    private String subType;

    private String subTypeDescription;

    @NotBlank
    private String source;

    @NotNull
    private LocalDateTime creationDateTime;

    @NotNull
    private LocalDateTime occurrenceDateTime;

    @NotNull
    private Long staffId;

    @NotBlank
    private String authorName;

    @NotBlank
    private String text;

    @NotBlank
    private String originalNoteText;

    @NotNull
    @Builder.Default
    private List<CaseNoteAmendment> amendments = new ArrayList<CaseNoteAmendment>();

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
      * Case Note Id (unique)
      */
    @ApiModelProperty(required = true, value = "Case Note Id (unique)")
    @JsonProperty("caseNoteId")
    public Long getCaseNoteId() {
        return caseNoteId;
    }

    public void setCaseNoteId(Long caseNoteId) {
        this.caseNoteId = caseNoteId;
    }

    /**
      * Booking Id of offender
      */
    @ApiModelProperty(required = true, value = "Booking Id of offender")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Case Note Type
      */
    @ApiModelProperty(required = true, value = "Case Note Type")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
      * Case Note Type Description
      */
    @ApiModelProperty(value = "Case Note Type Description")
    @JsonProperty("typeDescription")
    public String getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    /**
      * Case Note Sub Type
      */
    @ApiModelProperty(required = true, value = "Case Note Sub Type")
    @JsonProperty("subType")
    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
      * Case Note Sub Type Description
      */
    @ApiModelProperty(value = "Case Note Sub Type Description")
    @JsonProperty("subTypeDescription")
    public String getSubTypeDescription() {
        return subTypeDescription;
    }

    public void setSubTypeDescription(String subTypeDescription) {
        this.subTypeDescription = subTypeDescription;
    }

    /**
      * Source Type
      */
    @ApiModelProperty(required = true, value = "Source Type")
    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
      * Date and Time of Case Note creation
      */
    @ApiModelProperty(required = true, value = "Date and Time of Case Note creation")
    @JsonProperty("creationDateTime")
    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    /**
      * Date and Time of when case note contact with offender was made
      */
    @ApiModelProperty(required = true, value = "Date and Time of when case note contact with offender was made")
    @JsonProperty("occurrenceDateTime")
    public LocalDateTime getOccurrenceDateTime() {
        return occurrenceDateTime;
    }

    public void setOccurrenceDateTime(LocalDateTime occurrenceDateTime) {
        this.occurrenceDateTime = occurrenceDateTime;
    }

    /**
      * Id of staff member who created case note
      */
    @ApiModelProperty(required = true, value = "Id of staff member who created case note")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    /**
      * Name of staff member who created case note (lastname, firstname)
      */
    @ApiModelProperty(required = true, value = "Name of staff member who created case note (lastname, firstname)")
    @JsonProperty("authorName")
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
      * Case Note Text
      */
    @ApiModelProperty(required = true, value = "Case Note Text")
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
      * The initial case note information that was entered
      */
    @ApiModelProperty(required = true, value = "The initial case note information that was entered")
    @JsonProperty("originalNoteText")
    public String getOriginalNoteText() {
        return originalNoteText;
    }

    public void setOriginalNoteText(String originalNoteText) {
        this.originalNoteText = originalNoteText;
    }

    /**
      * Ordered list of amendments to the case note (oldest first)
      */
    @ApiModelProperty(required = true, value = "Ordered list of amendments to the case note (oldest first)")
    @JsonProperty("amendments")
    public List<CaseNoteAmendment> getAmendments() {
        return amendments;
    }

    public void setAmendments(List<CaseNoteAmendment> amendments) {
        this.amendments = amendments;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class CaseNote {\n");
        
        sb.append("  caseNoteId: ").append(caseNoteId).append("\n");
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  typeDescription: ").append(typeDescription).append("\n");
        sb.append("  subType: ").append(subType).append("\n");
        sb.append("  subTypeDescription: ").append(subTypeDescription).append("\n");
        sb.append("  source: ").append(source).append("\n");
        sb.append("  creationDateTime: ").append(creationDateTime).append("\n");
        sb.append("  occurrenceDateTime: ").append(occurrenceDateTime).append("\n");
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  authorName: ").append(authorName).append("\n");
        sb.append("  text: ").append(text).append("\n");
        sb.append("  originalNoteText: ").append(originalNoteText).append("\n");
        sb.append("  amendments: ").append(amendments).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
