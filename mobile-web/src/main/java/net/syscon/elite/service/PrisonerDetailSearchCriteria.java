package net.syscon.elite.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class PrisonerDetailSearchCriteria {
    private String offenderNo;
    private String firstName;
    private String middleNames;
    private String lastName;
    private String pncNumber;
    private String croNumber;
    private LocalDate dob;
    private LocalDate dobFrom;
    private LocalDate dobTo;
    private boolean partialNameMatch;
}
