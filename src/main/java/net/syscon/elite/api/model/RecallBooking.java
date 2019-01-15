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
 * Recall Offender Booking
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Recall Offender Booking")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RecallBooking {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @Length(max=10) @Pattern(regexp="^[A-Z]\\d{4}[A-Z]{2}$") @NotBlank
    private String offenderNo;

    @Length(max=35) @NotBlank
    private String lastName;

    @Length(max=35) @NotBlank
    private String firstName;

    @Pattern(regexp="^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$") @NotBlank
    private String dateOfBirth;

    @Length(max=12) @NotBlank
    private String gender;

    @Pattern(regexp="^[BY]$") @NotBlank
    private String reason;

    private boolean youthOffender;

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
      * A unique offender number.
      */
    @ApiModelProperty(required = true, value = "A unique offender number.")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * The offender's first name.
      */
    @ApiModelProperty(required = true, value = "The offender's first name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
      * The offender's lsst name.
      */
    @ApiModelProperty(required = true, value = "The offender's lsst name.")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
      * The offender's date of birth. Must be specified in YYYY-MM-DD format.
      */
    @ApiModelProperty(required = true, value = "The offender's date of birth. Must be specified in YYYY-MM-DD format.")
    @JsonProperty("dateOfBirth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
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

    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
      * A code representing the reason for the offender's recall. 'B' = Recall from HDC. 'Y' = Recall from DTO.
      */
    @ApiModelProperty(required = true, value = "A code representing the reason for the offender's recall. 'B' = Recall from HDC. 'Y' = Recall from DTO.")
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
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

    public void setYouthOffender(boolean youthOffender) {
        this.youthOffender = youthOffender;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class RecallBooking {\n");
        
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  dateOfBirth: ").append(dateOfBirth).append("\n");
        sb.append("  gender: ").append(gender).append("\n");
        sb.append("  reason: ").append(reason).append("\n");
        sb.append("  youthOffender: ").append(youthOffender).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
