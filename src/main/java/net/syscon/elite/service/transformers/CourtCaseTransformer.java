package net.syscon.elite.service.transformers;

import net.syscon.elite.api.model.CourtCase;
import net.syscon.elite.repository.jpa.model.CaseStatus;
import net.syscon.elite.repository.jpa.model.LegalCaseType;
import net.syscon.elite.repository.jpa.model.OffenderCourtCase;

import java.util.Collection;
import java.util.Collections;
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
                .agency(AgencyTransformer.transform(courtCase.getAgencyLocation()))
                .caseType(courtCase.getLegalCaseType().map(LegalCaseType::getDescription).orElse(null))
                .caseInfoPrefix(courtCase.getCaseInfoPrefix())
                .caseInfoNumber(courtCase.getCaseInfoNumber())
                .caseStatus(courtCase.getCaseStatus().map(CaseStatus::getDescription).orElse(null))
                .courtEvents(CourtEventTransformer.transform(courtCase.getCourtEvents()))
                .build();
    }

    public static List<CourtCase> transform(final Collection<OffenderCourtCase> courtCases) {
        return courtCases.stream().map(CourtCaseTransformer::transform).collect(Collectors.toUnmodifiableList());
    }
}
