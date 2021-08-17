package uk.gov.justice.hmpps.prison.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.jetbrains.annotations.NotNull;
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
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
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
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InmateAlertServiceImplTest {
    @Mock
    private InmateAlertRepository inmateAlertRepository;

    @Mock
    private OffenderAlertRepository offenderAlertRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private ReferenceDomainService referenceDomainService;

    private InmateAlertService service;

    private final int MAX_MATCH_SIZE = 10;

    @BeforeEach
    public void setUp() {
        service = new InmateAlertService(
                inmateAlertRepository,
                offenderAlertRepository,
                authenticationFacade,
                telemetryClient,
                referenceDomainService,
                MAX_MATCH_SIZE);
    }

    @Test
    public void testCorrectNumberAlertReturned() {
        final var alerts = createAlerts();

        when(inmateAlertRepository.getAlerts(eq(-1L), any(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = service.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).hasSize(alerts.getItems().size());
    }

    @Test
    public void testCorrectExpiredAlerts() {
        final var alerts = createAlerts();

        when(inmateAlertRepository.getAlerts(eq(-1L), isNull(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = service.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).extracting("expired").containsSequence(false, false, true, true, false);
    }

    @Test
    public void testAlertRepository_CreateAlertIsCalledWithCorrectParams() {
        final ReferenceCode alertType = getAlertReferenceCode();

        when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), anyBoolean()))
                .thenReturn(Optional.of(alertType));

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.createNewAlert(anyLong(), any())).thenReturn(1L);

        final var alertId = service.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());

        assertThat(alertId).isEqualTo(1L);

        verify(inmateAlertRepository).createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());
    }

    @Test
    public void testAlertDate_SevenDaysInThePastThrowsException() {
        assertThat(catchThrowable(() -> {
            service.createNewAlert(-1L, CreateAlert
                    .builder().alertDate(LocalDate.now().minusDays(8)).build());
        })).as("Alert date cannot go back more than seven days.").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testAlertDate_InTheFutureThrowsException() {
        assertThat(catchThrowable(() -> {
            service.createNewAlert(-1L, CreateAlert
                    .builder().alertDate(LocalDate.now().plusDays(1)).build());
        })).as("Alert date cannot be in the future.").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testAlertRepository_ExpireAlertIsCalledWithCorrectParams() {
        final var alert = Alert.builder()
                .alertId(4L)
                .bookingId(-1L)
                .alertType(format("ALERTYPE%d", 1L))
                .alertCode(format("ALERTCODE%d", 1L))
                .active(true)
                .comment(format("This is a comment %d", 1L))
                .dateCreated(LocalDate.now())
                .build();

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");

        when(inmateAlertRepository.updateAlert(anyLong(), anyLong(), any())).thenReturn(Optional.of(alert));
        when(inmateAlertRepository.getAlert(anyLong(), anyLong())).thenReturn(Optional.of(alert));

        final var updatedAlert = service.updateAlert(-1L, 4L, AlertChanges
                .builder()
                .expiryDate(LocalDate.now())
                .build());

        assertThat(updatedAlert).isEqualTo(alert);

        verify(inmateAlertRepository).updateAlert(-1L, 4L, AlertChanges
                .builder()
                .expiryDate(LocalDate.now())
                .build());
    }

    @Test
    public void testExceptionIsThrown_WhenExpiryDateAndCommentAreNull() {
        assertThatThrownBy(() -> service.updateAlert(1L, 2L, AlertChanges.builder().build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Please provide an expiry date, or a comment");
    }

    @Test
    public void testThatOnlyTheCommentTextGetsUpdate_WhenExpiryIsNull() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");

        when(inmateAlertRepository.updateAlert(anyLong(), anyLong(), any())).thenReturn(Optional.of(Alert.builder().build()));

        service.updateAlert(1L, 2L, AlertChanges.builder().comment("Test").build());

        verify(inmateAlertRepository).updateAlert(1L, 2L, AlertChanges.builder().comment("Test").build());
    }

    @Test
    public void testThatTelemetryFires_WhenCommentIsUpdated() {

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");

        when(inmateAlertRepository.updateAlert(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(Alert.builder().alertCode("X").alertType("XX").build()));

        service.updateAlert(1L, 2L, AlertChanges.builder().comment("Test").build());

        verify(inmateAlertRepository).updateAlert(1L, 2L, AlertChanges.builder().comment("Test").build());

        verify(telemetryClient).trackEvent("Alert updated", Map.of(
                "bookingId", "1",
                "alertSeq", "2",
                "comment", "Comment text updated",
                "updated_by", "ITAG_USER"
        ), null);
    }

    @Test
    public void testYouCannotCreateDuplicateAlerts() {
        final ReferenceCode alertType = getAlertReferenceCode();
        final var originalAlert = Alert.builder().alertCode("X").alertType("XX").build();

        when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), anyBoolean()))
                .thenReturn(Optional.of(alertType));

        when(inmateAlertRepository.getActiveAlerts(anyLong())).thenReturn(List.of(originalAlert));

        assertThatThrownBy(() -> service.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert already exists for this offender.");
    }

    @Test
    public void testTelemetryEventHasBeenRaised_OnAlertCreation() {
        final ReferenceCode alertType = getAlertReferenceCode();

        when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), anyBoolean()))
                .thenReturn(Optional.of(alertType));

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.createNewAlert(anyLong(), any())).thenReturn(1L);

        final var alertId = service.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());

        verify(telemetryClient).trackEvent("Alert created", Map.of(
                "alertSeq", String.valueOf(alertId),
                "alertDate", LocalDate.now().atStartOfDay().toLocalDate().toString(),
                "alertCode", "X",
                "alertType", "XX",
                "bookingId", "-1",
                "created_by", "ITAG_USER"
        ), null);
    }

    @Test
    public void testTelemetryEventHasBeenRaised_OnAlertExpire() {

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.getAlert(anyLong(), anyLong())).thenReturn(Optional.of(Alert.builder().active(true).build()));
        when(inmateAlertRepository.updateAlert(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(Alert.builder().build()));

        service.updateAlert(-1L, -2L, AlertChanges
                .builder()
                .expiryDate(LocalDate.now())
                .build());

        verify(telemetryClient).trackEvent("Alert updated", Map.of(
                "bookingId", "-1",
                "alertSeq", "-2",
                "expiryDate", LocalDate.now().atStartOfDay().toLocalDate().toString(),
                "updated_by", "ITAG_USER"
        ), null);
    }

    @Test
    public void testAlertTypeIsCorrect_BeforeCreating() {
        when(referenceDomainService.getReferenceCodeByDomainAndCode("ALERT", "X", true))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createNewAlert(-1L, CreateAlert.builder().alertType("X").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert type does not exist.");
    }

    @Test
    public void testAlertCodeIsCorrect_BeforeCreating() {
        final var alertType = new ReferenceCode();
        final var alertCode = new ReferenceCode();
        alertCode.setCode("A134");
        alertType.setSubCodes(List.of(alertCode));

        when(referenceDomainService.getReferenceCodeByDomainAndCode("ALERT", "X", true))
                .thenReturn(Optional.of(alertType));

        assertThatThrownBy(() ->
                service.createNewAlert(-1L, CreateAlert.builder().alertType("X").alertCode("XX").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert code does not exist.");
    }


    @Test
    public void testAlertCanNotBeMadeInactiveIfAlreadyInactive() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.getAlert(anyLong(), anyLong())).thenReturn(Optional.of(Alert.builder().active(false).build()));

        assertThatThrownBy(() ->
                service.updateAlert(-14, 1, AlertChanges.builder().expiryDate(LocalDate.now()).build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert is already inactive.");
    }

    @Test
    public void testGetInmateAlertsByOffenderNosAtAgency() {
        final var offenders = IntStream.range(1, 20).mapToObj(String::valueOf).collect(Collectors.toList());

        service.getInmateAlertsByOffenderNosAtAgency("MDI", offenders);

        verify(inmateAlertRepository).getAlertsByOffenderNos("MDI", List.of("1","2","3","4","5","6","7","8","9","10"),true, null, "bookingId,alertId", Order.ASC);
        verify(inmateAlertRepository).getAlertsByOffenderNos("MDI", List.of("11","12","13","14","15","16","17","18","19"),true, null, "bookingId,alertId", Order.ASC);
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

    @Nested
    class GetAlertsForBooking {
        @Captor
        private ArgumentCaptor<Specification<OffenderAlert>> specificationArgumentCaptor;

        @Captor
        private ArgumentCaptor<Pageable> pageableArgumentCaptor;


        @BeforeEach
        void setUp() {
            when(offenderAlertRepository.findAll(any(), any(PageRequest.class)))
                .thenAnswer(request -> new PageImpl<>(List.of(alertOfSequence(1), alertOfSequence(2)), request.getArgument(1), 2));
        }

        @Test
        @DisplayName("will setup filter with bookingId and filter properties")
        void willSetupFilterWithOffenderAndAlertCodes() {
            service.getAlertsForBooking(99L,
                LocalDate.parse("2020-01-01"),
                LocalDate.parse("2021-12-31"),
                "V",
                "ACTIVE",
                PageRequest.of(1, 10, Direction.ASC, "dateCreated"));

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), pageableArgumentCaptor.capture());

            assertThat(specificationArgumentCaptor.getValue()).isInstanceOf(OffenderAlertFilter.class);

            final OffenderAlertFilter filter = (OffenderAlertFilter) specificationArgumentCaptor.getValue();
            assertThat(filter.getBookingId()).isEqualTo(99L);
            assertThat(filter.getFromAlertDate()).isEqualTo("2020-01-01");
            assertThat(filter.getToAlertDate()).isEqualTo("2021-12-31");
            assertThat(filter.getAlertTypes()).isEqualTo("V");
            assertThat(filter.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("no parts of the filter are mandatory (other than bookingId)")
        void noPartsOfTheFilterAreMandatory() {
            service.getAlertsForBooking(99L,
                null,
                null,
                null,
                null,
                PageRequest.of(1, 10, Direction.ASC, "dateCreated"));

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), pageableArgumentCaptor.capture());

            assertThat(specificationArgumentCaptor.getValue()).isInstanceOf(OffenderAlertFilter.class);

            final OffenderAlertFilter filter = (OffenderAlertFilter) specificationArgumentCaptor.getValue();
            assertThat(filter.getBookingId()).isEqualTo(99L);
            assertThat(filter.getFromAlertDate()).isNull();
            assertThat(filter.getToAlertDate()).isNull();
            assertThat(filter.getAlertTypes()).isNull();
            assertThat(filter.getStatus()).isNull();
            assertThat(filter.getLatestBooking()).isNull();
            assertThat(filter.getOffenderNo()).isNull();
        }


        @Test
        @DisplayName("will map sort property names")
        void willMapSortPropertyNames() {
            service.getAlertsForBooking(99L,
                LocalDate.parse("2020-01-01"),
                LocalDate.parse("2021-12-31"),
                "V",
                "ACTIVE",
                PageRequest.of(1, 10, Direction.ASC, "dateCreated", "active"));

            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), pageableArgumentCaptor.capture());

            final var pageRequest = pageableArgumentCaptor.getValue();

            assertThat(pageRequest.getSort().stream())
                .extracting(Sort.Order::getProperty)
                .containsExactly("alertDate", "status");
        }

        @Test
        @DisplayName("will use sort direction for each property")
        void willUseSortDirection() {
            service.getAlertsForBooking(99L,
                LocalDate.parse("2020-01-01"),
                LocalDate.parse("2021-12-31"),
                "V",
                "ACTIVE",
                PageRequest.of(1, 10, Direction.ASC, "dateCreated", "active"));
            verify(offenderAlertRepository).findAll(specificationArgumentCaptor.capture(), pageableArgumentCaptor.capture());

            final var pageRequest = pageableArgumentCaptor.getValue();

            assertThat(pageRequest.getSort().stream())
                .extracting(Sort.Order::getDirection)
                .containsExactly(Direction.ASC, Direction.ASC);
        }

        @Test
        @DisplayName("will transform results")
        void willTransformResults() {
            final var alerts = service.getAlertsForBooking(99L,
                LocalDate.parse("2020-01-01"),
                LocalDate.parse("2021-12-31"),
                "V",
                "ACTIVE",
                PageRequest.of(1, 10, Direction.ASC, "dateCreated", "active"));

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

    private Page<Alert> createAlerts() {
        final var now = LocalDate.now();

        final var alerts = Arrays.asList(
                buildAlert(-1L, now.minusMonths(1), now.plusDays(2)),
                buildAlert(-2L, now.minusMonths(2), now.plusDays(1)),
                buildAlert(-3L, now.minusMonths(3), now),
                buildAlert(-4L, now.minusMonths(4), now.minusDays(1)),
                buildAlert(-5L, now.minusMonths(5), null)
        );

        return new Page<>(alerts, 5, 0, 10);
    }

    private Alert buildAlert(final long id, final LocalDate dateCreated, final LocalDate dateExpires) {
        return Alert.builder()
                .alertId(id)
                .alertType(format("ALERTYPE%d", id))
                .alertCode(format("ALERTCODE%d", id))
                .comment(format("This is a comment %d", id))
                .dateCreated(dateCreated)
                .dateExpires(dateExpires)
                .build();
    }

    @NotNull
    private ReferenceCode getAlertReferenceCode() {
        final var alertType = new ReferenceCode();
        alertType.setCode("XX");
        final var alertCode = new ReferenceCode();
        alertCode.setCode("X");
        alertType.setSubCodes(List.of(alertCode));
        return alertType;
    }
}
