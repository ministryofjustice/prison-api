package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderSentConditions;
import uk.gov.justice.hmpps.prison.PrisonApiServer;

import java.util.List;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = {PrisonApiServer.class})
class OffenderSentConditionsRepositoryTest {

    @Autowired
    OffenderSentConditionsRepository offenderSentConditionsRepository;

    @Test
    public void findChildRelatedConditionsByBookings(){

        final List<OffenderSentConditions> conditions = offenderSentConditionsRepository.findChildRelatedConditionsByBookings(of(-1L));

        assertThat(conditions).extracting(OffenderSentConditions::getOffenderSentConditionId).containsOnly(-1L);
        assertThat(conditions).extracting(OffenderSentConditions::getBookingId).containsOnly(-1L);
        assertThat(conditions).extracting(OffenderSentConditions::getGroomingFlag).containsOnly("Y");
        assertThat(conditions).extracting(OffenderSentConditions::getNoWorkWithUnderAge).containsOnly("N");
    }

}