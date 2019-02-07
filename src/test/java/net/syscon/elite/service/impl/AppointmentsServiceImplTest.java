package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.bulkappointments.*;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppointmentsServiceImplTest {
    private static final String USERNAME = "username";
    private static final String OFFENDER_NO = "A1234AX";
    private static final Long BOOKING_ID = 100L;
    private static final Long LOCATION_ID = 9090L;
    private static final String APPOINTMENT_TYPE = "AT";
    private static final LocalDateTime START_TIME = LocalDateTime.of(2017, 1, 1, 0, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2021, 1, 1, 0, 0);
    private static final String COMMENT = "C";

    private static final String DEFAULT_APPOINTMENT_TYPE = "DAT";
    private static final String DEFAULT_COMMENT = "DC";
    private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.of(2019, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_END_TIME = LocalDateTime.of(2019, 1, 1, 1, 0);
    private static final Long DEFAULT_LOCATION_ID = 10101L;

    @Mock
    private BookingService bookingService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private AppointmentsServiceImpl appointmentsService;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(authenticationFacade.getCurrentUsername()).thenReturn(USERNAME);
    }
}
