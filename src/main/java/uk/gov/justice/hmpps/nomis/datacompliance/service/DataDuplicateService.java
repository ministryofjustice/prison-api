package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataDuplicateService {

    private final DataComplianceEventPusher dataComplianceEventPusher;

    public void checkForDataDuplicates(final String offenderNo, final Long retentionCheckId) {

        // TODO GDPR-110 Implement data duplicate check

        dataComplianceEventPusher.sendDataDuplicateResult(DataDuplicateResult.builder()
                .offenderIdDisplay(offenderNo)
                .retentionCheckId(retentionCheckId)
                .build());
    }
}
