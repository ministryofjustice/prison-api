package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Offender Sentence Detail Dto
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Offender Sentence Detail Dto")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenderSentenceDetailDto {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    private LocalDate sentenceStartDate;

    private Integer additionalDaysAwarded;

    private LocalDate sentenceExpiryDate;

    private LocalDate automaticReleaseDate;

    private LocalDate automaticReleaseOverrideDate;

    private LocalDate conditionalReleaseDate;

    private LocalDate conditionalReleaseOverrideDate;

    private LocalDate nonParoleDate;

    private LocalDate nonParoleOverrideDate;

    private LocalDate postRecallReleaseDate;

    private LocalDate postRecallReleaseOverrideDate;

    private LocalDate nonDtoReleaseDate;

    public enum NonDtoReleaseDateType {
         ARD,  CRD,  NPD,  PRRD, 
    };

    private NonDtoReleaseDateType nonDtoReleaseDateType;

    private LocalDate licenceExpiryDate;

    private LocalDate homeDetentionCurfewEligibilityDate;

    private LocalDate paroleEligibilityDate;

    private LocalDate homeDetentionCurfewActualDate;

    private LocalDate actualParoleDate;

    private LocalDate releaseOnTemporaryLicenceDate;

    private LocalDate earlyRemovalSchemeEligibilityDate;

    private LocalDate earlyTermDate;

    private LocalDate midTermDate;

    private LocalDate lateTermDate;

    private LocalDate topupSupervisionExpiryDate;

    private LocalDate confirmedReleaseDate;

    private LocalDate releaseDate;

    private LocalDate tariffDate;

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
      * Sentence start date.
      */
    @ApiModelProperty(value = "Sentence start date.")
    @JsonProperty("sentenceStartDate")
    public LocalDate getSentenceStartDate() {
        return sentenceStartDate;
    }

    public void setSentenceStartDate(LocalDate sentenceStartDate) {
        this.sentenceStartDate = sentenceStartDate;
    }

    /**
      * ADA - days added to sentence term due to adjustments.
      */
    @ApiModelProperty(value = "ADA - days added to sentence term due to adjustments.")
    @JsonProperty("additionalDaysAwarded")
    public Integer getAdditionalDaysAwarded() {
        return additionalDaysAwarded;
    }

    public void setAdditionalDaysAwarded(Integer additionalDaysAwarded) {
        this.additionalDaysAwarded = additionalDaysAwarded;
    }

    /**
      * SED - date on which sentence expires.
      */
    @ApiModelProperty(value = "SED - date on which sentence expires.")
    @JsonProperty("sentenceExpiryDate")
    public LocalDate getSentenceExpiryDate() {
        return sentenceExpiryDate;
    }

    public void setSentenceExpiryDate(LocalDate sentenceExpiryDate) {
        this.sentenceExpiryDate = sentenceExpiryDate;
    }

    /**
      * ARD - calculated automatic (unconditional) release date for offender.
      */
    @ApiModelProperty(value = "ARD - calculated automatic (unconditional) release date for offender.")
    @JsonProperty("automaticReleaseDate")
    public LocalDate getAutomaticReleaseDate() {
        return automaticReleaseDate;
    }

    public void setAutomaticReleaseDate(LocalDate automaticReleaseDate) {
        this.automaticReleaseDate = automaticReleaseDate;
    }

    /**
      * ARD (override) - automatic (unconditional) release override date for offender.
      */
    @ApiModelProperty(value = "ARD (override) - automatic (unconditional) release override date for offender.")
    @JsonProperty("automaticReleaseOverrideDate")
    public LocalDate getAutomaticReleaseOverrideDate() {
        return automaticReleaseOverrideDate;
    }

    public void setAutomaticReleaseOverrideDate(LocalDate automaticReleaseOverrideDate) {
        this.automaticReleaseOverrideDate = automaticReleaseOverrideDate;
    }

    /**
      * CRD - calculated conditional release date for offender.
      */
    @ApiModelProperty(value = "CRD - calculated conditional release date for offender.")
    @JsonProperty("conditionalReleaseDate")
    public LocalDate getConditionalReleaseDate() {
        return conditionalReleaseDate;
    }

    public void setConditionalReleaseDate(LocalDate conditionalReleaseDate) {
        this.conditionalReleaseDate = conditionalReleaseDate;
    }

    /**
      * CRD (override) - conditional release override date for offender.
      */
    @ApiModelProperty(value = "CRD (override) - conditional release override date for offender.")
    @JsonProperty("conditionalReleaseOverrideDate")
    public LocalDate getConditionalReleaseOverrideDate() {
        return conditionalReleaseOverrideDate;
    }

    public void setConditionalReleaseOverrideDate(LocalDate conditionalReleaseOverrideDate) {
        this.conditionalReleaseOverrideDate = conditionalReleaseOverrideDate;
    }

    /**
      * NPD - calculated non-parole date for offender (relating to the 1991 act).
      */
    @ApiModelProperty(value = "NPD - calculated non-parole date for offender (relating to the 1991 act).")
    @JsonProperty("nonParoleDate")
    public LocalDate getNonParoleDate() {
        return nonParoleDate;
    }

    public void setNonParoleDate(LocalDate nonParoleDate) {
        this.nonParoleDate = nonParoleDate;
    }

    /**
      * NPD (override) - non-parole override date for offender.
      */
    @ApiModelProperty(value = "NPD (override) - non-parole override date for offender.")
    @JsonProperty("nonParoleOverrideDate")
    public LocalDate getNonParoleOverrideDate() {
        return nonParoleOverrideDate;
    }

    public void setNonParoleOverrideDate(LocalDate nonParoleOverrideDate) {
        this.nonParoleOverrideDate = nonParoleOverrideDate;
    }

    /**
      * PRRD - calculated post-recall release date for offender.
      */
    @ApiModelProperty(value = "PRRD - calculated post-recall release date for offender.")
    @JsonProperty("postRecallReleaseDate")
    public LocalDate getPostRecallReleaseDate() {
        return postRecallReleaseDate;
    }

    public void setPostRecallReleaseDate(LocalDate postRecallReleaseDate) {
        this.postRecallReleaseDate = postRecallReleaseDate;
    }

    /**
      * PRRD (override) - post-recall release override date for offender.
      */
    @ApiModelProperty(value = "PRRD (override) - post-recall release override date for offender.")
    @JsonProperty("postRecallReleaseOverrideDate")
    public LocalDate getPostRecallReleaseOverrideDate() {
        return postRecallReleaseOverrideDate;
    }

    public void setPostRecallReleaseOverrideDate(LocalDate postRecallReleaseOverrideDate) {
        this.postRecallReleaseOverrideDate = postRecallReleaseOverrideDate;
    }

    /**
      * Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD.
      */
    @ApiModelProperty(value = "Release date for non-DTO sentence (if applicable). This will be based on one of ARD, CRD, NPD or PRRD.")
    @JsonProperty("nonDtoReleaseDate")
    public LocalDate getNonDtoReleaseDate() {
        return nonDtoReleaseDate;
    }

    public void setNonDtoReleaseDate(LocalDate nonDtoReleaseDate) {
        this.nonDtoReleaseDate = nonDtoReleaseDate;
    }

    /**
      * Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'.
      */
    @ApiModelProperty(value = "Indicates which type of non-DTO release date is the effective release date. One of 'ARD', 'CRD', 'NPD' or 'PRRD'.")
    @JsonProperty("nonDtoReleaseDateType")
    public NonDtoReleaseDateType getNonDtoReleaseDateType() {
        return nonDtoReleaseDateType;
    }

    public void setNonDtoReleaseDateType(NonDtoReleaseDateType nonDtoReleaseDateType) {
        this.nonDtoReleaseDateType = nonDtoReleaseDateType;
    }

    /**
      * LED - date on which offender licence expires.
      */
    @ApiModelProperty(value = "LED - date on which offender licence expires.")
    @JsonProperty("licenceExpiryDate")
    public LocalDate getLicenceExpiryDate() {
        return licenceExpiryDate;
    }

    public void setLicenceExpiryDate(LocalDate licenceExpiryDate) {
        this.licenceExpiryDate = licenceExpiryDate;
    }

    /**
      * HDCED - date on which offender will be eligible for home detention curfew.
      */
    @ApiModelProperty(value = "HDCED - date on which offender will be eligible for home detention curfew.")
    @JsonProperty("homeDetentionCurfewEligibilityDate")
    public LocalDate getHomeDetentionCurfewEligibilityDate() {
        return homeDetentionCurfewEligibilityDate;
    }

    public void setHomeDetentionCurfewEligibilityDate(LocalDate homeDetentionCurfewEligibilityDate) {
        this.homeDetentionCurfewEligibilityDate = homeDetentionCurfewEligibilityDate;
    }

    /**
      * PED - date on which offender is eligible for parole.
      */
    @ApiModelProperty(value = "PED - date on which offender is eligible for parole.")
    @JsonProperty("paroleEligibilityDate")
    public LocalDate getParoleEligibilityDate() {
        return paroleEligibilityDate;
    }

    public void setParoleEligibilityDate(LocalDate paroleEligibilityDate) {
        this.paroleEligibilityDate = paroleEligibilityDate;
    }

    /**
      * HDCAD - the offender's actual home detention curfew date.
      */
    @ApiModelProperty(value = "HDCAD - the offender's actual home detention curfew date.")
    @JsonProperty("homeDetentionCurfewActualDate")
    public LocalDate getHomeDetentionCurfewActualDate() {
        return homeDetentionCurfewActualDate;
    }

    public void setHomeDetentionCurfewActualDate(LocalDate homeDetentionCurfewActualDate) {
        this.homeDetentionCurfewActualDate = homeDetentionCurfewActualDate;
    }

    /**
      * APD - the offender's actual parole date.
      */
    @ApiModelProperty(value = "APD - the offender's actual parole date.")
    @JsonProperty("actualParoleDate")
    public LocalDate getActualParoleDate() {
        return actualParoleDate;
    }

    public void setActualParoleDate(LocalDate actualParoleDate) {
        this.actualParoleDate = actualParoleDate;
    }

    /**
      * ROTL - the date on which offender will be released on temporary licence.
      */
    @ApiModelProperty(value = "ROTL - the date on which offender will be released on temporary licence.")
    @JsonProperty("releaseOnTemporaryLicenceDate")
    public LocalDate getReleaseOnTemporaryLicenceDate() {
        return releaseOnTemporaryLicenceDate;
    }

    public void setReleaseOnTemporaryLicenceDate(LocalDate releaseOnTemporaryLicenceDate) {
        this.releaseOnTemporaryLicenceDate = releaseOnTemporaryLicenceDate;
    }

    /**
      * ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).
      */
    @ApiModelProperty(value = "ERSED - the date on which offender will be eligible for early removal (under the Early Removal Scheme for foreign nationals).")
    @JsonProperty("earlyRemovalSchemeEligibilityDate")
    public LocalDate getEarlyRemovalSchemeEligibilityDate() {
        return earlyRemovalSchemeEligibilityDate;
    }

    public void setEarlyRemovalSchemeEligibilityDate(LocalDate earlyRemovalSchemeEligibilityDate) {
        this.earlyRemovalSchemeEligibilityDate = earlyRemovalSchemeEligibilityDate;
    }

    /**
      * ETD - early term date for offender.
      */
    @ApiModelProperty(value = "ETD - early term date for offender.")
    @JsonProperty("earlyTermDate")
    public LocalDate getEarlyTermDate() {
        return earlyTermDate;
    }

    public void setEarlyTermDate(LocalDate earlyTermDate) {
        this.earlyTermDate = earlyTermDate;
    }

    /**
      * MTD - mid term date for offender.
      */
    @ApiModelProperty(value = "MTD - mid term date for offender.")
    @JsonProperty("midTermDate")
    public LocalDate getMidTermDate() {
        return midTermDate;
    }

    public void setMidTermDate(LocalDate midTermDate) {
        this.midTermDate = midTermDate;
    }

    /**
      * LTD - late term date for offender.
      */
    @ApiModelProperty(value = "LTD - late term date for offender.")
    @JsonProperty("lateTermDate")
    public LocalDate getLateTermDate() {
        return lateTermDate;
    }

    public void setLateTermDate(LocalDate lateTermDate) {
        this.lateTermDate = lateTermDate;
    }

    /**
      * TUSED - top-up supervision expiry date for offender.
      */
    @ApiModelProperty(value = "TUSED - top-up supervision expiry date for offender.")
    @JsonProperty("topupSupervisionExpiryDate")
    public LocalDate getTopupSupervisionExpiryDate() {
        return topupSupervisionExpiryDate;
    }

    public void setTopupSupervisionExpiryDate(LocalDate topupSupervisionExpiryDate) {
        this.topupSupervisionExpiryDate = topupSupervisionExpiryDate;
    }

    /**
      * Confirmed release date for offender.
      */
    @ApiModelProperty(value = "Confirmed release date for offender.")
    @JsonProperty("confirmedReleaseDate")
    public LocalDate getConfirmedReleaseDate() {
        return confirmedReleaseDate;
    }

    public void setConfirmedReleaseDate(LocalDate confirmedReleaseDate) {
        this.confirmedReleaseDate = confirmedReleaseDate;
    }

    /**
      * Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm.
      */
    @ApiModelProperty(value = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm.")
    @JsonProperty("releaseDate")
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
      * Date on which minimum term is reached for parole (indeterminate/life sentences).
      */
    @ApiModelProperty(value = "Date on which minimum term is reached for parole (indeterminate/life sentences).")
    @JsonProperty("tariffDate")
    public LocalDate getTariffDate() {
        return tariffDate;
    }

    public void setTariffDate(LocalDate tariffDate) {
        this.tariffDate = tariffDate;
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

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class OffenderSentenceDetailDto {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  sentenceStartDate: ").append(sentenceStartDate).append("\n");
        sb.append("  additionalDaysAwarded: ").append(additionalDaysAwarded).append("\n");
        sb.append("  sentenceExpiryDate: ").append(sentenceExpiryDate).append("\n");
        sb.append("  automaticReleaseDate: ").append(automaticReleaseDate).append("\n");
        sb.append("  automaticReleaseOverrideDate: ").append(automaticReleaseOverrideDate).append("\n");
        sb.append("  conditionalReleaseDate: ").append(conditionalReleaseDate).append("\n");
        sb.append("  conditionalReleaseOverrideDate: ").append(conditionalReleaseOverrideDate).append("\n");
        sb.append("  nonParoleDate: ").append(nonParoleDate).append("\n");
        sb.append("  nonParoleOverrideDate: ").append(nonParoleOverrideDate).append("\n");
        sb.append("  postRecallReleaseDate: ").append(postRecallReleaseDate).append("\n");
        sb.append("  postRecallReleaseOverrideDate: ").append(postRecallReleaseOverrideDate).append("\n");
        sb.append("  nonDtoReleaseDate: ").append(nonDtoReleaseDate).append("\n");
        sb.append("  nonDtoReleaseDateType: ").append(nonDtoReleaseDateType).append("\n");
        sb.append("  licenceExpiryDate: ").append(licenceExpiryDate).append("\n");
        sb.append("  homeDetentionCurfewEligibilityDate: ").append(homeDetentionCurfewEligibilityDate).append("\n");
        sb.append("  paroleEligibilityDate: ").append(paroleEligibilityDate).append("\n");
        sb.append("  homeDetentionCurfewActualDate: ").append(homeDetentionCurfewActualDate).append("\n");
        sb.append("  actualParoleDate: ").append(actualParoleDate).append("\n");
        sb.append("  releaseOnTemporaryLicenceDate: ").append(releaseOnTemporaryLicenceDate).append("\n");
        sb.append("  earlyRemovalSchemeEligibilityDate: ").append(earlyRemovalSchemeEligibilityDate).append("\n");
        sb.append("  earlyTermDate: ").append(earlyTermDate).append("\n");
        sb.append("  midTermDate: ").append(midTermDate).append("\n");
        sb.append("  lateTermDate: ").append(lateTermDate).append("\n");
        sb.append("  topupSupervisionExpiryDate: ").append(topupSupervisionExpiryDate).append("\n");
        sb.append("  confirmedReleaseDate: ").append(confirmedReleaseDate).append("\n");
        sb.append("  releaseDate: ").append(releaseDate).append("\n");
        sb.append("  tariffDate: ").append(tariffDate).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  dateOfBirth: ").append(dateOfBirth).append("\n");
        sb.append("  agencyLocationId: ").append(agencyLocationId).append("\n");
        sb.append("  agencyLocationDesc: ").append(agencyLocationDesc).append("\n");
        sb.append("  internalLocationDesc: ").append(internalLocationDesc).append("\n");
        sb.append("  facialImageId: ").append(facialImageId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
