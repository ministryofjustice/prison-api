package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Prisoner Details
 **/
@SuppressWarnings("unused")
@Schema(description = "Prisoner Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@EqualsAndHashCode
@Data
@ToString
public class PrisonerDetail {

    @Schema(required = true, description = "The prisoner's unique offender number (aka NOMS Number in the UK).", example="A0000AA")
    @NotBlank
    private String offenderNo;

    @Schema(description = "The prisoner's title.", example="Earl")
    private String title;

    @Schema(description = "The prisoner's name suffix.", example="Mac")
    private String suffix;

    @Schema(required = true, description = "The prisoner's first name.", example="Thorfinn")
    @NotBlank
    private String firstName;

    @Schema(description = "The prisoner's middle name(s).", example="Skull-splitter")
    private String middleNames;

    @Schema(required = true, description = "The prisoner's last name.", example="Torf-Einarsson")
    @NotBlank
    private String lastName;

    @Schema(required = true, description = "The prisoner's date of birth (in YYYY-MM-DD format).", example="1960-02-29")
    @NotNull
    private LocalDate dateOfBirth;

    @Schema(required = true, description = "The prisoner's gender.", example="Female")
    @NotBlank
    private String gender;

    @Schema(required = true, description = "The prisoner's gender code.", example="F")
    @NotBlank
    private String sexCode;

    @Schema(description = "The prisoner's nationality.", example="Scottish")
    private String nationalities;

    @Schema(required = true, description = "Flag (Y or N) to indicate if prisoner is currently in prison.", example="N")
    @NotBlank
    private String currentlyInPrison;

    @Schema(description = "ID of prisoner's latest booking.", example="1")
    private Long latestBookingId;

    @Schema(description = "Latest location ID of a prisoner (if in prison).", example="WRI")
    private String latestLocationId;

    @Schema(description = "Name of the prison where the prisoner resides (if in prison).", example="Whitemoor (HMP)")
    private String latestLocation;

    @Schema(description = "Name of the location where the prisoner resides (if in prison)", example="WRI-B-3-018")
    private String internalLocation;

    @Schema(description = "The prisoner's PNC (Police National Computer) number.", example="01/000000A")
    private String pncNumber;

    @Schema(description = "The prisoner's CRO (Criminal Records Office) number.", example="01/0001/01A")
    private String croNumber;

    @Schema(description = "The prisoner's ethnicity.", example="White: British")
    private String ethnicity;

    @Schema(description = "The prisoner's ethnicity code.", example="W1")
    private String ethnicityCode;

    @Schema(description = "The prisoner's country of birth.", example="Norway")
    private String birthCountry;

    @Schema(description = "The prisoner's religion.", example="Pagan")
    private String religion;

    @Schema(description = "The prisoner's religion code.", example="PAG")
    private String religionCode;

    @Schema(description = "Status code of prisoner's latest conviction.", example="Convicted", allowableValues = {"Convicted","Remand"})
    private String convictedStatus;

    @Schema(description = "Legal Status", example="REMAND")
    private LegalStatus legalStatus;

    @JsonIgnore
    private String bandCode;

    @Schema(description = "The prisoner's imprisonment status.", example="LIFE")
    private String imprisonmentStatus;

    @Schema(description = "The prisoner's imprisonment status description.", example="Service Life Imprisonment")
    private String imprisonmentStatusDesc;

    @Schema(description = "Date prisoner was received into the prison.", example="1980-01-01")
    private LocalDate receptionDate;

    @Schema(description = "The prisoner's marital status.", example="Single")
    private String maritalStatus;

    @Schema(required = true, description = "The prisoner's current working first name.", example="Thorfinn")
    @NotBlank
    private String currentWorkingFirstName;

    @Schema(required = true, description = "The prisoner's current working last name.", example="Torf-Einarsson")
    @NotBlank
    private String currentWorkingLastName;

    @Schema(required = true, description = "The prisoner's current working date of birth (in YYYY-MM-DD format).", example="1960-02-29")
    @NotNull
    private LocalDate currentWorkingBirthDate;

    public PrisonerDetail(@NotBlank String offenderNo, String title, String suffix, @NotBlank String firstName, String middleNames, @NotBlank String lastName, @NotNull LocalDate dateOfBirth, @NotBlank String gender, @NotBlank String sexCode, String nationalities, @NotBlank String currentlyInPrison, Long latestBookingId, String latestLocationId, String latestLocation, String internalLocation, String pncNumber, String croNumber, String ethnicity, String ethnicityCode, String birthCountry, String religion, String religionCode, String convictedStatus, LegalStatus legalStatus, String bandCode, String imprisonmentStatus, String imprisonmentStatusDesc, LocalDate receptionDate, String maritalStatus, @NotBlank String currentWorkingFirstName, @NotBlank String currentWorkingLastName, @NotNull LocalDate currentWorkingBirthDate) {
        this.offenderNo = offenderNo;
        this.title = title;
        this.suffix = suffix;
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.sexCode = sexCode;
        this.nationalities = nationalities;
        this.currentlyInPrison = currentlyInPrison;
        this.latestBookingId = latestBookingId;
        this.latestLocationId = latestLocationId;
        this.latestLocation = latestLocation;
        this.internalLocation = internalLocation;
        this.pncNumber = pncNumber;
        this.croNumber = croNumber;
        this.ethnicity = ethnicity;
        this.ethnicityCode = ethnicityCode;
        this.birthCountry = birthCountry;
        this.religion = religion;
        this.religionCode = religionCode;
        this.convictedStatus = convictedStatus;
        this.legalStatus = legalStatus;
        this.bandCode = bandCode;
        this.imprisonmentStatus = imprisonmentStatus;
        this.imprisonmentStatusDesc = imprisonmentStatusDesc;
        this.receptionDate = receptionDate;
        this.maritalStatus = maritalStatus;
        this.currentWorkingFirstName = currentWorkingFirstName;
        this.currentWorkingLastName = currentWorkingLastName;
        this.currentWorkingBirthDate = currentWorkingBirthDate;
    }

    public PrisonerDetail() {
    }

    public PrisonerDetail deriveLegalDetails() {
        legalStatus = uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcLegalStatus(bandCode, imprisonmentStatus);
        convictedStatus = uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcConvictedStatus(bandCode);
        return this;
    }
}
