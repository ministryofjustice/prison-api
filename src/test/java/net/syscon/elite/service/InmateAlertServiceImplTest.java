package net.syscon.elite.service;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.AlertChanges;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.security.AuthenticationFacade;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InmateAlertServiceImplTest {
    @Mock
    private InmateAlertRepository inmateAlertRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private ReferenceDomainService referenceDomainService;

    private InmateAlertService service;

    @BeforeEach
    public void setUp() {
        service = new InmateAlertService(
                inmateAlertRepository,
                authenticationFacade,
                telemetryClient,
                referenceDomainService);
    }

    @Test
    public void testCorrectNumberAlertReturned() {
        final var alerts = createAlerts();

        when(inmateAlertRepository.getAlerts(eq(-1L), any(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = service.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).hasSize(alerts.getItems().size());
    }

    @Test
    public void testCorrectExpiredAlerts() {
        final var alerts = createAlerts();

        when(inmateAlertRepository.getAlerts(eq(-1L), isNull(), any(), any(), eq(0L), eq(10L))).thenReturn(alerts);

        final var returnedAlerts = service.getInmateAlerts(-1L, null, null, null, 0, 10);

        assertThat(returnedAlerts.getItems()).extracting("expired").containsSequence(false, false, true, true, false);
    }

    @Test
    public void testAlertRepository_CreateAlertIsCalledWithCorrectParams() {
        final ReferenceCode alertType = getAlertReferenceCode();

        when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), anyBoolean()))
                .thenReturn(Optional.of(alertType));

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.createNewAlert(anyLong(), any())).thenReturn(1L);

        final var alertId = service.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());

        assertThat(alertId).isEqualTo(1L);

        verify(inmateAlertRepository).createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());
    }

    @Test
    public void testAlertDate_SevenDaysInThePastThrowsException() {
        assertThat(catchThrowable(() -> {
            service.createNewAlert(-1L, CreateAlert
                    .builder().alertDate(LocalDate.now().minusDays(8)).build());
        })).as("Alert date cannot go back more than seven days.").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testAlertDate_InTheFutureThrowsException() {
        assertThat(catchThrowable(() -> {
            service.createNewAlert(-1L, CreateAlert
                    .builder().alertDate(LocalDate.now().plusDays(1)).build());
        })).as("Alert date cannot be in the future.").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testAlertRepository_ExpireAlertIsCalledWithCorrectParams() {
        final var alert = Alert.builder()
                .alertId(4L)
                .bookingId(-1L)
                .alertType(format("ALERTYPE%d", 1L))
                .alertCode(format("ALERTCODE%d", 1L))
                .active(true)
                .comment(format("This is a comment %d", 1L))
                .dateCreated(LocalDate.now())
                .build();

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");

        when(inmateAlertRepository.updateAlert(anyLong(), anyLong(), any())).thenReturn(Optional.of(alert));
        when(inmateAlertRepository.getAlert(anyLong(), anyLong())).thenReturn(Optional.of(alert));

        final var updatedAlert = service.updateAlert(-1L, 4L, AlertChanges
                .builder()
                .expiryDate(LocalDate.now())
                .build());

        assertThat(updatedAlert).isEqualTo(alert);

        verify(inmateAlertRepository).updateAlert(-1L, 4L, AlertChanges
                .builder()
                .expiryDate(LocalDate.now())
                .build());
    }

    @Test
    public void testExceptionIsThrown_WhenExpiryDateAndCommentAreNull() {
        assertThatThrownBy(() -> service.updateAlert(1L, 2L, AlertChanges.builder().build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Please provide an expiry date, or a comment");
    }

    @Test
    public void testThatOnlyTheCommentTextGetsUpdate_WhenExpiryIsNull() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");

        when(inmateAlertRepository.updateAlert(anyLong(), anyLong(), any())).thenReturn(Optional.of(Alert.builder().build()));

        service.updateAlert(1L, 2L, AlertChanges.builder().comment("Test").build());

        verify(inmateAlertRepository).updateAlert(1L, 2L, AlertChanges.builder().comment("Test").build());
    }

    @Test
    public void testThatTelemetryFires_WhenCommentIsUpdated() {

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");

        when(inmateAlertRepository.updateAlert(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(Alert.builder().alertCode("X").alertType("XX").build()));

        service.updateAlert(1L, 2L, AlertChanges.builder().comment("Test").build());

        verify(inmateAlertRepository).updateAlert(1L, 2L, AlertChanges.builder().comment("Test").build());

        verify(telemetryClient).trackEvent("Alert updated", Map.of(
                "bookingId", "1",
                "alertSeq", "2",
                "comment", "Comment text updated",
                "updated_by", "ITAG_USER"
        ), null);
    }

    @Test
    public void testYouCannotCreateDuplicateAlerts() {
        final ReferenceCode alertType = getAlertReferenceCode();
        final var originalAlert = Alert.builder().alertCode("X").alertType("XX").build();

        when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), anyBoolean()))
                .thenReturn(Optional.of(alertType));

        when(inmateAlertRepository.getActiveAlerts(anyLong())).thenReturn(List.of(originalAlert));

        assertThatThrownBy(() -> service.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert already exists for this offender.");
    }

    @Test
    public void testTelemetryEventHasBeenRaised_OnAlertCreation() {
        final ReferenceCode alertType = getAlertReferenceCode();

        when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), anyBoolean()))
                .thenReturn(Optional.of(alertType));

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.createNewAlert(anyLong(), any())).thenReturn(1L);

        final var alertId = service.createNewAlert(-1L, CreateAlert
                .builder()
                .alertCode("X")
                .alertType("XX")
                .alertDate(LocalDate.now().atStartOfDay().toLocalDate())
                .comment("comment1")
                .build());

        verify(telemetryClient).trackEvent("Alert created", Map.of(
                "alertSeq", String.valueOf(alertId),
                "alertDate", LocalDate.now().atStartOfDay().toLocalDate().toString(),
                "alertCode", "X",
                "alertType", "XX",
                "bookingId", "-1",
                "created_by", "ITAG_USER"
        ), null);
    }

    @Test
    public void testTelemetryEventHasBeenRaised_OnAlertExpire() {

        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.getAlert(anyLong(), anyLong())).thenReturn(Optional.of(Alert.builder().active(true).build()));
        when(inmateAlertRepository.updateAlert(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(Alert.builder().build()));

        service.updateAlert(-1L, -2L, AlertChanges
                .builder()
                .expiryDate(LocalDate.now())
                .build());

        verify(telemetryClient).trackEvent("Alert updated", Map.of(
                "bookingId", "-1",
                "alertSeq", "-2",
                "expiryDate", LocalDate.now().atStartOfDay().toLocalDate().toString(),
                "updated_by", "ITAG_USER"
        ), null);
    }

    @Test
    public void testAlertTypeIsCorrect_BeforeCreating() {
        when(referenceDomainService.getReferenceCodeByDomainAndCode("ALERT", "X", true))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createNewAlert(-1L, CreateAlert.builder().alertType("X").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert type does not exist.");
    }

    @Test
    public void testAlertCodeIsCorrect_BeforeCreating() {
        final var alertType = new ReferenceCode();
        final var alertCode = new ReferenceCode();
        alertCode.setCode("A134");
        alertType.setSubCodes(List.of(alertCode));

        when(referenceDomainService.getReferenceCodeByDomainAndCode("ALERT", "X", true))
                .thenReturn(Optional.of(alertType));

        assertThatThrownBy(() ->
                service.createNewAlert(-1L, CreateAlert.builder().alertType("X").alertCode("XX").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert code does not exist.");
    }


    @Test
    public void testAlertCanNotBeMadeInactiveIfAlreadyInactive() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ITAG_USER");
        when(inmateAlertRepository.getAlert(anyLong(), anyLong())).thenReturn(Optional.of(Alert.builder().active(false).build()));

        assertThatThrownBy(() ->
                service.updateAlert(-14, 1, AlertChanges.builder().expiryDate(LocalDate.now()).build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert is already inactive.");
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

    @NotNull
    private ReferenceCode getAlertReferenceCode() {
        final var alertType = new ReferenceCode();
        alertType.setCode("XX");
        final var alertCode = new ReferenceCode();
        alertCode.setCode("X");
        alertType.setSubCodes(List.of(alertCode));
        return alertType;
    }
}
