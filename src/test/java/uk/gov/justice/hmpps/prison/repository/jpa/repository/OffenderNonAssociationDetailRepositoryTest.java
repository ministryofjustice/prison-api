package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class OffenderNonAssociationDetailRepositoryTest {

    @Autowired
    private OffenderNonAssociationDetailRepository nonAssociationDetailRepositoryRepository;

    @Autowired
    private OffenderBookingRepository bookingRepository;

    @Autowired
    private OffenderRepository offenderRepository;

    @Autowired
    private ReferenceCodeRepository<? extends ReferenceCode> referenceCodeRepository;

    private OffenderBooking booking;

    @BeforeEach
    void setup() {
        booking = bookingRepository.findById(-1L).orElseThrow();
    }

    @Test
    void find_non_association_details_by_offender_booking() {
        final var nonAssociationDetails = nonAssociationDetailRepositoryRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(booking.getOffender().getNomsId());

        assertThat(nonAssociationDetails).hasSize(2);

        final var expected = nonAssociationDetails.stream().findFirst().orElseThrow();

        assertThat(expected.getOffenderBooking()).isEqualTo(booking);
        assertThat(expected.getOffender()).isEqualTo(offender(-1001L));
        assertThat(expected.getNsOffender()).isEqualTo(offender(-1035L));
        assertThat(expected.getTypeSequence()).isEqualTo(1);
        assertThat(expected.getEffectiveDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(expected.getExpiryDate()).isEqualTo(LocalDate.of(2063, 6, 1));
        assertThat(expected.getNonAssociationReason()).isEqualTo(referenceCode(NonAssociationReason.DOMAIN, "VIC"));
        assertThat(expected.getNonAssociationType()).isEqualTo(referenceCode(NonAssociationType.DOMAIN, "WING"));
        assertThat(expected.getAuthorizedBy()).isEqualTo("Fred Bloggs");
        assertThat(expected.getComments()).isEqualTo("a --> b");
        assertThat(expected.getRecipNonAssociationReason()).isEqualTo(referenceCode(NonAssociationReason.DOMAIN, "BUL"));
        assertThat(expected.getNonAssociation().getOffender()).isEqualTo(offender(-1001L));
        assertThat(expected.getNonAssociation().getNsOffender()).isEqualTo(offender(-1035L));
        assertThat(expected.getNonAssociation().getNonAssociationReason()).isEqualTo(referenceCode(NonAssociationReason.DOMAIN, "VIC"));
        assertThat(expected.getNonAssociation().getRecipNonAssociationReason()).isEqualTo(referenceCode(NonAssociationReason.DOMAIN, "PER"));
    }

    private Offender offender(final long id) {
        return offenderRepository.findById(id).orElseThrow();
    }

    private ReferenceCode referenceCode(final String domain, final String code) {
        return referenceCodeRepository.findById(new ReferenceCode.Pk(domain, code)).orElseThrow();
    }
}
