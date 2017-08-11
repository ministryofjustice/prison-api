package net.syscon.elite.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class PrisonerDetailSearchCriteria {
    private String firstName;
    private String middleNames;
    private String lastName;
    private String pncNumber;
    private String croNumber;
    private Date dob;
    private Date dobFrom;
    private Date dobTo;
}
