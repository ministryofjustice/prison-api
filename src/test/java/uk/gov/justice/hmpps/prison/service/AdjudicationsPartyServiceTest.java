package uk.gov.justice.hmpps.prison.service;

import org.assertj.core.matcher.AssertionMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import javax.persistence.EntityManager;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdjudicationsPartyServiceTest {

    private static final List<Long> EXAMPLE_VICTIM_STAFF_IDS = List.of(17380L, 17514L);
    private static final List<ExampleOffender> EXAMPLE_VICTIM_OFFENDERS = List.of(new ExampleOffender(1L, "A5015DY"), new ExampleOffender(2L, "G1835UN"));
    private static final List<ExampleOffender> EXAMPLE_CONNECTED_OFFENDERS = List.of(new ExampleOffender(3l, "G0662GG"), new ExampleOffender(4l, "A5060DY"));

    @Mock
    private AdjudicationRepository adjudicationRepository;
    @Mock
    private StaffUserAccountRepository staffUserAccountRepository;
    @Mock
    private OffenderBookingRepository bookingRepository;
    @Mock
    private EntityManager entityManager;

    private AdjudicationsPartyService service;

    private final Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());

    @BeforeEach
    public void beforeEach() {
        service = new AdjudicationsPartyService(
            staffUserAccountRepository,
            bookingRepository,
            adjudicationRepository,
            clock,
            entityManager);
    }

    @Test
    public void makesCallToRepositoryWithCorrectData() {
        var expectedVictimOffenderBooking = generateVictimOffenders();
        var expectedVictimOffenderIds = expectedVictimOffenderBooking.stream().map(b -> b.getOffender().getNomsId()).toList();
        var expectedConnectedOffenderBookings = generateConnectedOffenders();
        var expectedConnectedOffenderIds = expectedConnectedOffenderBookings.stream().map(b -> b.getOffender().getNomsId()).toList();
        var expectedVictimStaffUserAccounts = generateVictimStaff();
        var expectedVictimStaffIds = expectedVictimStaffUserAccounts.stream().map(s -> s.getStaff().getStaffId()).toList();
        var expectedVictimStaff = expectedVictimStaffUserAccounts.stream().map(s -> s.getStaff()).toList();

        when(adjudicationRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(basicAdjudication()));
        mockOffenderBookings(expectedConnectedOffenderBookings, expectedVictimOffenderBooking);
        mockStaffUserAccounts(expectedVictimStaffUserAccounts);


        service.updateAdjudicationParties(1l, expectedVictimStaffIds, expectedVictimOffenderIds, expectedConnectedOffenderIds);

        verify(adjudicationRepository).save(assertArgThat(actualAdjudication -> {
            assertThat(actualAdjudication.getVictimsOffenderBookings()).containsExactlyInAnyOrderElementsOf(expectedVictimOffenderBooking);
            assertThat(actualAdjudication.getConnectedOffenderBookings()).containsExactlyInAnyOrderElementsOf(expectedConnectedOffenderBookings);
            assertThat(actualAdjudication.getVictimsStaff()).containsExactlyInAnyOrderElementsOf(expectedVictimStaff);
        }));
    }

    @Test
    public void returnsCorrectData() {
        var expectedVictimOffenderBooking = generateVictimOffenders();
        var expectedVictimOffenderIds = expectedVictimOffenderBooking.stream().map(b -> b.getOffender().getNomsId()).toList();
        var expectedConnectedOffenderBookings = generateConnectedOffenders();
        var expectedConnectedOffenderIds = expectedConnectedOffenderBookings.stream().map(b -> b.getOffender().getNomsId()).toList();
        var expectedVictimStaffUserAccounts = generateVictimStaff();
        var expectedVictimStaffIds = expectedVictimStaffUserAccounts.stream().map(s -> s.getStaff().getStaffId()).toList();

        when(adjudicationRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(basicAdjudication()));
        mockOffenderBookings(expectedConnectedOffenderBookings, expectedVictimOffenderBooking);
        mockStaffUserAccounts(expectedVictimStaffUserAccounts);

        var returnedAdjudication = service.updateAdjudicationParties(1l, expectedVictimStaffIds, expectedVictimOffenderIds, expectedConnectedOffenderIds);
        assertThat(returnedAdjudication.getVictimStaffIds()).containsExactlyInAnyOrderElementsOf(expectedVictimStaffIds);
        assertThat(returnedAdjudication.getVictimOffenderIds()).containsExactlyInAnyOrderElementsOf(expectedVictimOffenderIds);
        assertThat(returnedAdjudication.getConnectedOffenderIds()).containsExactlyInAnyOrderElementsOf(expectedConnectedOffenderIds);
    }

    @Test
    public void removesPartiesFromAdjudication() {
        var expectedVictimOffenderBooking = generateVictimOffenders();
        var expectedVictimOffenderIds = expectedVictimOffenderBooking.stream().map(b -> b.getOffender().getNomsId()).toList();
        var expectedConnectedOffenderBookings = generateConnectedOffenders();
        var expectedConnectedOffenderIds = expectedConnectedOffenderBookings.stream().map(b -> b.getOffender().getNomsId()).toList();
        var expectedVictimStaffUserAccounts = generateVictimStaff();
        var expectedVictimStaffIds = expectedVictimStaffUserAccounts.stream().map(s -> s.getStaff().getStaffId()).toList();

        Adjudication adjudication = basicAdjudication();
        var idOfConnectedOffenderBookingToBeRemovedFromAdjudication = 1000l;
        adjudication.getParties().add(
            AdjudicationParty.builder()
                .id(new AdjudicationParty.PK(adjudication, 2l))
                .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
                .offenderBooking(
                    OffenderBooking.builder()
                        .bookingId(idOfConnectedOffenderBookingToBeRemovedFromAdjudication)
                        .build())
                .build());

        when(adjudicationRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(adjudication));
        mockOffenderBookings(expectedConnectedOffenderBookings, expectedVictimOffenderBooking);
        mockStaffUserAccounts(expectedVictimStaffUserAccounts);

        service.updateAdjudicationParties(1l, expectedVictimStaffIds, expectedVictimOffenderIds, expectedConnectedOffenderIds);

        verify(adjudicationRepository).save(assertArgThat(actualAdjudication -> {
            assertThat(actualAdjudication.getConnectedOffenderBookings().stream().map(OffenderBooking::getBookingId))
                .doesNotContain(idOfConnectedOffenderBookingToBeRemovedFromAdjudication);
        }));
    }

    @Test
    public void retainsPartiesFromAdjudication() {
        var expectedVictimOffenderBooking = generateVictimOffenders();
        var expectedVictimOffenderIds = expectedVictimOffenderBooking.stream().map(b -> b.getOffender().getNomsId()).toList();
        var expectedConnectedOffenderBookings = generateConnectedOffenders();
        var expectedConnectedOffenderIds = expectedConnectedOffenderBookings.stream().map(b -> b.getOffender().getNomsId()).toList();
        var expectedVictimStaffUserAccounts = generateVictimStaff();
        var expectedVictimStaffIds = expectedVictimStaffUserAccounts.stream().map(s -> s.getStaff().getStaffId()).toList();

        Adjudication adjudication = basicAdjudication();
        var idOfConnectedOffenderBookingToBeRetainedOnAdjudication = expectedConnectedOffenderBookings.get(0).getBookingId();
        var sequenceOfConnectedOffenderBookingToBeRetainedOnAdjudication = 2l;
        adjudication.getParties().add(
            AdjudicationParty.builder()
                .id(new AdjudicationParty.PK(adjudication, sequenceOfConnectedOffenderBookingToBeRetainedOnAdjudication))
                .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
                .offenderBooking(
                    OffenderBooking.builder()
                        .bookingId(idOfConnectedOffenderBookingToBeRetainedOnAdjudication)
                        .build())
                .build());

        when(adjudicationRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(adjudication));
        mockOffenderBookings(expectedConnectedOffenderBookings, expectedVictimOffenderBooking);
        mockStaffUserAccounts(expectedVictimStaffUserAccounts);

        service.updateAdjudicationParties(1l, expectedVictimStaffIds, expectedVictimOffenderIds, expectedConnectedOffenderIds);

        verify(adjudicationRepository).save(assertArgThat(actualAdjudication -> {
            assertThat(actualAdjudication.getConnectedOffenderPartyWithBookingId(idOfConnectedOffenderBookingToBeRetainedOnAdjudication)
                .get().getId().getPartySeq())
                .isEqualTo(sequenceOfConnectedOffenderBookingToBeRetainedOnAdjudication);
        }));
    }

    @Test
    public void removeIds(){
        var required = List.of(
            new ExampleClassWithId(1l),
            new ExampleClassWithId(2l),
            new ExampleClassWithId(3l),
            new ExampleClassWithId(4l),
            new ExampleClassWithId(5l));
        var current = List.of(
            new ExampleClassWithId(1l),
            new ExampleClassWithId(10l),
            new ExampleClassWithId(11l));
        assertThat(AdjudicationsPartyService.idsToRemove(required, current, e -> e.id))
            .containsExactlyInAnyOrderElementsOf(List.of(10l, 11l));
    }

    @Test
    public void addIds(){
        var required = List.of(
            new ExampleClassWithId(1l),
            new ExampleClassWithId(2l),
            new ExampleClassWithId(3l),
            new ExampleClassWithId(4l),
            new ExampleClassWithId(5l));
        var current = List.of(
            new ExampleClassWithId(1l),
            new ExampleClassWithId(10l),
            new ExampleClassWithId(11l));
        assertThat(AdjudicationsPartyService.idsToAdd(required, current, e -> e.id))
            .containsExactlyInAnyOrderElementsOf(List.of(2l, 3l, 4l, 5l));
    }

    @Test
    public void removeById(){
        var toRemain = List.of(new ExampleClassWithId(1l), new ExampleClassWithId(2l));
        var listToRemoveElementFrom = new ArrayList<>(toRemain);
        listToRemoveElementFrom.add(new ExampleClassWithId(3l));
        AdjudicationsPartyService.remove(listToRemoveElementFrom, e -> e.id, 3l);
        assertThat(listToRemoveElementFrom)
            .containsExactlyInAnyOrderElementsOf(toRemain);
    }

    private final Adjudication basicAdjudication() {
        final var adjudication = Adjudication.builder().build();
        final var offenderParty = AdjudicationParty.builder().id(new AdjudicationParty.PK(adjudication, 1l)).incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER).build();
        adjudication.getParties().add(offenderParty);
        return adjudication;
    }

    private List<OffenderBooking> generateVictimOffenders() {
        return EXAMPLE_VICTIM_OFFENDERS.stream().
            map(o -> OffenderBooking.builder()
                .bookingId(o.bookingId)
                .offender(Offender.builder()
                    .nomsId(o.offenderId)
                    .build())
                .build())
            .toList();
    }

    private List<OffenderBooking> generateConnectedOffenders() {
        return EXAMPLE_CONNECTED_OFFENDERS.stream().
            map(o -> OffenderBooking.builder()
                .bookingId(o.bookingId)
                .offender(Offender.builder()
                    .nomsId(o.offenderId)
                    .build())
                .build())
            .toList();
    }

    private List<StaffUserAccount> generateVictimStaff() {
        return EXAMPLE_VICTIM_STAFF_IDS.stream().
            map(id -> StaffUserAccount.builder().staff(Staff.builder().staffId(id).build()).build()).toList();
    }

    private void mockOffenderBookings(List<OffenderBooking>... offenderBookings) {
        when(bookingRepository.findByOffenderNomsIdAndBookingSequence(any(), any()))
            .thenAnswer(request -> {
                var offenderNo = request.getArgument(0);
                return List.of(offenderBookings).stream().flatMap(Collection::stream)
                    .filter(o -> o.getOffender().getNomsId().equals(offenderNo))
                    .findFirst();
            });
    }

    private void mockStaffUserAccounts(List<StaffUserAccount> staffUserAccounts) {
        when(staffUserAccountRepository.findByStaff_StaffId(any()))
            .thenAnswer(request -> {
                var staffId = request.getArgument(0);
                return staffUserAccounts.stream().filter(s -> s.getStaff().getStaffId().equals(staffId)).findFirst();
            });
    }

    private static <T> T assertArgThat(final Consumer<T> assertions) {
        return MockitoHamcrest.argThat(new AssertionMatcher<>() {
            @Override
            public void assertion(T actual) throws AssertionError {
                assertions.accept(actual);
            }
        });
    }

    private static class ExampleClassWithId {
        public long id;

        public ExampleClassWithId(Long id) {
            this.id = id;
        }
    }


    private final static class ExampleOffender {
        public long bookingId;
        public String offenderId;

        public ExampleOffender(long bookingId, String offenderId) {
            this.bookingId = bookingId;
            this.offenderId = offenderId;
        }
    }
}
