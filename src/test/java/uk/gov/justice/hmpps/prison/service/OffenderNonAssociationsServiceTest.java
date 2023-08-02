package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociation;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNonAssociationDetailRepository;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderNonAssociationsServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2023-08-01T12:45:00.00Z"), ZoneId.of("Europe/London"));

    private final Offender victim = Offender.builder()
            .nomsId("ABC")
            .firstName("Fred")
            .lastName("Bloggs")
            .build();

    private final OffenderBooking victimsBooking = OffenderBooking.builder()
            .bookingId(2L)
            .location(AgencyLocation.builder()
                    .id("PVI")
                    .description("Pentonville")
                    .build())
            .assignedLivingUnit(AgencyInternalLocation.builder()
                    .locationId(200L)
                    .description("cell 1")
                    .build())
            .build();

    private final OffenderBooking perpBooking = OffenderBooking.builder()
            .bookingId(3L)
            .location(AgencyLocation.builder()
                    .id("MDI")
                    .description("Moorland")
                    .build())
            .assignedLivingUnit(AgencyInternalLocation.builder()
                    .description("cell 2")
                    .locationId(123L)
                    .build())
            .build();
    private final Offender perpetrator = Offender.builder()
            .nomsId("DEF")
            .firstName("Joseph")
            .lastName("Bloggs")
            .build();

    private final OffenderBooking samePrisonOffenderBooking = OffenderBooking.builder()
            .bookingId(4L)
            .location(AgencyLocation.builder()
                    .id("PVI")
                    .description("Pentonville")
                    .build())
            .assignedLivingUnit(AgencyInternalLocation.builder()
                    .locationId(201L)
                    .description("cell 2")
                    .build())
            .build();
    private final Offender samePrisonOffender = Offender.builder()
            .nomsId("FFF")
            .firstName("Test")
            .lastName("Bloggs")
            .build();


    private final OffenderBooking victimBookingWithoutOptionals = OffenderBooking.builder()
            .bookingId(1L)
            .location(AgencyLocation.builder()
                    .id("PVI")
                    .description("Pentonville")
                    .build())
            .build();

    private final List<uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail> filterNaList = List.of(
            uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail.builder()
                    .offender(victim)
                    .nsOffender(perpetrator)
                    .offenderBooking(victimsBooking)
                    .effectiveDate(LocalDate.now(clock).minusDays(15))
                    .expiryDate(LocalDate.now(clock).minusDays(1))
                    .comments("This is an inactive NA")
                    .authorizedBy("the boss")
                    .nonAssociationReason(new NonAssociationReason("VIC", "Victim"))
                    .nonAssociationType(new NonAssociationType("WING", "Do Not Locate on Same Wing"))
                    .nonAssociation(uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociation.builder()
                            .offender(victim)
                            .nsOffender(perpetrator)
                            .offenderBooking(victimsBooking)
                            .nonAssociationReason(new NonAssociationReason("PER", "Perpetrator"))
                            .recipNonAssociationReason(new NonAssociationReason("PER", "recip - Perpetrator"))
                            .build())
                    .build(),
            uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail.builder()
                    .offender(victim)
                    .nsOffender(samePrisonOffender)
                    .offenderBooking(victimsBooking)
                    .effectiveDate(LocalDate.now(clock))
                    .comments("Active NA")
                    .authorizedBy("the boss")
                    .nonAssociationReason(new NonAssociationReason("VIC", "Victim"))
                    .nonAssociationType(new NonAssociationType("WING", "Do Not Locate on Same Wing"))
                    .nonAssociation(uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociation.builder()
                            .offender(victim)
                            .nsOffender(samePrisonOffender)
                            .offenderBooking(victimsBooking)
                            .nonAssociationReason(new NonAssociationReason("PER", "Perpetrator"))
                            .recipNonAssociationReason(new NonAssociationReason("PER", "recip - Perpetrator"))
                            .build())
                    .build(),
            uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail.builder()
                    .offender(victim)
                    .nsOffender(perpetrator)
                    .offenderBooking(victimsBooking)
                    .effectiveDate(LocalDate.now(clock).minusDays(1))
                    .expiryDate(LocalDate.now(clock).plusDays(1))
                    .comments("Different Prison")
                    .authorizedBy("the boss")
                    .nonAssociationReason(new NonAssociationReason("VIC", "Victim"))
                    .nonAssociationType(new NonAssociationType("WING", "Do Not Locate on Same Wing"))
                    .nonAssociation(uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociation.builder()
                            .offender(victim)
                            .nsOffender(perpetrator)
                            .offenderBooking(victimsBooking)
                            .nonAssociationReason(new NonAssociationReason("PER", "Perpetrator"))
                            .recipNonAssociationReason(new NonAssociationReason("PER", "recip - Perpetrator"))
                            .build())
                    .build());
    @Mock
    private OffenderBookingRepository bookingRepository;
    @Mock
    private OffenderNonAssociationDetailRepository offenderNonAssociationDetailRepository;
    private OffenderNonAssociationsService service;

    @BeforeEach
    void setup() {
        victim.addBooking(victimsBooking);
        victim.addBooking(victimBookingWithoutOptionals);
        perpetrator.addBooking(perpBooking);
        samePrisonOffender.addBooking(samePrisonOffenderBooking);
        service = new OffenderNonAssociationsService(bookingRepository, offenderNonAssociationDetailRepository, clock);
    }

    @Test
    void retrieve_maps_no_non_associations_for_offender() {
        when(bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(victimsBooking.getOffender().getNomsId(), 1)).thenReturn(Optional.of(victimsBooking));
        when(offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(victimsBooking.getOffender().getNomsId())).thenReturn(emptyList());

        assertThat(service.retrieveByOffenderNo(victimsBooking.getOffender().getNomsId(), false, false)).isEqualTo(OffenderNonAssociationDetails.builder()
                .offenderNo("ABC")
                .firstName("Fred")
                .lastName("Bloggs")
                .agencyDescription("Pentonville")
                .agencyId("PVI")
                .assignedLivingUnitDescription("cell 1")
                .assignedLivingUnitId(200L)
                .nonAssociations(List.of())
                .build());
    }

    @Test
    void retrieve_maps_associations_for_offender() {
        final List<uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail> nonAssociationDetails = List.of(
                uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail.builder()
                        .offender(victim)
                        .nsOffender(perpetrator)
                        .offenderBooking(victimsBooking)
                        .effectiveDate(LocalDate.now(clock).minusDays(1))
                        .expiryDate(LocalDate.now(clock).minusDays(1))
                        .comments("do not let these offenders share the same location")
                        .authorizedBy("the boss")
                        .nonAssociationReason(new NonAssociationReason("VIC", "Victim"))
                        .nonAssociationType(new NonAssociationType("WING", "Do Not Locate on Same Wing"))
                        .nonAssociation(uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociation.builder()
                                .offender(victim)
                                .nsOffender(perpetrator)
                                .nonAssociationReason(new NonAssociationReason("PER", "Perpetrator"))
                                .recipNonAssociationReason(new NonAssociationReason("PER", "recip - Perpetrator"))
                                .build())
                        .build());

        when(bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(victimsBooking.getOffender().getNomsId(), 1)).thenReturn(Optional.of(victimsBooking));
        when(offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(victimsBooking.getOffender().getNomsId())).thenReturn(nonAssociationDetails);

        assertThat(service.retrieveByOffenderNo(victimsBooking.getOffender().getNomsId(), false, false))
                .isEqualTo(OffenderNonAssociationDetails.builder()
                        .offenderNo("ABC")
                        .firstName("Fred")
                        .lastName("Bloggs")
                        .agencyDescription("Pentonville")
                        .agencyId("PVI")
                        .assignedLivingUnitDescription("cell 1")
                        .assignedLivingUnitId(200L)
                        .nonAssociations(List.of(
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDate.now(clock).minusDays(1).atStartOfDay())
                                        .expiryDate(LocalDate.now(clock).minusDays(1).atStartOfDay())
                                        .reasonCode("VIC")
                                        .reasonDescription("Victim")
                                        .typeCode("WING")
                                        .typeDescription("Do Not Locate on Same Wing")
                                        .comments("do not let these offenders share the same location")
                                        .authorisedBy("the boss")
                                        .offenderNonAssociation(OffenderNonAssociation.builder()
                                                .offenderNo(perpetrator.getNomsId())
                                                .firstName(perpetrator.getFirstName())
                                                .lastName(perpetrator.getLastName())
                                                .reasonCode("PER")
                                                .reasonDescription("recip - Perpetrator")
                                                .agencyDescription("Moorland")
                                                .agencyId("MDI")
                                                .assignedLivingUnitDescription("cell 2")
                                                .assignedLivingUnitId(123L)
                                                .build())
                                        .build()))
                        .build()
                );
    }

    @Test
    void retrieve_maps_associations_for_offender_with_all() {

        when(bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(victimsBooking.getOffender().getNomsId(), 1)).thenReturn(Optional.of(victimsBooking));
        when(offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(victimsBooking.getOffender().getNomsId())).thenReturn(filterNaList);

        assertThat(service.retrieveByOffenderNo(victimsBooking.getOffender().getNomsId(), false, false))
                .isEqualTo(OffenderNonAssociationDetails.builder()
                        .offenderNo("ABC")
                        .firstName("Fred")
                        .lastName("Bloggs")
                        .agencyDescription("Pentonville")
                        .agencyId("PVI")
                        .assignedLivingUnitDescription("cell 1")
                        .assignedLivingUnitId(200L)
                        .nonAssociations(List.of(
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDate.now(clock).minusDays(15).atStartOfDay())
                                        .expiryDate(LocalDate.now(clock).minusDays(1).atStartOfDay())
                                        .reasonCode("VIC")
                                        .reasonDescription("Victim")
                                        .typeCode("WING")
                                        .typeDescription("Do Not Locate on Same Wing")
                                        .comments("This is an inactive NA")
                                        .authorisedBy("the boss")
                                        .offenderNonAssociation(OffenderNonAssociation.builder()
                                                .offenderNo(perpetrator.getNomsId())
                                                .firstName(perpetrator.getFirstName())
                                                .lastName(perpetrator.getLastName())
                                                .reasonCode("PER")
                                                .reasonDescription("recip - Perpetrator")
                                                .agencyDescription("Moorland")
                                                .agencyId("MDI")
                                                .assignedLivingUnitDescription("cell 2")
                                                .assignedLivingUnitId(123L)
                                                .build())
                                        .build(),
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDate.now(clock).atStartOfDay())
                                        .reasonCode("VIC")
                                        .reasonDescription("Victim")
                                        .typeCode("WING")
                                        .typeDescription("Do Not Locate on Same Wing")
                                        .comments("Active NA")
                                        .authorisedBy("the boss")
                                        .offenderNonAssociation(OffenderNonAssociation.builder()
                                                .offenderNo(samePrisonOffender.getNomsId())
                                                .firstName(samePrisonOffender.getFirstName())
                                                .lastName(samePrisonOffender.getLastName())
                                                .reasonCode("PER")
                                                .reasonDescription("recip - Perpetrator")
                                                .agencyDescription("Pentonville")
                                                .agencyId("PVI")
                                                .assignedLivingUnitDescription("cell 2")
                                                .assignedLivingUnitId(201L)
                                                .build())
                                        .build(),
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDate.now(clock).minusDays(1).atStartOfDay())
                                        .expiryDate(LocalDate.now(clock).plusDays(1).atStartOfDay())
                                        .reasonCode("VIC")
                                        .reasonDescription("Victim")
                                        .typeCode("WING")
                                        .typeDescription("Do Not Locate on Same Wing")
                                        .comments("Different Prison")
                                        .authorisedBy("the boss")
                                        .offenderNonAssociation(OffenderNonAssociation.builder()
                                                .offenderNo(perpetrator.getNomsId())
                                                .firstName(perpetrator.getFirstName())
                                                .lastName(perpetrator.getLastName())
                                                .reasonCode("PER")
                                                .reasonDescription("recip - Perpetrator")
                                                .agencyDescription("Moorland")
                                                .agencyId("MDI")
                                                .assignedLivingUnitDescription("cell 2")
                                                .assignedLivingUnitId(123L)
                                                .build())
                                        .build()))
                        .build()
                );
    }

    @Test
    void retrieve_maps_associations_for_offender_with_active_same_prison() {

        when(bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(victimsBooking.getOffender().getNomsId(), 1)).thenReturn(Optional.of(victimsBooking));
        when(offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(victimsBooking.getOffender().getNomsId())).thenReturn(filterNaList);

        assertThat(service.retrieveByOffenderNo(victimsBooking.getOffender().getNomsId(), true, true))
                .isEqualTo(OffenderNonAssociationDetails.builder()
                        .offenderNo("ABC")
                        .firstName("Fred")
                        .lastName("Bloggs")
                        .agencyDescription("Pentonville")
                        .agencyId("PVI")
                        .assignedLivingUnitDescription("cell 1")
                        .assignedLivingUnitId(200L)
                        .nonAssociations(List.of(
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDate.now(clock).atStartOfDay())
                                        .reasonCode("VIC")
                                        .reasonDescription("Victim")
                                        .typeCode("WING")
                                        .typeDescription("Do Not Locate on Same Wing")
                                        .comments("Active NA")
                                        .authorisedBy("the boss")
                                        .offenderNonAssociation(OffenderNonAssociation.builder()
                                                .offenderNo(samePrisonOffender.getNomsId())
                                                .firstName(samePrisonOffender.getFirstName())
                                                .lastName(samePrisonOffender.getLastName())
                                                .reasonCode("PER")
                                                .reasonDescription("recip - Perpetrator")
                                                .agencyDescription("Pentonville")
                                                .agencyId("PVI")
                                                .assignedLivingUnitDescription("cell 2")
                                                .assignedLivingUnitId(201L)
                                                .build())
                                        .build()))
                        .build()
                );
    }

    @Test
    void retrieve_maps_associations_for_offender_with_all_same_prison() {

        when(bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(victimsBooking.getOffender().getNomsId(), 1)).thenReturn(Optional.of(victimsBooking));
        when(offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(victimsBooking.getOffender().getNomsId())).thenReturn(filterNaList);

        assertThat(service.retrieveByOffenderNo(victimsBooking.getOffender().getNomsId(), true, false))
                .isEqualTo(OffenderNonAssociationDetails.builder()
                        .offenderNo("ABC")
                        .firstName("Fred")
                        .lastName("Bloggs")
                        .agencyDescription("Pentonville")
                        .agencyId("PVI")
                        .assignedLivingUnitDescription("cell 1")
                        .assignedLivingUnitId(200L)
                        .nonAssociations(List.of(
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDate.now(clock).atStartOfDay())
                                        .reasonCode("VIC")
                                        .reasonDescription("Victim")
                                        .typeCode("WING")
                                        .typeDescription("Do Not Locate on Same Wing")
                                        .comments("Active NA")
                                        .authorisedBy("the boss")
                                        .offenderNonAssociation(OffenderNonAssociation.builder()
                                                .offenderNo(samePrisonOffender.getNomsId())
                                                .firstName(samePrisonOffender.getFirstName())
                                                .lastName(samePrisonOffender.getLastName())
                                                .reasonCode("PER")
                                                .reasonDescription("recip - Perpetrator")
                                                .agencyDescription("Pentonville")
                                                .agencyId("PVI")
                                                .assignedLivingUnitDescription("cell 2")
                                                .assignedLivingUnitId(201L)
                                                .build())
                                        .build()))
                        .build()
                );
    }

    @Test
    void retrieve_maps_associations_for_offender_with_any_prison_active_only() {

        when(bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(victimsBooking.getOffender().getNomsId(), 1)).thenReturn(Optional.of(victimsBooking));
        when(offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(victimsBooking.getOffender().getNomsId())).thenReturn(filterNaList);

        assertThat(service.retrieveByOffenderNo(victimsBooking.getOffender().getNomsId(), false, true))
                .isEqualTo(OffenderNonAssociationDetails.builder()
                        .offenderNo("ABC")
                        .firstName("Fred")
                        .lastName("Bloggs")
                        .agencyDescription("Pentonville")
                        .agencyId("PVI")
                        .assignedLivingUnitDescription("cell 1")
                        .assignedLivingUnitId(200L)
                        .nonAssociations(List.of(
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDate.now(clock).atStartOfDay())
                                        .reasonCode("VIC")
                                        .reasonDescription("Victim")
                                        .typeCode("WING")
                                        .typeDescription("Do Not Locate on Same Wing")
                                        .comments("Active NA")
                                        .authorisedBy("the boss")
                                        .offenderNonAssociation(OffenderNonAssociation.builder()
                                                .offenderNo(samePrisonOffender.getNomsId())
                                                .firstName(samePrisonOffender.getFirstName())
                                                .lastName(samePrisonOffender.getLastName())
                                                .reasonCode("PER")
                                                .reasonDescription("recip - Perpetrator")
                                                .agencyDescription("Pentonville")
                                                .agencyId("PVI")
                                                .assignedLivingUnitDescription("cell 2")
                                                .assignedLivingUnitId(201L)
                                                .build())
                                        .build(),
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDate.now(clock).minusDays(1).atStartOfDay())
                                        .expiryDate(LocalDate.now(clock).plusDays(1).atStartOfDay())
                                        .reasonCode("VIC")
                                        .reasonDescription("Victim")
                                        .typeCode("WING")
                                        .typeDescription("Do Not Locate on Same Wing")
                                        .comments("Different Prison")
                                        .authorisedBy("the boss")
                                        .offenderNonAssociation(OffenderNonAssociation.builder()
                                                .offenderNo(perpetrator.getNomsId())
                                                .firstName(perpetrator.getFirstName())
                                                .lastName(perpetrator.getLastName())
                                                .reasonCode("PER")
                                                .reasonDescription("recip - Perpetrator")
                                                .agencyDescription("Moorland")
                                                .agencyId("MDI")
                                                .assignedLivingUnitDescription("cell 2")
                                                .assignedLivingUnitId(123L)
                                                .build())
                                        .build()))
                        .build()
                );
    }


    @Test
    void retrieve_fails_when_offender_no_not_found() {
        final var prisonerNumber = "XXXXXX";
        when(bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(prisonerNumber, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveByOffenderNo(prisonerNumber, false, false))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Booking not found for offender %s.", prisonerNumber);
    }

    @Test
    void retrieve_handles_missing_optionals() {
        final List<uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail> nonAssociationDetails = List.of(
                uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail.builder()
                        .offender(victim)
                        .nsOffender(perpetrator)
                        .offenderBooking(victimsBooking)
                        .effectiveDate(LocalDate.now(clock).minusDays(1))
                        .expiryDate(LocalDate.now(clock).minusDays(1))
                        .comments("do not let these offenders share the same location")
                        .authorizedBy("the boss")
                        .nonAssociationReason(new NonAssociationReason("VIC", "Victim"))
                        .nonAssociationType(new NonAssociationType("WING", "Do Not Locate on Same Wing"))
                        .nonAssociation(uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociation.builder()
                                .offender(victim)
                                .nsOffender(perpetrator)
                                .nsOffenderBooking(OffenderBooking.builder()
                                        .location(AgencyLocation.builder()
                                                .id("LEI")
                                                .description("Leeds")
                                                .build())
                                        .build())
                                .build())
                        .build());

        when(bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(victimsBooking.getOffender().getNomsId(), 1)).thenReturn(Optional.of(victimBookingWithoutOptionals));
        when(offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(victimsBooking.getOffender().getNomsId())).thenReturn(nonAssociationDetails);

        assertThat(service.retrieveByOffenderNo(victimsBooking.getOffender().getNomsId(), false, false)).isEqualTo(OffenderNonAssociationDetails.builder()
                .offenderNo("ABC")
                .firstName("Fred")
                .lastName("Bloggs")
                .agencyDescription("Pentonville")
                .agencyId("PVI")
                .assignedLivingUnitDescription(null)
                .assignedLivingUnitId(null)
                .nonAssociations(List.of(
                        OffenderNonAssociationDetail.builder()
                                .effectiveDate(LocalDate.now(clock).minusDays(1).atStartOfDay())
                                .expiryDate(LocalDate.now(clock).minusDays(1).atStartOfDay())
                                .reasonCode("VIC")
                                .reasonDescription("Victim")
                                .typeCode("WING")
                                .typeDescription("Do Not Locate on Same Wing")
                                .comments("do not let these offenders share the same location")
                                .authorisedBy("the boss")
                                .offenderNonAssociation(OffenderNonAssociation.builder()
                                        .offenderNo(perpetrator.getNomsId())
                                        .firstName(perpetrator.getFirstName())
                                        .lastName(perpetrator.getLastName())
                                        .reasonCode(null)
                                        .reasonDescription(null)
                                        .agencyId("MDI")
                                        .agencyDescription("Moorland")
                                        .assignedLivingUnitDescription("cell 2")
                                        .assignedLivingUnitId(123L)
                                        .build())
                                .build()))
                .build());
    }
}
