package net.syscon.elite.repository;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.UpdateAlert;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.assertj.core.groups.Tuple;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class InmateAlertRepositoryTest {

    @Autowired
    private InmateAlertRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testGetInmateAlertsByOffenderNos() {
        final var alerts = repository.getInmateAlertsByOffenderNos(null, List.of("A1234AA", "A1234AG"), true, null, null, Order.ASC);

        assertThat(alerts).asList().extracting("bookingId", "alertId", "offenderNo", "alertType", "alertCode", "comment", "dateExpires", "active", "expired")
                .containsExactly(
                        Tuple.tuple(-7L, 1L, "A1234AG", "V", "VOP", "Alert Text 7", null, true, false),
                        Tuple.tuple(-1L, 1L, "A1234AA", "X", "XA", "Alert Text 1-1", null, true, false),
                        Tuple.tuple(-1L, 2L, "A1234AA", "H", "HC", "Alert Text 1-2", null, true, false),
                        Tuple.tuple(-1L, 3L, "A1234AA", "R", "RSS", "Inactive Alert", LocalDate.now(), false, true)
                );
    }

    @Test
    public void testGetInmateAlertsByOffenderNosOrdered() {
        final var alerts = repository.getInmateAlertsByOffenderNos(null, List.of("A1234AA", "A1234AG"), false, null, "alertType", Order.ASC);

        assertThat(alerts).asList().extracting("bookingId", "alertId", "offenderNo", "alertType", "expired")
                .containsExactly(
                        Tuple.tuple(-1L, 2L, "A1234AA", "H", false),
                        Tuple.tuple(-1L, 3L, "A1234AA", "R", true),
                        Tuple.tuple(-7L, 1L, "A1234AG", "V", false),
                        Tuple.tuple(-1L, 1L, "A1234AA", "X", false));
    }

    @Test
    public void testGetInmateAlertsByOffenderNosQuery() {
        final var alerts = repository.getInmateAlertsByOffenderNos(null, List.of("A1234AA", "A1234AG"), false, "alertCode:eq:'XA',or:alertCode:eq:'RSS'", null, Order.ASC);

        assertThat(alerts).asList().extracting("bookingId", "alertId", "offenderNo", "alertCode", "expired")
                .containsExactly(
                        Tuple.tuple(-1L, 1L, "A1234AA", "XA", false),
                        Tuple.tuple(-1L, 3L, "A1234AA", "RSS", true));
    }

    @Test
    public void testThatAnAlertGetsCreated() {
        final var bookingId = -10L;
        final var latestAlertSeq = repository.createNewAlert("ITAG_USER",bookingId, CreateAlert
                        .builder()
                        .alertDate(LocalDate.now())
                        .alertType("X")
                        .alertCode("XX")
                        .comment("Poor behaviour")
                        .build());


        final var alert = repository.getInmateAlerts(bookingId, latestAlertSeq).orElse(Alert.builder().build());

        assertThat(alert)
                .extracting( "alertId", "alertType", "alertCode", "comment", "expired")
                .contains( latestAlertSeq, "X", "XX", "Poor behaviour", false);

    }

    @Test
    public void testThatAnAlertGetsUpdated() {
        final var bookingId = -14L;
        final var alertSeq = 1L;
        final var expiryDate = LocalDate.now();
        repository.updateAlert("ITAG_USER", bookingId, alertSeq, UpdateAlert
                .builder()
                .expiryDate(expiryDate)
                .alertStatus("INACTIVE")
                .build());


        final var alert = repository.getInmateAlerts(bookingId, alertSeq).orElse(Alert.builder().build());

        assertThat(alert)
                .extracting( "alertId", "comment", "dateExpires", "active", "expired")
                .contains( alertSeq, "Test alert for expiry", expiryDate, false, true);

    }
}
