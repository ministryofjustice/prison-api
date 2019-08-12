package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.security.AuthenticationFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Arrays;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InmateAlertServiceImplTest {
    @Mock
    private InmateAlertRepository inmateAlertRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private InmateAlertServiceImpl serviceToTest;

    @Test
    public void testCorrectNumberAlertReturned() {
        final var alerts = createAlerts();

        when(inmateAlertRepository.getInmateAlerts(eq(-1L), any(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = serviceToTest.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).hasSize(alerts.getItems().size());
    }

    @Test
    public void testCorrectExpiredAlerts() {
        final var alerts = createAlerts();

        when(inmateAlertRepository.getInmateAlerts(eq(-1L), isNull(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = serviceToTest.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).extracting("expired").containsSequence(false, false, true, true, false);
    }

    @Test
    public void testThatAlertRepository_CreateAlertIsCalledWithCorrectParams() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.createNewAlert(anyString(),anyLong(), any())).thenReturn(1L);

        final var alertId = serviceToTest.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());

        assertThat(alertId).isEqualTo(1L);

        verify(inmateAlertRepository).createNewAlert("ITAG_USER",-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());
    }

    @Test
    public void testThatAlertDate_SevenDaysInThePastThrowsException() {
        assertThat(catchThrowable(() -> {
            serviceToTest.createNewAlert(-1L, CreateAlert
                    .builder().alertDate(LocalDate.now().minusDays(8)).build());
        })).as("Alert date cannot go back more than seven days.").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testThatAlertDate_InTheFutureThrowsException() {
        assertThat(catchThrowable(() -> {
            serviceToTest.createNewAlert(-1L, CreateAlert
                    .builder().alertDate(LocalDate.now().plusDays(1)).build());
        })).as("Alert date cannot be in the future.").isInstanceOf(IllegalArgumentException.class);
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
