package net.syscon.elite.service.impl;

import lombok.val;
import net.syscon.elite.api.model.adjudications.Adjudication;
import net.syscon.elite.api.model.adjudications.AdjudicationDetail;
import net.syscon.elite.api.model.adjudications.AdjudicationOffence;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.AdjudicationsRepository;
import net.syscon.elite.service.AdjudicationSearchCriteria;
import net.syscon.elite.service.AdjudicationService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdjudicationServiceImplTest {

    @Mock private AdjudicationsRepository adjudicationsRepository;
    @Mock private BookingService bookingService;
    private AdjudicationService adjudicationService;

    @Before
    public void setup() {
        adjudicationService = new AdjudicationServiceImpl(adjudicationsRepository, bookingService);
    }

    @Test
    public void findAdjudications() {

        val adjudication = Adjudication.builder().build();
        val expectedResult = new Page<>(List.of(adjudication), 1, new PageRequest());
        val criteria = AdjudicationSearchCriteria.builder().offenderNumber("OFF-1").build();

        when(adjudicationsRepository.findAdjudications(any())).thenReturn(expectedResult);

        assertThat(adjudicationService.findAdjudications(criteria)).isEqualTo(expectedResult);

        verify(bookingService).verifyCanViewLatestBooking(criteria.getOffenderNumber());
    }

    @Test
    public void findAdjudicationDetails() {

        val dbResult = AdjudicationDetail.builder().establishment("MOORLANDS (HMP)").build();
        val expectedResult = AdjudicationDetail.builder().establishment("Moorlands (HMP)").build();

        when(adjudicationsRepository.findAdjudicationDetails(any(), anyLong())).thenReturn(Optional.of(dbResult));

        assertThat(adjudicationService.findAdjudication("OFF-1", -1)).isEqualTo(expectedResult);

        verify(bookingService).verifyCanViewLatestBooking("OFF-1");
        verify(adjudicationsRepository).findAdjudicationDetails("OFF-1", -1);
    }

    @Test
    public void cannotFindAdjudicationDetails() {

        assertThatThrownBy(() -> adjudicationService.findAdjudication("OFF-1", -1))
                .isInstanceOf(EntityNotFoundException.class);

        verify(bookingService).verifyCanViewLatestBooking("OFF-1");
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