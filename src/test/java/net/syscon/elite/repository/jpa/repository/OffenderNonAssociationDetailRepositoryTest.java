package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

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

    @Test
    void find_non_association_details_by_offender_booking() {
        assertThat(nonAssociationDetailRepositoryRepository.findAllByOffenderBooking_BookingId(-1L)).hasSize(1);
    }
}
