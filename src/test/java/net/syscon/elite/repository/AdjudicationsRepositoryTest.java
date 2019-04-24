package net.syscon.elite.repository;

import net.syscon.elite.api.model.Adjudication;
import net.syscon.elite.api.model.AdjudicationCharge;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.AdjudicationSearchCriteria;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.assertj.core.api.ListAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AdjudicationsRepositoryTest {

    private static final Adjudication EARLIEST_ADJUDICATION = new Adjudication(-1, LocalDateTime.of(2017, 2, 23, 0, 0), -1, "LEI", 1L, List.of(
            new AdjudicationCharge(
                    "5139/1",
                    "Commits any assault - assault on non prison officer member of staff",
                    "51:1N",
                    null),
            new AdjudicationCharge(
                    "5139/2",
                    "Detains any person against his will - detention against will -non offr/staff/inmate",
                    "51:2B",
                    "DISMISSED")));
    private static final Adjudication MIDDLE_ADJUDICATION = new Adjudication(-5, LocalDateTime.of(2019, 1, 25, 0, 0), -2, "LEI", 1L, List.of(
            new AdjudicationCharge(
                    "5139/5",
                    "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release",
                    "51:8D",
                    "PROVED")));
    private static final Adjudication LATEST_ADJUDICATION = new Adjudication(-3, LocalDateTime.of(2019, 8, 25, 0, 0), -3, "MDI", 1L, List.of(
            new AdjudicationCharge(
                    "5139/4",
                    "Detains any person against his will - detention against will of staff (not prison offr)",
                    "51:2D",
                    "PROVED")));
    @Autowired
    private AdjudicationsRepository repository;

    @Test
    public void testGetDetailsMultiple() {

        final var awards = repository.findAwards(-3L);

        assertThat(awards).asList()
                .hasSize(2)
                .extracting("sanctionCode", "sanctionCodeDescription", "limit", "months", "days", "comment", "status", "statusDescription", "effectiveDate")
                .contains(tuple("FORFEIT", "Forfeiture of Privileges", null, null, 30, null, "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 8)),
                        tuple("STOP_PCT", "Stoppage of Earnings (%)", BigDecimal.valueOf(2020L, 2), 4, 5, "test comment", "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 9)));
    }

    @Test
    public void testGetDetailsInvalidBookingId() {
        final var awards = repository.findAwards(1001L);
        assertThat(awards.isEmpty()).isTrue();
    }

    @Test
    public void testGetDetailsMultiple2() {

        final var awards = repository.findAwards(-1L);

        assertThat(awards).asList()
                .hasSize(2)
                .extracting("sanctionCode", "sanctionCodeDescription", "limit", "months", "days", "comment", "status", "statusDescription", "effectiveDate")
                .contains(tuple("ADA", "Additional Days Added", null, null, null, null, "SUSPENDED", "Suspended", LocalDate.of(2016, 10, 17)),
                        tuple("CC", "Cellular Confinement", null, null, 15, null, "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 9)));
    }

    @Test
    public void retrieveAdjudicationsForOffender() {

        final var results = repository.findAdjudicationsForOffender(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
    }

    @Test
    public void filterByStartDate() {

        final var results = repository.findAdjudicationsForOffender(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .startDate(MIDDLE_ADJUDICATION.getReportTime().plusDays(1).toLocalDate())
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION);
    }

    @Test
    public void filterByEndDate() {

        final var results = repository.findAdjudicationsForOffender(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .endDate(MIDDLE_ADJUDICATION.getReportTime().minusDays(1).toLocalDate())
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(EARLIEST_ADJUDICATION);
    }

    @Test
    public void filterByOffence() {

        final var results = repository.findAdjudicationsForOffender(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .offenceId("86")
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(MIDDLE_ADJUDICATION);
    }

    @Test
    public void filterByLocation() {

        final var results = repository.findAdjudicationsForOffender(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .agencyId(LATEST_ADJUDICATION.getAgencyId())
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION);
    }

    @Test
    public void pagination() {

        resultsFor(new PageRequest(0L, null)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(1L, null)).containsExactly(MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(2L, null)).containsExactly(EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(3L, null)).isEmpty();

        resultsFor(new PageRequest(null, 0L)).isEmpty();
        resultsFor(new PageRequest(null, 1L)).containsExactly(LATEST_ADJUDICATION);
        resultsFor(new PageRequest(null, 2L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION);
        resultsFor(new PageRequest(null, 3L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(null, 4L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);

        resultsFor(new PageRequest(1L, 1L)).containsExactly(MIDDLE_ADJUDICATION);
        resultsFor(new PageRequest(1L, 2L)).containsExactly(MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(2L, 1L)).containsExactly(EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(2L, 2L)).containsExactly(EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(3L, 1L)).isEmpty();
    }

    private ListAssert<Adjudication> resultsFor(final PageRequest pageRequest) {
        return assertThat(repository.findAdjudicationsForOffender(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .pageRequest(pageRequest)
                .build()).getItems());
    }


    @Test
    public void anotherInmateHasAnAdjudicationForSameIncident() {

        final var results = repository.findAdjudicationsForOffender(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118HHH")
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(
                new Adjudication(-2, LocalDateTime.of(2017, 2, 23, 0, 0), -1, "LEI", 2L, List.of(
                        new AdjudicationCharge(
                                "5139/3",
                                "Detains any person against his will - detention against will of prison officer grade",
                                "51:2C",
                                "NOT_PROVED"))));
    }
}
