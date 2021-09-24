package uk.gov.justice.hmpps.prison.service.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.CourtHearing;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;

import java.util.List;

import static java.time.LocalDate.EPOCH;
import static java.time.LocalTime.MIDNIGHT;
import static org.assertj.core.api.Assertions.assertThat;

public class CourtCaseTransformerTest {

    private static final LegalCaseType LEGAL_CASE_TYPE = new LegalCaseType("A", "Adult");

    private static final CaseStatus CASE_STATUS = new CaseStatus("A", "Active");

    private OffenderCourtCase offenderCourtCase;

    private AgencyLocation courtLocation;

    @BeforeEach
    void setup() {
        OffenderBooking booking = OffenderBooking.builder()
                .bookingId(-1L)
                .location(AgencyLocation.builder()
                        .id("LEI")
                        .active(true)
                        .type(new AgencyLocationType("INST"))
                        .description("Leeds")
                        .build()).build();

        courtLocation = AgencyLocation.builder()
                .id("MDI")
                .active(true)
                .type(AgencyLocationType.COURT_TYPE)
                .description("Moorland")
                .build();

        offenderCourtCase = OffenderCourtCase.builder()
                .id(-1L)
                .caseSeq(-2L)
                .beginDate(EPOCH)
                .agencyLocation(courtLocation)
                .legalCaseType(LEGAL_CASE_TYPE)
                .caseInfoPrefix("CIP")
                .caseInfoNumber("CIN20177010")
                .caseStatus(CASE_STATUS)
                .courtEvents(List.of(CourtEvent.builder()
                        .id(-1L)
                        .offenderBooking(booking)
                        .eventDate(EPOCH)
                        .startTime(EPOCH.atStartOfDay())
                        .courtLocation(courtLocation)
                        .build()))
                .offenderBooking(booking)
                .build();
    }

    @Test
    void transform() {
        var transformed = CourtCaseTransformer.transform(offenderCourtCase);

        assertThat(transformed).isEqualTo(
                CourtCase.builder()
                        .id(-1L)
                        .caseSeq(-2L)
                        .beginDate(EPOCH)
                        .caseInfoPrefix("CIP")
                        .caseInfoNumber("CIN20177010")
                        .agency(Agency.builder()
                                .agencyId(courtLocation.getId())
                                .agencyType(courtLocation.getType().getCode())
                                .description(courtLocation.getDescription())
                                .active(true)
                                .build())
                        .caseStatus(CASE_STATUS.getDescription())
                        .caseType(LEGAL_CASE_TYPE.getDescription())
                        .courtHearings(List.of(CourtHearing.builder()
                                .id(-1L)
                                .dateTime(EPOCH.atTime(MIDNIGHT))
                                .location(Agency.builder()
                                        .agencyId(courtLocation.getId())
                                        .description(courtLocation.getDescription())
                                        .agencyType("CRT")
                                        .active(true)
                                        .build())
                                .build()))
                        .build());
    }
}
