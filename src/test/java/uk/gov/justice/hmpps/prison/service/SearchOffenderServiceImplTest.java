package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingSearchRequest;
import uk.gov.justice.hmpps.prison.service.support.SearchOffenderRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchOffenderServiceImplTest {

    @Mock
    BookingService bookingService;
    @Mock
    UserService userService;
    @Mock
    InmateRepository inmateRepository;

    @Test
    public void testFindOffenders_findAssessmentsCorrectlyBatchesQueries() {
        final var offenderNoRegex = "^[A-Za-z]\\d{4}[A-Za-z]{2}$}";
        final int maxBatchSize = 1;

        final var bookings = List.of(
                OffenderBooking.builder().firstName("firstName1").bookingId(1L).bookingNo("1").build(),
                OffenderBooking.builder().firstName("firstName2").bookingId(2L).bookingNo("2").build()
        );

        when(inmateRepository.searchForOffenderBookings(isA(OffenderBookingSearchRequest.class))).thenReturn(new Page<>(bookings, bookings.size(), 0, bookings.size()));

        final var service = new SearchOffenderService(bookingService, userService, inmateRepository, offenderNoRegex, maxBatchSize);

        service.findOffenders(SearchOffenderRequest.builder().keywords("firstName").locationPrefix("LEI").returnCategory(true).build());

        verify(inmateRepository).findAssessments(eq(List.of(1L)), anyString(), anySet());
        verify(inmateRepository).findAssessments(eq(List.of(2L)), anyString(), anySet());
    }
}
