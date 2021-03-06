package uk.gov.justice.hmpps.prison.service;

import lombok.Builder;
import lombok.Getter;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;

@Getter
@Builder
public class OffenderIepReviewSearchCriteria {
    private final String agencyId;
    private final String iepLevel;
    private final String location;
    private final PageRequest pageRequest;
}
