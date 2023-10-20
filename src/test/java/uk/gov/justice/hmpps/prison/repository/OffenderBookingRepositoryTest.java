package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceCalcTypeRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.util.List;

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
    private SentenceCalcTypeRepository sentenceCalcTypeRepository;

    @Autowired
    private OffenderBookingRepository repository;

    @Autowired
    private AgencyLocationRepository agencyLocationRepository;

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
    void canFindAllBookingsAtEstablishment() {

        final var location = agencyLocationRepository.getReferenceById("LEI");

        final var result = repository.findAllOffenderBookingsByActiveTrueAndLocationAndSentences_statusAndSentences_CalculationType_CalculationTypeNotLikeAndSentences_CalculationType_CategoryNot(
            location,
            "A",
            "%AGG%",
            "LICENCE"
        );

       assertThat(result).hasSize(2);

    }

    @Test
    @DisplayName("can find by primary key")
    void canFindByPrimaryKey() {
        final var booking = repository.findById(-1L);

        assertThat(booking).isPresent();
    }

}
