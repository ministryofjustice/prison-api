package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

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
@ToString
public class PrisonerDetail {

    @ApiModelProperty(required = true, value = "The prisoner's unique offender number (aka NOMS Number in the UK).", example="A0000AA")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(value = "The prisoner's title.", example="Earl")
    private String title;

    @ApiModelProperty(value = "The prisoner's name suffix.", example="Mac")
    private String suffix;

    @ApiModelProperty(required = true, value = "The prisoner's first name.", example="Thorfinn")
    @NotBlank
    private String firstName;

    @ApiModelProperty(value = "The prisoner's middle name(s).", example="Skull-splitter")
    private String middleNames;

    @ApiModelProperty(required = true, value = "The prisoner's last name.", example="Torf-Einarsson")
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "The prisoner's date of birth (in YYYY-MM-DD format).", example="1960-02-29")
    @NotNull
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "The prisoner's gender.", example="Female")
    @NotBlank
    private String gender;

    @ApiModelProperty(required = true, value = "The prisoner's gender code.", example="F")
    @NotBlank
    private String sexCode;

    @ApiModelProperty(value = "The prisoner's nationality.", example="Scottish")
    private String nationalities;

    @ApiModelProperty(required = true, value = "Flag (Y or N) to indicate if prisoner is currently in prison.", example="N")
    @NotBlank
    private String currentlyInPrison;

    @ApiModelProperty(value = "ID of prisoner's latest booking.", example="1")
    private Long latestBookingId;

    @ApiModelProperty(value = "Latest location ID of a prisoner (if in prison).", example="WRI")
    private String latestLocationId;

    @ApiModelProperty(value = "Name of the prison where the prisoner resides (if in prison).", example="Whitemoor (HMP)")
    private String latestLocation;

    @ApiModelProperty(value = "Name of the location where the prisoner resides (if in prison)", example="WRI-B-3-018")
    private String internalLocation;

    @ApiModelProperty(value = "The prisoner's PNC (Police National Computer) number.", example="01/000000A")
    private String pncNumber;

    @ApiModelProperty(value = "The prisoner's CRO (Criminal Records Office) number.", example="01/0001/01A")
    private String croNumber;

    @ApiModelProperty(value = "The prisoner's ethnicity.", example="White: British")
    private String ethnicity;

    @ApiModelProperty(value = "The prisoner's ethnicity code.", example="W1")
    private String ethnicityCode;

    @ApiModelProperty(value = "The prisoner's country of birth.", example="Norway")
    private String birthCountry;

    @ApiModelProperty(value = "The prisoner's religion.", example="Pagan")
    private String religion;

    @ApiModelProperty(value = "The prisoner's religion code.", example="PAG")
    private String religionCode;

    @ApiModelProperty(value = "Status code of prisoner's latest conviction.", example="Convicted", allowableValues = "Convicted,Remand")
    private String convictedStatus;

    @ApiModelProperty(value = "Legal Status", example="REMAND")
    private LegalStatus legalStatus;

    @JsonIgnore
    private String bandCode;

    @ApiModelProperty(value = "The prisoner's imprisonment status.", example="LIFE")
    private String imprisonmentStatus;

    @ApiModelProperty(value = "The prisoner's imprisonment status description.", example="Service Life Imprisonment")
    private String imprisonmentStatusDesc;

    @ApiModelProperty(value = "Date prisoner was received into the prison.", example="1980-01-01")
    private LocalDate receptionDate;

    @ApiModelProperty(value = "The prisoner's marital status.", example="Single")
    private String maritalStatus;

    @ApiModelProperty(required = true, value = "The prisoner's current working first name.", example="Thorfinn")
    @NotBlank
    private String currentWorkingFirstName;

    @ApiModelProperty(required = true, value = "The prisoner's current working last name.", example="Torf-Einarsson")
    @NotBlank
    private String currentWorkingLastName;

    @ApiModelProperty(required = true, value = "The prisoner's current working date of birth (in YYYY-MM-DD format).", example="1960-02-29")
    @NotNull
    private LocalDate currentWorkingBirthDate;

    public void deriveLegalDetails() {
        legalStatus = uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcLegalStatus(bandCode, imprisonmentStatus);
        convictedStatus = uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcConvictedStatus(bandCode);
    }

}
