package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class ScheduledAppointmentRepositoryTest {

    @Autowired
    private ScheduledAppointmentRepository scheduledAppointmentRepository;

    @Test
    public void getAppointments() {
      final var appointments = scheduledAppointmentRepository.findByAgencyIdAndEventDate("LEI", LocalDate.now());

      assertThat(appointments).isNotEmpty();
    }
}
