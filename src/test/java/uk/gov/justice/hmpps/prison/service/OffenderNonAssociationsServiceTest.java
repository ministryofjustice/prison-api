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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderNonAssociationsServiceTest {

    private final Offender victim = Offender.builder()
            .nomsId("ABC")
            .firstName("Fred")
            .lastName("Bloggs")
            .build();

    private final OffenderBooking victimsBooking = OffenderBooking.builder()
            .bookingId(1L)
            .offender(victim)
            .location(AgencyLocation.builder()
                    .description("Pentonville")
                    .build())
            .assignedLivingUnit(AgencyInternalLocation.builder()
                    .description("cell 1")
                    .build())
            .build();

    private final Offender perpetrator = Offender.builder()
            .nomsId("DEF")
            .firstName("Joseph")
            .lastName("Bloggs")
            .build();

    @Mock
    private OffenderNonAssociationDetailRepository nonAssociationDetailRepository;

    @Mock
    private OffenderBookingRepository bookingRepository;

    private OffenderNonAssociationsService service;

    @BeforeEach
    void setup() {
        service = new OffenderNonAssociationsService(nonAssociationDetailRepository, bookingRepository);
    }

    @Test
    void retrieve_maps_no_non_associations_for_booking() {
        when(bookingRepository.findById(victimsBooking.getBookingId())).thenReturn(Optional.of(victimsBooking));

        when(nonAssociationDetailRepository.findAllByOffenderBooking_BookingId(1L)).thenReturn(Collections.emptyList());

        assertThat(service.retrieve(1L)).isEqualTo(OffenderNonAssociationDetails.builder()
                .offenderNo("ABC")
                .firstName("Fred")
                .lastName("Bloggs")
                .agencyDescription("Pentonville")
                .assignedLivingUnitDescription("cell 1")
                .nonAssociations(List.of())
                .build());
    }

    @Test
    void retrieve_maps_associations_for_booking() {
        when(bookingRepository.findById(victimsBooking.getBookingId())).thenReturn(Optional.of(victimsBooking));

        when(nonAssociationDetailRepository.findAllByOffenderBooking_BookingId(victimsBooking.getBookingId())).thenReturn(List.of(
                uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail.builder()
                        .offender(victim)
                        .nsOffender(perpetrator)
                        .offenderBooking(victimsBooking)
                        .effectiveDate(LocalDateTime.of(2020, 7, 3, 12, 0, 0))
                        .expiryDate(LocalDateTime.of(2020, 12, 3, 12, 0, 0))
                        .comments("do not let these offenders share the same location")
                        .authorizedBy("the boss")
                        .nonAssociationReason(new NonAssociationReason("VIC", "Victim"))
                        .nonAssociationType(new NonAssociationType("WING", "Do Not Locate on Same Wing"))
                        .nonAssociation(uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociation.builder()
                                .offender(victim)
                                .nsOffender(perpetrator)
                                .nonAssociationReason(new NonAssociationReason("PER", "Perpetrator"))
                                .recipNonAssociationReason(new NonAssociationReason("PER", "recip - Perpetrator"))
                                .nsOffenderBooking(OffenderBooking.builder()
                                        .location(AgencyLocation.builder()
                                                .description("Moorland")
                                                .build())
                                        .assignedLivingUnit(AgencyInternalLocation.builder()
                                                .description("cell 2")
                                                .build())
                                        .build())
                                .build())
                        .build()));

        assertThat(service.retrieve(victimsBooking.getBookingId()))
                .isEqualTo(OffenderNonAssociationDetails.builder()
                        .offenderNo("ABC")
                        .firstName("Fred")
                        .lastName("Bloggs")
                        .agencyDescription("Pentonville")
                        .assignedLivingUnitDescription("cell 1")
                        .nonAssociations(List.of(
                                OffenderNonAssociationDetail.builder()
                                        .effectiveDate(LocalDateTime.of(2020, 7, 3, 12, 0, 0))
                                        .expiryDate(LocalDateTime.of(2020, 12, 3, 12, 0, 0))
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
                                                .assignedLivingUnitDescription("cell 2")
                                                .build())
                                        .build()))
                        .build()
                );
    }

    @Test
    void retrieve_fails_when_booking_not_found() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieve(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Offender booking with id %d not found.", 99L);
    }
}
