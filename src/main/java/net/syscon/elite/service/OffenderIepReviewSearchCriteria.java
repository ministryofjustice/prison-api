package net.syscon.elite.service;

import lombok.Builder;
import lombok.Getter;
import net.syscon.elite.api.support.PageRequest;

@Getter
@Builder
public class OffenderIepReviewSearchCriteria {
    private final String agencyId;
    private final String iepLevel;
    private final String location;
    private final PageRequest pageRequest;
}
