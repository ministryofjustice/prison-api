package net.syscon.elite.repository;

import net.syscon.elite.api.support.Order;

import java.util.List;
import java.util.Optional;

public interface CustodyStatusRepository {
    List<CustodyStatusRecord> listCustodyStatusRecords();
    Optional<CustodyStatusRecord> getCustodyStatusRecord(String offenderNo);
}
