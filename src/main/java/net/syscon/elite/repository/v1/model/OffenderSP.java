package net.syscon.elite.repository.v1.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

@Data
@ToString
@Builder
public class OffenderSP {

    private String firstName;
    private String middleNames;
    private String lastName;
    private LocalDate birthDate;
    private String sexCode;
    private String sexDesc;
    private String title;
    private String suffix;
    private String pncNumber;
    private String croNumber;
    private String imprisonmentStatus;
    private String imprisonmentStatusDesc;
    private String convictedStatus;
    private ResultSet aliases;
    private String nationalities;
    private String religionCode;
    private String religionDesc;
    private String dietCode;
    private String dietDesc;
    private String ethnicityCode;
    private String ethnicityDesc;
    private String iepLevel;
    private String iepLevelDesc;
    private String spokenLanguageCode;
    private String spokenLanguageDesc;
    private String interpreterRequestedFlag;
    private String csraCode;
    private String csraDescription;
    private String catLevel;
    private String catLevelDesc;

    private List<AliasSP> offenderAliases;


}
