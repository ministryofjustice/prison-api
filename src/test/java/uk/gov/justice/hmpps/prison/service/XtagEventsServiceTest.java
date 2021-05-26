package uk.gov.justice.hmpps.prison.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.OffenderEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.XtagEventsRepository;
import uk.gov.justice.hmpps.prison.service.filters.OffenderEventsFilter;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEventsTransformer;
import uk.gov.justice.hmpps.prison.service.xtag.XtagEventNonJpa;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class XtagEventsServiceTest {

    private final Timestamp MOVEMENT_TIME = Timestamp.valueOf("2019-07-12 21:00:00.000");

    @Mock
    private XtagEventsRepository repository;

    @Mock
    private OffenderEventsTransformer transformer;

    @Mock
    private MovementsService movementsService;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    private XtagEventsService service;

    @BeforeEach
    public void setUp() {
        service = new XtagEventsService(repository, transformer, movementsService, offenderRepository, offenderBookingRepository);
    }

    @Test
    public void shouldAddNomsIdToOffenderAliasEvent() {
        assertEventIsDecoratedWithOffenderDisplayNoUsingOffenderId("OFFENDER_ALIAS-CHANGED");
    }

    @Test
    public void shouldAddNomsIdToOffenderDetailsChangedEvent() {
        assertEventIsDecoratedWithOffenderDisplayNoUsingOffenderId("OFFENDER_DETAILS-CHANGED");
    }

    @Test
    public void shouldDecorateWithExternalMovementData() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();
        final var externalMovementEntity = Movement.builder()
                .fromAgency("MDI")
                .toAgency("BAI")
                .movementType("REL")
                .movementDate(MOVEMENT_TIME.toLocalDateTime().toLocalDate())
                .movementTime(MOVEMENT_TIME.toLocalDateTime().toLocalTime())
                .offenderNo("A2345GB")
                .build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));
        when(movementsService.getMovementByBookingIdAndSequence(1L, 2)).thenReturn(Optional.of(externalMovementEntity));
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("EXTERNAL_MOVEMENT_RECORD-INSERTED").offenderId(1L).movementSeq(2L).bookingId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("offenderIdDisplay").containsExactly("A2345GB");
        assertThat(offenderEventList).extracting("fromAgencyLocationId").containsExactly("MDI");
        assertThat(offenderEventList).extracting("toAgencyLocationId").containsExactly("BAI");
        assertThat(offenderEventList).extracting("movementDateTime").containsExactly(MOVEMENT_TIME.toLocalDateTime());
        assertThat(offenderEventList).extracting("movementType").containsExactly("REL");
    }

    @Test
    public void shouldDecorateWithExternalMovementDataHandlesNullableFields() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();

        final var externalMovementEntity = Movement.builder().offenderNo("A2345GB").build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));
        when(movementsService.getMovementByBookingIdAndSequence(1L, 2)).thenReturn(Optional.of(externalMovementEntity));
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("EXTERNAL_MOVEMENT_RECORD-INSERTED").offenderId(1L).movementSeq(2L).bookingId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("bookingId").containsExactly(1L);
        assertThat(offenderEventList).extracting("movementSeq").containsExactly(2L);
        assertThat(offenderEventList).extracting("offenderIdDisplay").containsExactly("A2345GB");
        assertThat(offenderEventList).extracting("fromAgencyLocationId").containsNull();
        assertThat(offenderEventList).extracting("toAgencyLocationId").containsNull();
        assertThat(offenderEventList).extracting("movementDateTime").containsNull();
        assertThat(offenderEventList).extracting("movementType").containsNull();
    }

    @Test
    public void shouldDecorateWithExternalMovementDataHandlesNoRecordFound() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));
        when(movementsService.getMovementByBookingIdAndSequence(1L, 2)).thenReturn(Optional.empty());
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("EXTERNAL_MOVEMENT_RECORD-INSERTED").offenderId(1L).movementSeq(2L).bookingId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("bookingId").containsExactly(1L);
        assertThat(offenderEventList).extracting("movementSeq").containsExactly(2L);
        assertThat(offenderEventList).extracting("offenderIdDisplay").containsNull();
        assertThat(offenderEventList).extracting("fromAgencyLocationId").containsNull();
        assertThat(offenderEventList).extracting("toAgencyLocationId").containsNull();
        assertThat(offenderEventList).extracting("movementDateTime").containsNull();
        assertThat(offenderEventList).extracting("movementType").containsNull();
    }

    @Test
    public void shouldDecorateOffenderUpdatedWithOffenderDisplayNo() {
        assertEventIsDecoratedWithOffenderDisplayNoUsingOffenderId("OFFENDER-UPDATED");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "OFFENDER_MOVEMENT-DISCHARGE",
        "OFFENDER_MOVEMENT-RECEPTION",
        "BED_ASSIGNMENT_HISTORY-INSERTED",
        "CONFIRMED_RELEASE_DATE-CHANGED",
        "SENTENCE_DATES-CHANGED"})
    public void shouldDecorateWithOffenderDisplayNoUsingBookingId(String eventType) {
        assertEventIsDecoratedWithOffenderDisplayNoUsingBookingId(eventType);
    }

    @Test
    public void sentenceDateChangedDecorationFailureShouldNotPreventEventBeingRaised() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));

        when(offenderBookingRepository.findById(1234L)).thenReturn(Optional.empty());
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("SENTENCE_DATES-CHANGED").offenderId(1L).bookingId(1234L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("bookingId").containsExactly(1234L);
    }

    @Test
    public void shouldDecorateConfirmedReleaseDateChangedWithOffenderDisplayNo() {
        assertEventIsDecoratedWithOffenderDisplayNoUsingBookingId("CONFIRMED_RELEASE_DATE-CHANGED");
    }

    @Test
    public void confirmedReleaseDateChangedDecorationFailureShouldNotPreventEventBeingRaised() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));

        when(offenderBookingRepository.findById(1234L)).thenReturn(Optional.empty());
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("CONFIRMED_RELEASE_DATE-CHANGED").offenderId(1L).bookingId(1234L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("bookingId").containsExactly(1234L);
    }

    @Test
    public void appliesFudgeWhenNotCurrentlyInDaylightSavingsTime() {

        final var aWinterDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0);

        final var actual = XtagEventsService.asUtcPlusOne(aWinterDate);

        assertThat(actual).isEqualTo(aWinterDate.plusHours(1L));
    }

    @Test
    public void doesNotApplyFudgeWhenCurrentlyInDaylightSavingsTime() {

        final var aSummerDate = LocalDateTime.of(2020, 7, 1, 0, 0, 0, 0);

        final var actual = XtagEventsService.asUtcPlusOne(aSummerDate);

        assertThat(actual).isEqualTo(aSummerDate);
    }

    private void assertEventIsDecoratedWithOffenderDisplayNoUsingOffenderId(String eventName) {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();
        final var offender = Offender.builder().nomsId("A2345GB").id(1L).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));

        when(offenderRepository.findById(1L)).thenReturn(Optional.of(offender));
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType(eventName).offenderId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("offenderIdDisplay").containsExactly("A2345GB");
        assertThat(offenderEventList).extracting("offenderId").containsExactly(1L);
    }

    private void assertEventIsDecoratedWithOffenderDisplayNoUsingBookingId(String eventName) {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();
        final var offenderBooking = OffenderBooking.builder().bookingId(1234L).offender(Offender.builder().nomsId("A2345GB").id(1L).build()).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));

        when(offenderBookingRepository.findById(1234L)).thenReturn(Optional.of(offenderBooking));
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType(eventName).offenderId(1L).bookingId(1234L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("offenderIdDisplay").containsExactly("A2345GB");
        assertThat(offenderEventList).extracting("bookingId").containsExactly(1234L);
    }

}
