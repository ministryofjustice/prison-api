package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.repository.InmateAlertRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAlertFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAlertRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InmateAlertServiceImplTest {
    @Mock
    private InmateAlertRepository inmateAlertRepository;

    @Mock
    private OffenderAlertRepository offenderAlertRepository;

    private InmateAlertService service;

    @BeforeEach
    public void setUp() {
        final int MAX_MATCH_SIZE = 10;
        service = new InmateAlertService(
            inmateAlertRepository,
            offenderAlertRepository,
            MAX_MATCH_SIZE
        );
    }

    @Test
    void testGetInmateAlertsByOffenderNosAtAgency() {
        final var offenders = IntStream.range(1, 20).mapToObj(String::valueOf).toList();

        service.getInmateAlertsByOffenderNosAtAgency("MDI", offenders);

        verify(inmateAlertRepository).getAlertsByOffenderNos("MDI", List.of("1","2","3","4","5","6","7","8","9","10"),true, "bookingId,alertId", Order.ASC);
        verify(inmateAlertRepository).getAlertsByOffenderNos("MDI", List.of("11","12","13","14","15","16","17","18","19"),true, "bookingId,alertId", Order.ASC);
    }

    @Nested
    class GetAlertsForLatestBookingForOffender {
        @Captor
        private ArgumentCaptor<Specification<OffenderAlert>> specificationArgumentCaptor;

        @Captor
        private ArgumentCaptor<Sort> sortArgumentCaptor;

        @BeforeEach
        void setUp() {
            when(offenderAlertRepository.findAll(any(), any(Sort.class)))
                .thenReturn(List.of(alertOfSequence(1), alertOfSequence(2)));
        }

        @Test
        @DisplayName("will setup filter with offender and alert codes")
        void willSetupFilterWithOffenderAndAlertCodes() {
            service.getAlertsForLatestBookingForOffender("A1179MT", "XCU,XTACT", "alertType,bookingId", Direction.DESC);

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

            assertThat(specificationArgumentCaptor.getValue()).isInstanceOf(OffenderAlertFilter.class);
            final OffenderAlertFilter filter = (OffenderAlertFilter) specificationArgumentCaptor.getValue();
            assertThat(filter.getOffenderNo()).isEqualTo("A1179MT");
            assertThat(filter.getAlertCodes()).isEqualTo("XCU,XTACT");
            assertThat(filter.getLatestBooking()).isTrue();
        }

        @Test
        @DisplayName("will map sort property names")
        void willMapSortPropertyNames() {
            service.getAlertsForLatestBookingForOffender("A1179MT", "XCU,XTACT", "alertType,bookingId", Direction.DESC);

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

            final var sort = sortArgumentCaptor.getValue();

            assertThat(sort.stream())
                .extracting(Sort.Order::getProperty)
                .containsExactly("alertType", "offenderBooking.bookingId");
        }

        @Test
        @DisplayName("will use sort direction for each property")
        void willUseSortDirection() {
            service.getAlertsForLatestBookingForOffender("A1179MT", "XCU,XTACT", "alertType,bookingId", Direction.DESC);

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

            final var sort = sortArgumentCaptor.getValue();

            assertThat(sort.stream())
                .extracting(Sort.Order::getDirection)
                .containsExactly(Direction.DESC, Direction.DESC);
        }

        @Test
        @DisplayName("will transform results")
        void willTransformResults() {
            final var alerts = service.getAlertsForLatestBookingForOffender("A1179MT", "XCU,XTACT", "alertType,bookingId", Direction.DESC);

            assertThat(alerts).hasSize(2).extracting(Alert::getAlertId).containsExactly(1L, 2L);

        }
    }
    @Nested
    class GetAlertsForAllBookingsForOffender {
        @Captor
        private ArgumentCaptor<Specification<OffenderAlert>> specificationArgumentCaptor;

        @Captor
        private ArgumentCaptor<Sort> sortArgumentCaptor;

        @BeforeEach
        void setUp() {
            when(offenderAlertRepository.findAll(any(), any(Sort.class)))
                .thenReturn(List.of(alertOfSequence(1), alertOfSequence(2)));
        }

        @Test
        @DisplayName("will setup filter with offender and alert codes")
        void willSetupFilterWithOffenderAndAlertCodes() {
            service.getAlertsForAllBookingsForOffender("A1179MT", "XCU,XTACT", "alertType,bookingId", Direction.DESC);

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

            assertThat(specificationArgumentCaptor.getValue()).isInstanceOf(OffenderAlertFilter.class);
            final OffenderAlertFilter filter = (OffenderAlertFilter) specificationArgumentCaptor.getValue();
            assertThat(filter.getOffenderNo()).isEqualTo("A1179MT");
            assertThat(filter.getAlertCodes()).isEqualTo("XCU,XTACT");
            assertThat(filter.getLatestBooking()).isNull();
        }

        @Test
        @DisplayName("will map sort property names")
        void willMapSortPropertyNames() {
            service.getAlertsForAllBookingsForOffender("A1179MT", "XCU,XTACT", "alertType,bookingId", Direction.DESC);

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

            final var sort = sortArgumentCaptor.getValue();

            assertThat(sort.stream())
                .extracting(Sort.Order::getProperty)
                .containsExactly("alertType", "offenderBooking.bookingId");
        }

        @Test
        @DisplayName("will use sort direction for each property")
        void willUseSortDirection() {
            service.getAlertsForAllBookingsForOffender("A1179MT", "XCU,XTACT", "alertType,bookingId", Direction.DESC);

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

            final var sort = sortArgumentCaptor.getValue();

            assertThat(sort.stream())
                .extracting(Sort.Order::getDirection)
                .containsExactly(Direction.DESC, Direction.DESC);
        }

        @Test
        @DisplayName("will transform results")
        void willTransformResults() {
            final var alerts = service.getAlertsForAllBookingsForOffender("A1179MT", "XCU,XTACT", "alertType,bookingId", Direction.DESC);

            assertThat(alerts).hasSize(2).extracting(Alert::getAlertId).containsExactly(1L, 2L);

        }
    }

    private OffenderAlert alertOfSequence(int sequence) {
        return OffenderAlert
            .builder()
            .alertDate(LocalDate.parse("2020-01-30"))
            .offenderBooking(OffenderBooking
                .builder()
                .offender(Offender.builder().nomsId("A1234JK").build())
                .build())
            .code(new AlertCode("RSS", "Risk to Staff - Custody"))
            .alertCode("RSS")
            .comment("Do not trust this person")
            .createUser(StaffUserAccount
                .builder()
                .username("someuser")
                .staff(Staff.builder().firstName("JANE").lastName("BUBBLES").build())
                .build())
            .expiryDate(LocalDate.parse("2120-10-30"))
            .modifyUser(StaffUserAccount
                .builder()
                .username("someotheruser")
                .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
                .build())
            .sequence(sequence)
            .type(new AlertType("R", "Risk"))
            .alertType("R")
            .status("ACTIVE")
            .createDatetime(LocalDateTime.now().minusYears(10))
            .createUserId("someuser")
            .modifyDatetime(LocalDateTime.now().minusYears(1))
            .modifyUserId("someotheruser")
            .build();
    }
}
