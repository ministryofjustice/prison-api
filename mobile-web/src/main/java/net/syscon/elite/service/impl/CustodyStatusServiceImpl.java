package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.repository.CustodyStatusRecord;
import net.syscon.elite.service.CustodyStatusCalculator;
import net.syscon.elite.service.CustodyStatusService;
import net.syscon.elite.service.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustodyStatusServiceImpl implements CustodyStatusService {
    private final CustodyStatusRepository custodyStatusRepository;

    private CustodyStatusCalculator calculator = new CustodyStatusCalculator();

    public CustodyStatusServiceImpl(CustodyStatusRepository custodyStatusRepository) {
        this.custodyStatusRepository = custodyStatusRepository;
    }

    @Override
    public PrisonerCustodyStatus getCustodyStatus(String offenderNo) {
        return custodyStatusRepository.getCustodyStatusRecord(offenderNo)
                .map(this::toCustodyStatus)
                .orElseThrow(new EntityNotFoundException(offenderNo));
    }

    @Override
    public List<PrisonerCustodyStatus> listCustodyStatuses(String locationId, CustodyStatusCode custodyStatusCode, String orderBy, Order order) {
        return custodyStatusRepository.listCustodyStatusRecords(locationId, orderBy, order)
                .stream()
                .map(this::toCustodyStatus)
                .filter(x -> filterOnCustodyStatus(x, custodyStatusCode))
                .collect(Collectors.toList());
    }

    private boolean filterOnCustodyStatus(PrisonerCustodyStatus record, CustodyStatusCode custodyStatusCode) {
        if (custodyStatusCode != null) {
            return custodyStatusCode.equals(record.getCustodyStatusCode());
        }

        return true;
    }

    private PrisonerCustodyStatus toCustodyStatus(CustodyStatusRecord record) {
        CustodyStatusCode custodyStatusCode = calculator.CustodyStatusCodeOf(record);

        return PrisonerCustodyStatus
                .builder()
                .offenderNo(record.getOffender_id_display())
                .locationId(record.getAgy_loc_id())
                .custodyStatusCode(custodyStatusCode)
                .custodyStatusDescription(custodyStatusCode.toString())
                .build();
    }
}

