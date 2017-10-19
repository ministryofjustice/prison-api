package net.syscon.elite.repository.impl;

import com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.repository.CustodyStatusRecord;
import net.syscon.util.IQueryBuilder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CustodyStatusRepositoryImpl extends RepositoryBase implements CustodyStatusRepository {
    private final Map<String, FieldMapper> custodyStatusRecordMapping =
            new ImmutableMap.Builder<String, FieldMapper>().build();

    @Override
    public List<CustodyStatusRecord> listCustodyStatusRecords(String query, String orderByField, Order order) {
        String sql = getQueryBuilder("LIST_CUSTODY_STATUSES", orderByField, order).build();
        return jdbcTemplate.query(sql, getCustodyStatusRowMapper(sql));
    }

    @Override
    public Optional<CustodyStatusRecord> getCustodyStatusRecord(String offenderNo) {
        String sql = getQueryBuilder("GET_CUSTODY_STATUS").build();

        CustodyStatusRecord record;

        try {
            record = jdbcTemplate.queryForObject(sql, createParams("offenderNo", offenderNo), getCustodyStatusRowMapper(sql));
        } catch (EmptyResultDataAccessException ex) {
            record = null;
        }

        return Optional.ofNullable(record);
    }

    private RowMapper<CustodyStatusRecord> getCustodyStatusRowMapper(String sql) {
        return Row2BeanRowMapper.makeMapping(sql, CustodyStatusRecord.class, custodyStatusRecordMapping);
    }

    private IQueryBuilder getQueryBuilder(String query) {
        return queryBuilderFactory.getQueryBuilder(getQuery(query), custodyStatusRecordMapping);
    }

    private IQueryBuilder getQueryBuilder(String query, String orderByField, Order order) {
        return getQueryBuilder(query).addOrderBy((order == Order.ASC), orderByField);
    }
}

