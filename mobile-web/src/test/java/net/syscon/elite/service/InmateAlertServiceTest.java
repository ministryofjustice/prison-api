package net.syscon.elite.service;


import net.syscon.elite.persistence.InmateAlertRepository;
import net.syscon.elite.service.impl.InmateAlertServiceImpl;
import net.syscon.elite.web.api.model.Alert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static net.syscon.elite.web.api.resource.BookingResource.Order.asc;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class InmateAlertServiceTest {

    private InmatesAlertService serviceToTest;

    @Mock
    private InmateAlertRepository inmateAlertRepository;

    @Before
    public void setup() {
        serviceToTest = new InmateAlertServiceImpl(inmateAlertRepository);
    }


    @Test
    public void testCorrectNumberAlertReturned() {
        List<Alert> alerts = createAlerts();
        Mockito.when(inmateAlertRepository.getInmateAlert(eq("bookingId"), eq(""), eq("alertCode"), eq(asc), eq(0), eq(10))
        ).thenReturn(alerts);
        final List<Alert> returnedAlerts = serviceToTest.getInmateAlerts("bookingId", "", "alertCode", asc, 0, 10);
        assertThat(returnedAlerts).hasSize(alerts.size());
    }

    @Test
    public void testCorrectExpiredAlerts() {
        List<Alert> alerts = createAlerts();
        Mockito.when(inmateAlertRepository.getInmateAlert(eq("bookingId"), eq(""), eq("alertCode"), eq(asc), eq(0), eq(10))
        ).thenReturn(alerts);
        final List<Alert> returnedAlerts = serviceToTest.getInmateAlerts("bookingId", "", "alertCode", asc, 0, 10);
        assertThat(returnedAlerts).extracting("expired").containsSequence(false, false, true, true, false);
    }

    private List<Alert> createAlerts() {
        List<Alert> alerts = new ArrayList<>();
        DateTime now = new DateTime(DateTimeZone.UTC).withTimeAtStartOfDay();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
        alerts.add(new Alert(1L, "ALERTYPE1", "ALERTCODE1", "This is a comment 1", now.minusMonths(1).toString(dtf), now.plusDays(2).toString(dtf), null));
        alerts.add(new Alert(2L, "ALERTYPE2", "ALERTCODE2", "This is a comment 2", now.minusMonths(2).toString(dtf), now.plusDays(1).toString(dtf), null));
        alerts.add(new Alert(3L, "ALERTYPE3", "ALERTCODE3", "This is a comment 3", now.minusMonths(3).toString(dtf), now.toString(dtf), null));
        alerts.add(new Alert(4L, "ALERTYPE4", "ALERTCODE4", "This is a comment 4", now.minusMonths(4).toString(dtf), now.minusDays(1).toString(dtf), null));
        alerts.add(new Alert(5L, "ALERTYPE5", "ALERTCODE5", "This is a comment 5", now.minusMonths(5).toString(dtf), null, null));
        return alerts;
    }
}
