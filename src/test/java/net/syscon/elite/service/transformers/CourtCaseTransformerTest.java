package net.syscon.elite.service.transformers;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.CourtCase;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.CaseStatus;
import net.syscon.elite.repository.jpa.model.LegalCaseType;
import net.syscon.elite.repository.jpa.model.OffenderCourtCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class CourtCaseTransformerTest {

    private static final LegalCaseType LEGAL_CASE_TYPE = new LegalCaseType("A", "Adult");

    private static final CaseStatus CASE_STATUS = new CaseStatus("A", "Active");

    private OffenderCourtCase offenderCourtCase;

    private AgencyLocation agencyLocation;

    @BeforeEach
    void setup() {
        agencyLocation = AgencyLocation.builder()
                .id("MDI")
                .activeFlag(ActiveFlag.Y)
                .type("CRT")
                .description("Moorland")
                .build();

        offenderCourtCase = OffenderCourtCase.builder()
                .id(-1L)
                .caseSeq(-2L)
                .beginDate(LocalDate.EPOCH)
                .agencyLocation(agencyLocation)
                .legalCaseType(LEGAL_CASE_TYPE)
                .caseInfoPrefix("CIP")
                .caseInfoNumber("CIN20177010")
                .caseStatus(CASE_STATUS)
                .build();
    }

    @Test
    void transform() {
        var transformed = CourtCaseTransformer.transform(offenderCourtCase);

        assertThat(transformed)
                .extracting(
                        CourtCase::getId,
                        CourtCase::getCaseSeq,
                        CourtCase::getBeginDate,
                        CourtCase::getCaseInfoPrefix,
                        CourtCase::getCaseInfoNumber)
                .containsOnly(
                        -1L,
                        -2L,
                        LocalDate.EPOCH,
                        "CIP",
                        "CIN20177010");

        assertThat(transformed.getAgency())
                .extracting(
                        Agency::getAgencyId,
                        Agency::getAgencyType,
                        Agency::getDescription)
                .containsOnly(
                        agencyLocation.getId(),
                        agencyLocation.getType(),
                        agencyLocation.getDescription()
                );

        assertThat(transformed.getCaseStatus()).isEqualToIgnoringCase(CASE_STATUS.getDescription());

        assertThat(transformed.getCaseType()).isEqualToIgnoringCase(LEGAL_CASE_TYPE.getDescription());
    }
}
