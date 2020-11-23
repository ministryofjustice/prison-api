package uk.gov.justice.hmpps.prison.api.model.adjudications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.web.config.AppBeanConfiguration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = AppBeanConfiguration.class)
public class AdjudicationDetailTest {

    @Autowired
    private JacksonTester<AdjudicationDetail> json;

    @Test
    public void checkSerialization() throws Exception {

        AdjudicationDetail detail = AdjudicationDetail.builder()
                .adjudicationNumber(-7L)
                .incidentTime(LocalDateTime.of(1999, 6, 25, 0, 0))
                .establishment("Moorland (HMP & YOI)")
                .interiorLocation("MDI-1-1-001")
                .incidentDetails("mKSouDOCmKSouDO")
                .reportNumber(-4L)
                .reportType("Miscellaneous")
                .reporterFirstName("Jo")
                .reporterLastName("O'brien")
                .reportTime(LocalDateTime.of(2019, 8, 25, 0, 3))
                .hearing(
                        Hearing.builder()
                                .oicHearingId(-1L)
                                .hearingType("Governor's Hearing Adult")
                                .hearingTime(LocalDateTime.of(2015, 1, 2, 14, 0))
                                .location("LEI-AABCW-1")
                                .heardByFirstName("Test")
                                .heardByLastName("User")
                                .otherRepresentatives("Other folk")
                                .comment("A Comment")
                                .result(HearingResult.builder()
                                        .oicOffenceCode("51:2D")
                                        .offenceType("Prison Rule 51")
                                        .offenceDescription("Detains any person against his will - detention against will of staff (not prison offr)")
                                        .plea("Unfit to Plea or Attend")
                                        .finding("Charge Proved")
                                        .oicHearingId(-1)
                                        .resultSeq(1L)
                                        .sanction(Sanction.builder()
                                                .sanctionType("Stoppage of Earnings (%)")
                                                .sanctionDays(21L)
                                                .sanctionMonths(null)
                                                .compensationAmount(50L)
                                                .effectiveDate(LocalDateTime.of(2017, 11, 7, 0, 0))
                                                .status("Immediate")
                                                .statusDate(LocalDateTime.of(2017, 11, 8, 0, 0))
                                                .comment(null)
                                                .sanctionSeq(1L)
                                                .consecutiveSanctionSeq(1L)
                                                .oicHearingId(-1)
                                                .resultSeq(1L)
                                                .build())
                                        .build())
                                .build())
                .hearing(Hearing.builder()
                        .oicHearingId(-2L)
                        .hearingType("Governor's Hearing Adult")
                        .hearingTime(LocalDateTime.of(2015, 1, 2, 14, 0))
                        .location("LEI-A-1-1001")
                        .heardByFirstName("CA")
                        .heardByLastName("User")
                        .otherRepresentatives("Some Other folk")
                        .comment("B Comment")
                        .result(HearingResult.builder()
                                .oicOffenceCode("51:2C")
                                .offenceType("Prison Rule 51")
                                .offenceDescription("Detains any person against his will - detention against will of prison officer grade")
                                .plea("Not guilty")
                                .finding("Charge Proved")
                                .oicHearingId(-2L)
                                .resultSeq(1L)
                                .sanction(Sanction.builder()
                                        .sanctionType("Cellular Confinement")
                                        .sanctionDays(7L)
                                        .sanctionMonths(1L)
                                        .compensationAmount(null)
                                        .effectiveDate(LocalDateTime.of(2017, 11, 07, 0, 0))
                                        .status("Immediate")
                                        .statusDate(null)
                                        .comment(null)
                                        .sanctionSeq(2L)
                                        .consecutiveSanctionSeq(2L)
                                        .oicHearingId(-2L)
                                        .resultSeq(1L)
                                        .build())
                                .build())
                        .result(HearingResult.builder()
                                .oicOffenceCode("51:1J")
                                .offenceType("Prison Rule 51")
                                .offenceDescription("Commits any assault - assault on prison officer")
                                .plea("Not guilty")
                                .finding("Charge Proved")
                                .oicHearingId(-2L)
                                .resultSeq(2L)
                                .sanction(
                                        Sanction.builder()
                                                .sanctionType("Forfeiture of Privileges")
                                                .sanctionDays(7L)
                                                .sanctionMonths(null)
                                                .compensationAmount(null)
                                                .effectiveDate(LocalDateTime.of(2017, 11, 8, 0, 0))
                                                .status("Immediate")
                                                .statusDate(null)
                                                .comment("LOTV")
                                                .sanctionSeq(3L)
                                                .consecutiveSanctionSeq(1L)
                                                .oicHearingId(-2)
                                                .resultSeq(2L)
                                                .build())
                                .build())
                        .build())
                .build();

        assertThat(json.write(detail)).isEqualToJson("adjudication-details.json");
    }

}
