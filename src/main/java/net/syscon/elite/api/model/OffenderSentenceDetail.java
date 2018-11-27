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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Offender Sentence Detail
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Sentence Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenderSentenceDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String agencyLocationId;

    @NotBlank
    private String agencyLocationDesc;

    @NotBlank
    private String internalLocationDesc;

    private Long facialImageId;

    private SentenceDetail sentenceDetail;

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
      * Offender booking id.
      */
    @ApiModelProperty(required = true, value = "Offender booking id.")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Offender Unique Reference
      */
    @ApiModelProperty(required = true, value = "Offender Unique Reference")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * First Name
      */
    @ApiModelProperty(required = true, value = "First Name")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
      * Last Name
      */
    @ApiModelProperty(required = true, value = "Last Name")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
      * Offender date of birth.
      */
    @ApiModelProperty(required = true, value = "Offender date of birth.")
    @JsonProperty("dateOfBirth")
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
      * Agency Id
      */
    @ApiModelProperty(required = true, value = "Agency Id")
    @JsonProperty("agencyLocationId")
    public String getAgencyLocationId() {
        return agencyLocationId;
    }

    public void setAgencyLocationId(String agencyLocationId) {
        this.agencyLocationId = agencyLocationId;
    }

    /**
      * Agency Description
      */
    @ApiModelProperty(required = true, value = "Agency Description")
    @JsonProperty("agencyLocationDesc")
    public String getAgencyLocationDesc() {
        return agencyLocationDesc;
    }

    public void setAgencyLocationDesc(String agencyLocationDesc) {
        this.agencyLocationDesc = agencyLocationDesc;
    }

    /**
      * Description of the location within the prison
      */
    @ApiModelProperty(required = true, value = "Description of the location within the prison")
    @JsonProperty("internalLocationDesc")
    public String getInternalLocationDesc() {
        return internalLocationDesc;
    }

    public void setInternalLocationDesc(String internalLocationDesc) {
        this.internalLocationDesc = internalLocationDesc;
    }

    /**
      * Identifier of facial image of offender.
      */
    @ApiModelProperty(value = "Identifier of facial image of offender.")
    @JsonProperty("facialImageId")
    public Long getFacialImageId() {
        return facialImageId;
    }

    public void setFacialImageId(Long facialImageId) {
        this.facialImageId = facialImageId;
    }

    /**
      * Offender Sentence Detail Information
      */
    @ApiModelProperty(value = "Offender Sentence Detail Information")
    @JsonProperty("sentenceDetail")
    public SentenceDetail getSentenceDetail() {
        return sentenceDetail;
    }

    public void setSentenceDetail(SentenceDetail sentenceDetail) {
        this.sentenceDetail = sentenceDetail;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class OffenderSentenceDetail {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  dateOfBirth: ").append(dateOfBirth).append("\n");
        sb.append("  agencyLocationId: ").append(agencyLocationId).append("\n");
        sb.append("  agencyLocationDesc: ").append(agencyLocationDesc).append("\n");
        sb.append("  internalLocationDesc: ").append(internalLocationDesc).append("\n");
        sb.append("  facialImageId: ").append(facialImageId).append("\n");
        sb.append("  sentenceDetail: ").append(sentenceDetail).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
