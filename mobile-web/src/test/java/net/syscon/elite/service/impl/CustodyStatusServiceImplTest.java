package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CustodyStatus;
import net.syscon.elite.repository.CustodyStatusRecord;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.service.CustodyStatusService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link CustodyStatusServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustodyStatusServiceImplTest {

    @Test
    public void canRetrieveCustodyStatusOfExistingOffenderFromRepository() {
        String simpleOffenderNo = UUID.randomUUID().toString();

        CustodyStatusRepository custodyStatusRepository = mock(CustodyStatusRepository.class);

        when(custodyStatusRepository.getCustodyStatusRecord(simpleOffenderNo))
                .thenReturn(Optional.of(CustodyStatusRecord
                        .builder()
                        .offender_id_display(simpleOffenderNo)
                        .build()));

        CustodyStatusService service = new CustodyStatusServiceImpl(custodyStatusRepository);

        CustodyStatus custodyStatus = service.getCustodyStatus(simpleOffenderNo);

        assertEquals("returns a Custody Status for an existing offender", custodyStatus.getOffenderNo(), simpleOffenderNo);
    }


}