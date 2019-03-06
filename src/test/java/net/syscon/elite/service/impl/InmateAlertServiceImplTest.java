package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Arrays;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class InmateAlertServiceImplTest {
    @Mock
    private InmateAlertRepository inmateAlertRepository;

    @InjectMocks
    private InmateAlertServiceImpl serviceToTest;

    @Test
    public void testCorrectNumberAlertReturned() {
        final var alerts = createAlerts();

        Mockito.when(inmateAlertRepository.getInmateAlerts(eq(-1L), any(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = serviceToTest.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).hasSize(alerts.getItems().size());
    }

    @Test
    public void testCorrectExpiredAlerts() {
        final var alerts = createAlerts();

        Mockito.when(inmateAlertRepository.getInmateAlerts(eq(-1L), isNull(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = serviceToTest.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).extracting("expired").containsSequence(false, false, true, true, false);
    }

    private Page<Alert> createAlerts() {
        final var now = LocalDate.now();

        final var alerts = Arrays.asList(
                buildAlert(-1L, now.minusMonths(1), now.plusDays(2)),
                buildAlert(-2L, now.minusMonths(2), now.plusDays(1)),
                buildAlert(-3L, now.minusMonths(3), now),
                buildAlert(-4L, now.minusMonths(4), now.minusDays(1)),
                buildAlert(-5L, now.minusMonths(5), null)
            );

        return new Page<>(alerts, 5, 0, 10);
    }

    private Alert buildAlert(final long id, final LocalDate dateCreated, final LocalDate dateExpires) {
        return Alert.builder()
                .alertId(id)
                .alertType(format("ALERTYPE%d", id))
                .alertCode(format("ALERTCODE%d", id))
                .comment(format("This is a comment %d", id))
                .dateCreated(dateCreated)
                .dateExpires(dateExpires)
                .build();
    }
}
