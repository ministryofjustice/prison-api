package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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
        Page<Alert> alerts = createAlerts();

        Mockito.when(inmateAlertRepository.getInmateAlerts(anyLong(), anyString(), anyString(), any(), anyLong(), anyLong())).thenReturn(alerts);

        Page<Alert> returnedAlerts = serviceToTest.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).hasSize(alerts.getItems().size());
    }

    @Test
    public void testCorrectExpiredAlerts() {
        Page<Alert> alerts = createAlerts();

        Mockito.when(inmateAlertRepository.getInmateAlerts(anyLong(), anyString(), anyString(), any(), anyLong(), anyLong())).thenReturn(alerts);

        Page<Alert> returnedAlerts = serviceToTest.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).extracting("expired").containsSequence(false, false, true, true, false);
    }

    private Page<Alert> createAlerts() {
        LocalDate now = LocalDate.now();

        List<Alert> alerts = Arrays.asList(
                buildAlert(-1L, now.minusMonths(1), now.plusDays(2)),
                buildAlert(-2L, now.minusMonths(2), now.plusDays(1)),
                buildAlert(-3L, now.minusMonths(3), now),
                buildAlert(-4L, now.minusMonths(4), now.minusDays(1)),
                buildAlert(-5L, now.minusMonths(5), null)
            );

        return new Page<>(alerts, 5, 0, 10);
    }

    private Alert buildAlert(long id, LocalDate dateCreated, LocalDate dateExpires) {
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
