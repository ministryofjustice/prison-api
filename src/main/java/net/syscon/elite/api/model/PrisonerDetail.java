package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Prisoner Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Prisoner Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class PrisonerDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @ApiModelProperty(required = true, value = "The prisoner's unique offender number (aka NOMS Number in the UK).")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(value = "The prisoner's title.")
    private String title;

    @ApiModelProperty(value = "The prisoner's name suffix.")
    private String suffix;

    @ApiModelProperty(required = true, value = "The prisoner's first name.")
    @NotBlank
    private String firstName;

    @ApiModelProperty(value = "The prisoner's middle name(s).")
    private String middleNames;

    @ApiModelProperty(required = true, value = "The prisoner's last name.")
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "The prisoner's date of birth (in YYYY-MM-DD format).")
    @NotNull
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "The prisoner's gender.")
    @NotBlank
    private String gender;

    @ApiModelProperty(required = true, value = "The prisoner's gender code.")
    @NotBlank
    private String sexCode;

    @ApiModelProperty(value = "The prisoner's nationality.")
    private String nationalities;

    @ApiModelProperty(required = true, value = "Flag (Y or N) to indicate if prisoner is currently in prison.")
    @NotBlank
    private String currentlyInPrison;

    @ApiModelProperty(value = "ID of prisoner's latest booking.")
    private Long latestBookingId;

    @ApiModelProperty(value = "Latest location ID of a prisoner (if in prison).")
    private String latestLocationId;

    @ApiModelProperty(value = "Name of the prison where the prisoner resides (if in prison).")
    private String latestLocation;

    @ApiModelProperty(value = "Name of the location where the prisoner resides (if in prison)")
    private String internalLocation;

    @ApiModelProperty(value = "The prisoner's PNC (Police National Computer) number.")
    private String pncNumber;

    @ApiModelProperty(value = "The prisoner's CRO (Criminal Records Office) number.")
    private String croNumber;

    @ApiModelProperty(value = "The prisoner's ethnicity.", example="White: British")
    private String ethnicity;

    @ApiModelProperty(value = "The prisoner's ethnicity code.", example="W1")
    private String ethnicityCode;

    @ApiModelProperty(value = "The prisoner's country of birth.")
    private String birthCountry;

    @ApiModelProperty(value = "The prisoner's religion.", example="Church of England")
    private String religion;

    @ApiModelProperty(value = "The prisoner's religion code.", example="CHRCE")
    private String religionCode;

    @ApiModelProperty(value = "Status code of prisoner's latest conviction.")
    private String convictedStatus;

    @ApiModelProperty(value = "The prisoner's imprisonment status.")
    private String imprisonmentStatus;

    @ApiModelProperty(value = "The prisoner's imprisonment status description.")
    private String imprisonmentStatusDesc;

    @ApiModelProperty(value = "Date prisoner was received into the prison.")
    private LocalDate receptionDate;

    @ApiModelProperty(value = "The prisoner's marital status.")
    private String maritalStatus;

    @ApiModelProperty(required = true, value = "The prisoner's current working first name.")
    @NotBlank
    private String currentWorkingFirstName;

    @ApiModelProperty(required = true, value = "The prisoner's current working last name.")
    @NotBlank
    private String currentWorkingLastName;

    @ApiModelProperty(required = true, value = "The prisoner's current working date of birth (in YYYY-MM-DD format).")
    @NotNull
    private LocalDate currentWorkingBirthDate;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public String toString() {

        return new StringBuilder()
                .append("class PrisonerDetail {\n")
                .append("  offenderNo: ").append(offenderNo).append("\n")
                .append("  title: ").append(title).append("\n")
                .append("  suffix: ").append(suffix).append("\n")
                .append("  firstName: ").append(firstName).append("\n")
                .append("  middleNames: ").append(middleNames).append("\n")
                .append("  lastName: ").append(lastName).append("\n")
                .append("  dateOfBirth: ").append(dateOfBirth).append("\n")
                .append("  gender: ").append(gender).append("\n")
                .append("  nationalities: ").append(nationalities).append("\n")
                .append("  currentlyInPrison: ").append(currentlyInPrison).append("\n")
                .append("  latestBookingId: ").append(latestBookingId).append("\n")
                .append("  latestLocationId: ").append(latestLocationId).append("\n")
                .append("  latestLocation: ").append(latestLocation).append("\n")
                .append("  internalLocation: ").append(internalLocation).append("\n")
                .append("  pncNumber: ").append(pncNumber).append("\n")
                .append("  croNumber: ").append(croNumber).append("\n")
                .append("  ethnicity: ").append(ethnicity).append("\n")
                .append("  birthCountry: ").append(birthCountry).append("\n")
                .append("  religion: ").append(religion).append("\n")
                .append("  convictedStatus: ").append(convictedStatus).append("\n")
                .append("  imprisonmentStatus: ").append(imprisonmentStatus).append("\n")
                .append("  receptionDate: ").append(receptionDate).append("\n")
                .append("  maritalStatus: ").append(maritalStatus).append("\n")
                .append("  currentWorkingFirstName: ").append(currentWorkingFirstName).append("\n")
                .append("  currentWorkingLastName: ").append(currentWorkingLastName).append("\n")
                .append("  currentWorkingBirthDate: ").append(currentWorkingBirthDate).append("\n")
                .append("}\n")
                .toString();
    }
}
