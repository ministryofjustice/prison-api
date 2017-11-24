package net.syscon.elite.api.resource.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.resource.CustodyStatusResource;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.service.CustodyStatusService;
import net.syscon.elite.service.impl.CustodyStatusServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link CustodyStatusResourceImpl}.
 */
@RunWith(DataProviderRunner.class)
public class CustodyStatusResourceImplTest {

    private CustodyStatusService custodyStatusService = mock(CustodyStatusServiceImpl.class);

    private CustodyStatusResource resource = new CustodyStatusResourceImpl(custodyStatusService);

    private LocalDate nowDate = LocalDate.now();
    private LocalDate pastDate = LocalDate.of(2016, 07, 15);

    @Before
    public void init() {
        List<PrisonerCustodyStatus> nowRecords = ImmutableList.of(
                PrisonerCustodyStatus.builder()
                        .offenderNo("A")
                        .custodyStatusCode(CustodyStatusCode.ACTIVE_IN)
                        .build()
        );

        when(custodyStatusService.listCustodyStatuses(Lists.newArrayList(), nowDate, null))
                .thenReturn(nowRecords);

        List<PrisonerCustodyStatus> pastRecords = ImmutableList.of(
                PrisonerCustodyStatus.builder()
                        .offenderNo("A")
                        .custodyStatusCode(CustodyStatusCode.IN_TRANSIT)
                        .build()
        );

        when(custodyStatusService.listCustodyStatuses(Lists.newArrayList(), nowDate, null))
                .thenReturn(nowRecords);

        when(custodyStatusService.listCustodyStatuses(Lists.newArrayList(), pastDate, null))
                .thenReturn(pastRecords);
    }

    @Test
    public void canRetrievePastCustodyStatusWhenPastDateProvided() {
        CustodyStatusResource.GetPrisonerCustodyStatusesResponse response = resource.getPrisonerCustodyStatuses(
                Lists.newArrayList(), pastDate.toString(), null, null);

        List<PrisonerCustodyStatus> entity = (List<PrisonerCustodyStatus>) response.getEntity();

        assertEquals(CustodyStatusCode.IN_TRANSIT, entity.get(0).getCustodyStatusCode());
    }

    @Test
    public void canRetrieveCurrentCustodyStatusWhenNoDateProvided() {
        CustodyStatusResource.GetPrisonerCustodyStatusesResponse response = resource.getPrisonerCustodyStatuses(
                Lists.newArrayList(), null, null, null);

        List<PrisonerCustodyStatus> entity = (List<PrisonerCustodyStatus>) response.getEntity();

        assertEquals(CustodyStatusCode.ACTIVE_IN, entity.get(0).getCustodyStatusCode());
    }
}
