package uk.gov.justice.hmpps.prison.repository;

import lombok.val;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationCharge;
import uk.gov.justice.hmpps.prison.api.model.adjudications.OffenderAdjudicationHearing;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.service.AdjudicationSearchCriteria;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AdjudicationsRepositoryTest {

    private static final Adjudication EARLIEST_ADJUDICATION = new Adjudication(-3, LocalDateTime.of(2019, 8, 25, 0, 3), -3, "MDI", 1L, List.of(
        new AdjudicationCharge(
            "5139/4",
            "51:2D",
            "Detains any person against his will - detention against will of staff (not prison offr)",
            "PROVED")));
    private static final Adjudication MIDDLE_ADJUDICATION = new Adjudication(-3001, LocalDateTime.of(2019, 9, 25, 0, 4), -3001, "LEI", 2L, List.of(
        new AdjudicationCharge(
            "5139/8",
            "51:8D",
            "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release",
            null)));

    private static final Adjudication LATEST_ADJUDICATION = new Adjudication(-3002, LocalDateTime.of(2019, 10, 25, 0, 5), -3002, "BXI", 3L, List.of(
        new AdjudicationCharge(
            "5139/9",
            "51:8D",
            "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release",
            null)));

    @Autowired
    private AdjudicationsRepository repository;

    @Test
    public void findAdjudicationOffences() {

        var offences = repository.findAdjudicationOffences("A1181GG");
        assertThat(offences).extracting("id", "code", "description").containsExactly(
            tuple("85", "51:2D", "Detains any person against his will - detention against will of staff (not prison offr)"),
            tuple("86", "51:8D", "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release")
        );

        offences = repository.findAdjudicationOffences("A1181HH");
        assertThat(offences).extracting("id", "code", "description").containsExactly(
            tuple("84", "51:2C", "Detains any person against his will - detention against will of prison officer grade"),
            tuple("85", "51:2D", "Detains any person against his will - detention against will of staff (not prison offr)")
        );
    }

    @Test
    public void findAdjudicationLocations() {

        var locations = repository.findAdjudicationAgencies("A1181GG");
        assertThat(locations).extracting("agencyId", "description", "agencyType").containsExactlyInAnyOrder(
            tuple("LEI", "LEEDS", "INST"),
            tuple("MDI", "MOORLAND", "INST"),
            tuple("BXI", "BRIXTON", "INST")
        );

        locations = repository.findAdjudicationAgencies("A1181HH");
        assertThat(locations).extracting("agencyId", "description", "agencyType").containsExactlyInAnyOrder(
            tuple("LEI", "LEEDS", "INST"),
            tuple("MDI", "MOORLAND", "INST")
        );
    }

    @Test
    public void retrieveAdjudicationsForOffender() {
        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1181GG")
            .pageRequest(new PageRequest(0L, 10L))
            .build());

        assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
    }

    @Sql(scripts = {"/sql/adjudicationHistorySort_init.sql"},
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Sql(scripts = {"/sql/adjudicationHistorySort_clean.sql"},
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Test
    public void adjudicationsHistorySortTest() {
        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1181GG")
            .pageRequest(new PageRequest(0L, 10L))
            .build());

        assertThat(results.getItems().stream().filter(f -> f.getAdjudicationNumber() == -3001L).findFirst().get().getAdjudicationCharges().get(0).getFindingCode()).isEqualTo("PROVED");
    }

    @Sql(scripts = {"/sql/incident_no_suspect.sql"},
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Sql(scripts = {"/sql/incident_no_suspect_cleanup.sql"},
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Test
    public void adjudicationHistoryContainsAdjudicationWithoutSuspectCode(){
        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1183SH")
            .pageRequest(new PageRequest(0L, 10L))
            .build());

        assertThat(results.getItems().stream().filter(f -> f.getAdjudicationNumber() == -3003)).isNotEmpty();
    }

    @Test
    public void filterByStartDate() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1181GG")
            .startDate(MIDDLE_ADJUDICATION.getReportTime().plusDays(1).toLocalDate())
            .pageRequest(new PageRequest(0L, 10L))
            .build());

        assertThat(results.getItems()).containsExactlyInAnyOrder(LATEST_ADJUDICATION);
    }

    @Test
    public void filterByEndDate() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1181GG")
            .endDate(MIDDLE_ADJUDICATION.getReportTime().minusDays(1).toLocalDate())
            .pageRequest(new PageRequest(0L, 10L))
            .build());

        assertThat(results.getItems()).containsExactly(EARLIEST_ADJUDICATION);
    }

    @Test
    public void filterByOffence() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1181GG")
            .offenceId("86")
            .pageRequest(new PageRequest(0L, 10L))
            .build());

        assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION);
    }

    @Test
    public void filterByLocation() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1181GG")
            .agencyId(EARLIEST_ADJUDICATION.getAgencyId())
            .pageRequest(new PageRequest(0L, 10L))
            .build());

        assertThat(results.getItems()).containsExactly(EARLIEST_ADJUDICATION);
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
        return assertThat(repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1181GG")
            .pageRequest(pageRequest)
            .build()).getItems());
    }

    @Test
    public void anotherInmateHasAnAdjudicationForSameIncident() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
            .offenderNumber("A1181HH")
            .pageRequest(new PageRequest(1L, 1L))
            .build());

        assertThat(results.getItems()).containsExactly(
            new Adjudication(-2, LocalDateTime.of(2017, 2, 23, 0, 1), -1, "LEI", 2L, List.of(
                new AdjudicationCharge(
                    "5139/3",
                    "51:2C",
                    "Detains any person against his will - detention against will of prison officer grade",
                    "NOT_PROVEN"))));
    }

    @Test
    public void findOffenderAdjudicationHearings() {
        val results = repository.findOffenderAdjudicationHearings(
            "LEI",
            LocalDate.of(2015, 1, 2),
            LocalDate.of(2015, 1, 3),
            Set.of("A1181HH"));

        assertThat(results).containsExactlyInAnyOrder(
            new OffenderAdjudicationHearing(
                "LEI",
                "A1181HH",
                -1,
                "Governor's Hearing Adult",
                LocalDateTime.of(2015, 1, 2, 14, 0),
                -1000,
                "LEI-AABCW-1",
                "SCH"
            ),
            new OffenderAdjudicationHearing(
                "LEI",
                "A1181HH",
                -2,
                "Governor's Hearing Adult",
                LocalDateTime.of(2015, 1, 2, 14, 0),
                -1001,
                "LEI-A-1-1001",
                "SCH")
        );
    }
}
