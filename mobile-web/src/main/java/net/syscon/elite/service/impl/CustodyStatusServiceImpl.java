package net.syscon.elite.service.impl;

import com.google.common.collect.Lists;
import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.repository.CustodyStatusRecord;
import net.syscon.elite.service.CustodyStatusCalculator;
import net.syscon.elite.service.CustodyStatusService;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
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
    public List<PrisonerCustodyStatus> listCustodyStatuses(Order order) {
        return listCustodyStatuses(null, order);
    }

    @Override
    public List<PrisonerCustodyStatus> listCustodyStatuses(CustodyStatusCode custodyStatusCode) {
        return listCustodyStatuses(Arrays.asList(custodyStatusCode), null);
    }

    @Override
    public List<PrisonerCustodyStatus> listCustodyStatuses(List<CustodyStatusCode> custodyStatusCodes, Order order) {
        return custodyStatusRepository.listCustodyStatusRecords()
                .stream()
                .map(this::toCustodyStatus)
                .filter(x -> filterOnCustodyStatus(x, custodyStatusCodes))
                .sorted(new PrisonerCustodyStatusComparator(order))
                .collect(Collectors.toList());
    }

    private boolean filterOnCustodyStatus(PrisonerCustodyStatus record, List<CustodyStatusCode> custodyStatusCodes) {
        if (custodyStatusCodes != null && custodyStatusCodes.size() > 0) {
            return custodyStatusCodes.contains(record.getCustodyStatusCode());
        }

        return true;
    }

    private PrisonerCustodyStatus toCustodyStatus(CustodyStatusRecord record) {
        CustodyStatusCode custodyStatusCode = calculator.CustodyStatusCodeOf(record);

        return PrisonerCustodyStatus
                .builder()
                .offenderNo(record.getOffender_id_display())
                .custodyStatusCode(custodyStatusCode)
                .custodyStatusDescription(custodyStatusCode.toString())
                .build();
    }

    private class PrisonerCustodyStatusComparator implements Comparator<PrisonerCustodyStatus> {
        private final Order order;

        public PrisonerCustodyStatusComparator(Order order) {
            this.order = order;
        }

        @Override
        public int compare(PrisonerCustodyStatus a, PrisonerCustodyStatus b) {
            return order == Order.ASC ?
                    a.getCustodyStatusDescription().compareTo(b.getCustodyStatusDescription()) :
                    b.getCustodyStatusDescription().compareTo(a.getCustodyStatusDescription());
        }
    }
}

