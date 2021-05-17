package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.CourtHearing;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple transform object to help reduce boiler plate mapping of entity {@link OffenderCourtCase} to model object {@link CourtCase}.
 */
public class CourtCaseTransformer {

    public static CourtCase transform(final OffenderCourtCase courtCase) {
        return CourtCase.builder()
                .id(courtCase.getId())
                .caseSeq(courtCase.getCaseSeq())
                .beginDate(courtCase.getBeginDate())
                .agency(AgencyTransformer.transform(courtCase.getAgencyLocation(), false))
                .caseType(courtCase.getLegalCaseType().map(LegalCaseType::getDescription).orElse(null))
                .caseInfoPrefix(courtCase.getCaseInfoPrefix())
                .caseInfoNumber(courtCase.getCaseInfoNumber())
                .caseStatus(courtCase.getCaseStatus().map(CaseStatus::getDescription).orElse(null))
                .courtHearings(courtHearingsFor(courtCase))
                .build();
    }

    private static List<CourtHearing> courtHearingsFor(final OffenderCourtCase courtCase) {
        return courtCase.getCourtEvents().stream()
                .map(ce -> CourtHearing.builder()
                        .id(ce.getId())
                        .location(AgencyTransformer.transform(ce.getCourtLocation(), false))
                        .dateTime(ce.getEventDateTime())
                        .build())
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<CourtCase> transform(final Collection<OffenderCourtCase> courtCases) {
        return courtCases.stream().map(CourtCaseTransformer::transform).collect(Collectors.toUnmodifiableList());
    }
}
