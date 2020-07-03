package uk.gov.justice.hmpps.prison.service;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Hearing;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.AdjudicationsRepository;
import uk.gov.justice.hmpps.prison.repository.AgencyRepository;
import uk.gov.justice.hmpps.prison.repository.LocationRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

@ExtendWith(MockitoExtension.class)
public class AdjudicationServiceImplTest {

    @Mock
    private AdjudicationsRepository adjudicationsRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private BookingService bookingService;
    private AdjudicationService adjudicationService;

    @BeforeEach
    public void setup() {
        adjudicationService = new AdjudicationService(adjudicationsRepository, agencyRepository, locationRepository, bookingService);
    }

    @Test
    public void findAdjudications() {

        val adjudication = Adjudication.builder().build();
        val expectedResult = new Page<>(List.of(adjudication), 1, new PageRequest());
        val criteria = AdjudicationSearchCriteria.builder().offenderNumber("OFF-1").build();

        when(adjudicationsRepository.findAdjudications(any())).thenReturn(expectedResult);

        assertThat(adjudicationService.findAdjudications(criteria)).isEqualTo(expectedResult);

        verify(bookingService).verifyCanViewSensitiveBookingInfo(criteria.getOffenderNumber());
    }

    @Test
    public void findAdjudicationDetails() {

        val dbResult = AdjudicationDetail.builder()
                .agencyId("MDI")
                .internalLocationId(1L)
                .hearing(Hearing.builder().internalLocationId(2L).build())
                .build();

        val expectedResult = AdjudicationDetail.builder()
                .agencyId("MDI")
                .establishment("Moorlands (HMP)")
                .internalLocationId(1)
                .interiorLocation("Wing 1")
                .hearing(Hearing.builder()
                        .internalLocationId(2L)
                        .establishment("Moorlands (HMP)")
                        .location("Hearing Room 1")
                        .build())
                .build();

        val incidentLocation = Location.builder().agencyId("MDI").description("MDI-AA-WING-1").userDescription("Wing 1").build();
        val hearingLocation = Location.builder().agencyId("MDI").description("MDI-AA-CR-1").userDescription("Hearing Room 1").build();
        val agency = Agency.builder().description("MOORLANDS (HMP)").build();

        when(agencyRepository.findAgency("MDI", ALL, null)).thenReturn(Optional.of(agency));
        when(locationRepository.findLocation(1L, ALL)).thenReturn(Optional.of(incidentLocation));
        when(locationRepository.findLocation(2L, ALL)).thenReturn(Optional.of(hearingLocation));

        when(adjudicationsRepository.findAdjudicationDetails(any(), anyLong())).thenReturn(Optional.of(dbResult));

        assertThat(adjudicationService.findAdjudication("OFF-1", -1)).isEqualTo(expectedResult);

        verify(adjudicationsRepository).findAdjudicationDetails("OFF-1", -1);
    }

    @Test
    public void cannotFindAdjudicationDetails() {

        assertThatThrownBy(() -> adjudicationService.findAdjudication("OFF-1", -1))
                .isInstanceOf(EntityNotFoundException.class);

        verify(adjudicationsRepository).findAdjudicationDetails("OFF-1", -1);
    }

    @Test
    public void adjudicationOffences() {

        val expectedResult = List.of(AdjudicationOffence.builder().build());

        when(adjudicationsRepository.findAdjudicationOffences(anyString())).thenReturn(expectedResult);

        assertThat(adjudicationService.findAdjudicationsOffences("OFF-1")).isEqualTo(expectedResult);

        verify(adjudicationsRepository).findAdjudicationOffences("OFF-1");
    }

    @Test
    public void adjudicationAgencies() {

        val dbResult = List.of(Agency.builder().description("MOORLANDS (HMP)").build());
        val expectedResult = List.of(Agency.builder().description("Moorlands (HMP)").build());

        when(adjudicationsRepository.findAdjudicationAgencies(anyString())).thenReturn(dbResult);

        assertThat(adjudicationService.findAdjudicationAgencies("OFF-1")).isEqualTo(expectedResult);

        verify(adjudicationsRepository).findAdjudicationAgencies("OFF-1");
    }
}
