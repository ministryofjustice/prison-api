package uk.gov.justice.hmpps.prison.service;


import lombok.Builder;
import lombok.Getter;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;

import java.time.LocalDate;

@Getter
@Builder
public class AdjudicationSearchCriteria {

    private final String offenderNumber;
    private final String offenceId;
    private final String agencyId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String findingCode;
    private final PageRequest pageRequest;
}
