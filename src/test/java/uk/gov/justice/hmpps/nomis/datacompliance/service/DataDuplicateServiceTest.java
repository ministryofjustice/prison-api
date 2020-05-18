package uk.gov.justice.hmpps.nomis.datacompliance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataDuplicateServiceTest {

    @Mock
    private DataComplianceEventPusher dataComplianceEventPusher;

    private DataDuplicateService dataDuplicateService;

    @BeforeEach
    void setUp() {
        dataDuplicateService = new DataDuplicateService(dataComplianceEventPusher);
    }

    @Test
    void checkForDataDuplicates() {
        dataDuplicateService.checkForDataDuplicates("A1234AA", 123L);

        verify(dataComplianceEventPusher).sendDataDuplicateResult(DataDuplicateResult.builder()
                .offenderIdDisplay("A1234AA")
                .retentionCheckId(123L)
                .build());
    }
}
