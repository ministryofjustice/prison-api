package net.syscon.elite.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import net.syscon.elite.api.model.MainSentence;
import net.syscon.elite.repository.SentenceRepository;
import net.syscon.elite.web.config.PersistenceConfigs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("nomis,nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class SentenceRepositoryTest {

    @Autowired
    private SentenceRepository repository;

    @Before
    public final void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public final void testGetMainSentence() {
        final MainSentence sentence = repository.getMainSentence(-1L);
        assertNotNull(sentence);
        assertEquals("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)", sentence.getMainOffenceDescription());
        assertEquals("6 months", sentence.getSentenceLength());
        assertEquals("2018-04-23", sentence.getReleaseDate().toString());
    }

    @Test
    public final void testGetMainSentenceInvalidBookingId() {
        final MainSentence sentence = repository.getMainSentence(1001L);
        assertNotNull(sentence);
        assertNull(sentence.getMainOffenceDescription());
        assertNull(sentence.getSentenceLength());
        assertNull(sentence.getReleaseDate());
    }
}
