package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * New Offender Booking
 **/
@SuppressWarnings("unused")
@ApiModel(description = "New Offender Booking")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewBooking {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @Length(max = 35)
    @NotBlank
    private String lastName;

    @Length(max = 35)
    @NotBlank
    private String firstName;

    @Length(max = 35)
    private String middleName1;

    @Length(max = 35)
    private String middleName2;

    @Length(max = 12)
    private String title;

    @Length(max = 12)
    private String suffix;

    @Pattern(regexp = "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$")
    @NotBlank
    private String dateOfBirth;

    @Length(max = 12)
    @NotBlank
    private String gender;

    @NotBlank
    private String reason;

    private boolean youthOffender;

    @Length(max = 12)
    private String ethnicity;

    @Length(max = 10)
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$")
    private String offenderNo;

    @Length(max = 20)
    private String pncNumber;

    @Length(max = 20)
    private String croNumber;

    @Length(max = 20)
    private String externalIdentifier;

    @Length(max = 12)
    private String externalIdentifierType;

    @Length(max = 36)
    private String correlationId;

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
     * The offender's first name.
     */
    @ApiModelProperty(required = true, value = "The offender's first name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * The offender's last name.
     */
    @ApiModelProperty(required = true, value = "The offender's last name.")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * The offender's middle name.
     */
    @ApiModelProperty(value = "The offender's middle name.")
    @JsonProperty("middleName1")
    public String getMiddleName1() {
        return middleName1;
    }

    public void setMiddleName1(final String middleName1) {
        this.middleName1 = middleName1;
    }

    /**
     * An additional middle name for the offender.
     */
    @ApiModelProperty(value = "An additional middle name for the offender.")
    @JsonProperty("middleName2")
    public String getMiddleName2() {
        return middleName2;
    }

    public void setMiddleName2(final String middleName2) {
        this.middleName2 = middleName2;
    }

    /**
     * A code representing the offender's title (from TITLE reference domain).
     */
    @ApiModelProperty(value = "A code representing the offender's title (from TITLE reference domain).")
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * A code representing a suffix to apply to offender's name (from SUFFIX reference domain).
     */
    @ApiModelProperty(value = "A code representing a suffix to apply to offender's name (from SUFFIX reference domain).")
    @JsonProperty("suffix")
    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    /**
     * The offender's date of birth. Must be specified in YYYY-MM-DD format.
     */
    @ApiModelProperty(required = true, value = "The offender's date of birth. Must be specified in YYYY-MM-DD format.")
    @JsonProperty("dateOfBirth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * A code representing the offender's gender (from the SEX reference domain).
     */
    @ApiModelProperty(required = true, value = "A code representing the offender's gender (from the SEX reference domain).")
    @JsonProperty("gender")
    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    /**
     * A code representing the reason for the offender's admission.
     */
    @ApiModelProperty(required = true, value = "A code representing the reason for the offender's admission.")
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    /**
     * A flag to indicate that the offender is a youth/young offender (or not). Defaults to false if not specified.
     */
    @ApiModelProperty(value = "A flag to indicate that the offender is a youth/young offender (or not). Defaults to false if not specified.")
    @JsonProperty("youthOffender")
    public boolean getYouthOffender() {
        return youthOffender;
    }

    public void setYouthOffender(final boolean youthOffender) {
        this.youthOffender = youthOffender;
    }

    /**
     * A code representing the offender's ethnicity (from the ETHNICITY reference domain).
     */
    @ApiModelProperty(value = "A code representing the offender's ethnicity (from the ETHNICITY reference domain).")
    @JsonProperty("ethnicity")
    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(final String ethnicity) {
        this.ethnicity = ethnicity;
    }

    /**
     * A unique offender number. If set, a new booking will be created for an existing offender. If not set, a new offender and new offender booking will be created (subject to de-duplication checks).
     */
    @ApiModelProperty(value = "A unique offender number. If set, a new booking will be created for an existing offender. If not set, a new offender and new offender booking will be created (subject to de-duplication checks).")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(final String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
     * The offender's PNC (Police National Computer) number.
     */
    @ApiModelProperty(value = "The offender's PNC (Police National Computer) number.")
    @JsonProperty("pncNumber")
    public String getPncNumber() {
        return pncNumber;
    }

    public void setPncNumber(final String pncNumber) {
        this.pncNumber = pncNumber;
    }

    /**
     * The offender's CRO (Criminal Records Office) number.
     */
    @ApiModelProperty(value = "The offender's CRO (Criminal Records Office) number.")
    @JsonProperty("croNumber")
    public String getCroNumber() {
        return croNumber;
    }

    public void setCroNumber(final String croNumber) {
        this.croNumber = croNumber;
    }

    /**
     * An external system identifier for the offender or offender booking. This may be useful if the booking is being created by an external system.
     */
    @ApiModelProperty(value = "An external system identifier for the offender or offender booking. This may be useful if the booking is being created by an external system.")
    @JsonProperty("externalIdentifier")
    public String getExternalIdentifier() {
        return externalIdentifier;
    }

    public void setExternalIdentifier(final String externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    /**
     * A code representing the type of external identifier specified in <i>externalIdentifier</> property (from ID_TYPE reference domain).
     */
    @ApiModelProperty(value = "A code representing the type of external identifier specified in <i>externalIdentifier</> property (from ID_TYPE reference domain).")
    @JsonProperty("externalIdentifierType")
    public String getExternalIdentifierType() {
        return externalIdentifierType;
    }

    public void setExternalIdentifierType(final String externalIdentifierType) {
        this.externalIdentifierType = externalIdentifierType;
    }

    /**
     * A unique correlation id for idempotent request control.
     */
    @ApiModelProperty(value = "A unique correlation id for idempotent request control.")
    @JsonProperty("correlationId")
    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(final String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class NewBooking {\n");

        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  middleName1: ").append(middleName1).append("\n");
        sb.append("  middleName2: ").append(middleName2).append("\n");
        sb.append("  title: ").append(title).append("\n");
        sb.append("  suffix: ").append(suffix).append("\n");
        sb.append("  dateOfBirth: ").append(dateOfBirth).append("\n");
        sb.append("  gender: ").append(gender).append("\n");
        sb.append("  reason: ").append(reason).append("\n");
        sb.append("  youthOffender: ").append(youthOffender).append("\n");
        sb.append("  ethnicity: ").append(ethnicity).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  pncNumber: ").append(pncNumber).append("\n");
        sb.append("  croNumber: ").append(croNumber).append("\n");
        sb.append("  externalIdentifier: ").append(externalIdentifier).append("\n");
        sb.append("  externalIdentifierType: ").append(externalIdentifierType).append("\n");
        sb.append("  correlationId: ").append(correlationId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
