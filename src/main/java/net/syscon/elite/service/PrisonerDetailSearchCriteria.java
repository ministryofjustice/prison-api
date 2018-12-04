package net.syscon.elite.service;

import lombok.*;
import lombok.experimental.Wither;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@ToString
@Wither
@EqualsAndHashCode
public class PrisonerDetailSearchCriteria {
    private boolean includeAliases;
    private String offenderNo;
    private String firstName;
    private String sexCode;
    private String middleNames;
    private String lastName;
    private String latestLocationId;
    private String pncNumber;
    private String croNumber;
    private LocalDate dob;
    private LocalDate dobFrom;
    private LocalDate dobTo;
    private int maxYearsRange;
    private boolean partialNameMatch;
    private boolean anyMatch;
    private boolean prioritisedMatch;
}
