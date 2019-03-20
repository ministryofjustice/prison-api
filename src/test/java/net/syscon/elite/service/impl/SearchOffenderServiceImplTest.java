package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.support.SearchOffenderRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchOffenderServiceImplTest {

    @Mock
    BookingService bookingService;
    @Mock
    UserService userService;
    @Mock
    InmateRepository inmateRepository;
    @Mock
    AuthenticationFacade authenticationFacade;

    @Test
    public void testFindAssessmentsCorrectlyBatchesQueries() {
        final var locationTypeGranularity = "WING";
        final var offenderNoRegex = "^[A-Za-z]\\d{4}[A-Za-z]{2}$}";
        final int maxBatchSize = 1;

        final var bookings = List.of(
                OffenderBooking.builder().firstName("firstName1").bookingId(1L).bookingNo("1").build(),
                OffenderBooking.builder().firstName("firstName2").bookingId(2L).bookingNo("2").build()
        );

        when(inmateRepository.searchForOffenderBookings(anySet(), isNull(), eq("FIRSTNAME"), isNull(), eq("LEI"),
                isNull(), eq(locationTypeGranularity), any())).thenReturn(new Page<>(bookings, bookings.size(), 0, bookings.size()));

        final var service = new SearchOffenderServiceImpl(bookingService, userService, inmateRepository, authenticationFacade,
                locationTypeGranularity, offenderNoRegex, maxBatchSize);

        service.findOffenders(SearchOffenderRequest.builder().keywords("firstName").locationPrefix("LEI").returnCategory(true).build());

        verify(inmateRepository, times(1)).findAssessments(eq(List.of(1L)), eq("CATEGORY"), anySet());
        verify(inmateRepository, times(1)).findAssessments(eq(List.of(2L)), eq("CATEGORY"), anySet());
    }
}
