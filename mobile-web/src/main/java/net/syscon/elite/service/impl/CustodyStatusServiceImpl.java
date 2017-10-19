package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CustodyStatus;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.repository.CustodyStatusRecord;
import net.syscon.elite.service.CustodyStatusCalculator;
import net.syscon.elite.service.CustodyStatusService;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustodyStatusServiceImpl implements CustodyStatusService {
    private final CustodyStatusRepository custodyStatusRepository;

    public CustodyStatusServiceImpl(CustodyStatusRepository custodyStatusRepository) {
        this.custodyStatusRepository = custodyStatusRepository;
    }

    @Override
    public CustodyStatus getCustodyStatus(String offenderNo) {
        return custodyStatusRepository.getCustodyStatusRecord(offenderNo)
                .map(this::toCustodyStatus)
                .orElseThrow(new EntityNotFoundException(offenderNo));
    }

    @Override
    public List<CustodyStatus> listCustodyStatuses(String query, String orderBy, Order order) {
        return custodyStatusRepository.listCustodyStatusRecords(query, orderBy, order)
                .stream()
                .map(this::toCustodyStatus)
                .collect(Collectors.toList());
    }

    private CustodyStatus toCustodyStatus(CustodyStatusRecord record) {
        return CustodyStatus
                .builder()
                .offenderNo(record.getOffender_id_display())
                .locationId(record.getAgy_loc_id())
                .custodyStatus(CustodyStatusCalculator.custodyStatusOf(record))
                .build();
    }
}

