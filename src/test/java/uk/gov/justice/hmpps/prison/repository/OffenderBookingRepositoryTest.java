package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.IepLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AvailablePrisonIepLevelRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class, PersistenceConfigs.class})
@WithMockUser
@Slf4j
@DisplayName("OffenderBookingRepositoryTest with OffenderBookingFilter")
public class OffenderBookingRepositoryTest {

    @Autowired
    private OffenderBookingRepository repository;

    @Autowired
    private StaffUserAccountRepository staffUserAccountRepository;

    @Autowired
    private ReferenceCodeRepository<IepLevel> iepLevelReferenceCodeRepository;

    @Autowired
    private AvailablePrisonIepLevelRepository availablePrisonIepLevelRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("can find all for a booking")
    void canFindAllForABooking() {
        final var filter = OffenderBookingFilter
            .builder()
            .bookingSequence(1)
            .active(true)
            .build();

        final var pageOfBookings = repository.findAll(
            filter,
            PageRequest.of(0, 10, Sort.by("bookingId")));
        
        assertThat(pageOfBookings.getContent()).hasSize(10);

    }

    @Test
    @DisplayName("can find all bookings in a prison")
    void canFindAllForABookingInAPrison() {
        final var filter = OffenderBookingFilter
            .builder()
            .bookingSequence(1)
            .active(true)
            .prisonId("LEI")
            .build();

        final var pageOfBookings = repository.findAll(
            filter,
            PageRequest.of(0, 10, Sort.by("bookingId")));

        assertThat(pageOfBookings.getContent()).hasSize(10);

    }

    @Test
    @DisplayName("can find all bookings filtering by caseload")
    void canFindAllForABookingFilteringByCaseloads() {
        final var filter = OffenderBookingFilter
            .builder()
            .bookingSequence(1)
            .active(true)
            .caseloadIds(List.of("BXI"))
            .build();

        final var pageOfBookings = repository.findAll(
            filter,
            PageRequest.of(0, 10, Sort.by("bookingId")));

        assertThat(pageOfBookings.getContent()).hasSize(2);

    }

    @Test
    @DisplayName("can find all bookings from a list of nomsIds")
    void canFindAllForABookingInAListOfNomsIds() {
        final var filter = OffenderBookingFilter
            .builder()
            .bookingSequence(1)
            .active(true)
            .offenderNos(List.of("A1234AA", "A1234AB"))
            .build();

        final var pageOfBookings = repository.findAll(
            filter,
            PageRequest.of(0, 10, Sort.by("bookingId")));

        assertThat(pageOfBookings.getContent()).hasSize(2);

    }

    @Test
    @DisplayName("can find all bookings from a list of booking Ids")
    void canFindAllBookingsInListOfBookingIds() {
        final var filter = OffenderBookingFilter
            .builder()
            .bookingSequence(1)
            .active(true)
            .bookingIds(List.of(-1L, -2L))
            .build();

        final var pageOfBookings = repository.findAll(
            filter,
            PageRequest.of(0, 10, Sort.by("bookingId")));

        assertThat(pageOfBookings.getContent()).hasSize(2);

    }
    @Test
    @DisplayName("can find by primary key")
    void canFindByPrimaryKey() {
        final var booking = repository.findById(-1L);

        assertThat(booking).isPresent();
    }


    @Test
    public void givenExistingBooking_whenAddingMultipleIepLevel() {

        final long bookingId = -54L;
        final LocalDateTime before = LocalDateTime.of(2017, 9, 6, 0, 0);

        assertThatOffenderIepLevelsForBookingAre(bookingId, Tuple.tuple(BigDecimal.valueOf(1L), Timestamp.valueOf("2017-09-06 00:00:00.000"), "LEI", "BAS", null, "ITAG_USER"));

        final var booking = repository.findById(bookingId).orElseThrow();

        booking.addIepLevel(iepLevelReferenceCodeRepository.findById(IepLevel.pk("STD")).orElseThrow(), "A comment",  LocalDateTime.now(), staffUserAccountRepository.findById("ITAG_USER_ADM").orElseThrow());
        entityManager.flush();

        final Timestamp today = Timestamp.valueOf(LocalDate.now().atStartOfDay());

        assertThatOffenderIepLevelsForBookingAre(bookingId,
                Tuple.tuple(BigDecimal.valueOf(1L), Timestamp.valueOf("2017-09-06 00:00:00.000"), "LEI", "BAS", null, "ITAG_USER"),
                Tuple.tuple(BigDecimal.valueOf(2L), today, "BMI", "STD", "A comment", "ITAG_USER_ADM"));

        booking.addIepLevel(iepLevelReferenceCodeRepository.findById(IepLevel.pk("ENH")).orElseThrow(), "Comment 2",  LocalDateTime.now(), staffUserAccountRepository.findById("API_TEST_USER").orElseThrow());
        entityManager.flush();

        assertThatOffenderIepLevelsForBookingAre(bookingId,
                Tuple.tuple(BigDecimal.valueOf(1L), Timestamp.valueOf("2017-09-06 00:00:00.000"), "LEI", "BAS", null, "ITAG_USER"),
                Tuple.tuple(BigDecimal.valueOf(2L), today, "BMI", "STD", "A comment", "ITAG_USER_ADM"),
                Tuple.tuple(BigDecimal.valueOf(3L), today, "BMI", "ENH", "Comment 2", "API_TEST_USER"));

        final LocalDateTime after = LocalDateTime.now();

        assertThatOffenderIepLevelTimesForBookingAreBetween(bookingId, before, after);
    }

    private void assertThatOffenderIepLevelTimesForBookingAreBetween(long bookingId, LocalDateTime before, LocalDateTime after) {
        final var levels = offenderIepLevelsForBooking(bookingId);

        final Timestamp beforeTs = truncateNanos(before);
        final Timestamp afterTs = truncateNanos(after);

        assertThat(levels).noneMatch(iepLevel -> iepTime(iepLevel).before(beforeTs));
        assertThat(levels).noneMatch(iepLevel -> iepTime(iepLevel).after(afterTs));

    }

    private static Timestamp iepTime(Map<String, Object> level) {
        return (Timestamp) level.get("IEP_TIME");
    }

    private static Timestamp truncateNanos(LocalDateTime t) {
        final var ts = Timestamp.valueOf(t);
        ts.setNanos(0);
        return ts;
    }

    private void assertThatOffenderIepLevelsForBookingAre(long bookingId, Tuple... expected) {

        assertThat(offenderIepLevelsForBooking(bookingId))
                .extracting("IEP_LEVEL_SEQ", "IEP_DATE", "AGY_LOC_ID", "IEP_LEVEL", "COMMENT_TEXT", "USER_ID")
                .containsExactly(expected);
    }

    private List<Map<String, Object>> offenderIepLevelsForBooking(Long bookingId) {
        return jdbcTemplate.queryForList(
                "SELECT IEP_LEVEL_SEQ, IEP_DATE, IEP_TIME, AGY_LOC_ID, IEP_LEVEL, COMMENT_TEXT, USER_ID " +
                        "FROM OFFENDER_IEP_LEVELS " +
                        "WHERE OFFENDER_BOOK_ID = :bookingId " +
                        "ORDER BY IEP_LEVEL_SEQ", bookingId);
    }

}
