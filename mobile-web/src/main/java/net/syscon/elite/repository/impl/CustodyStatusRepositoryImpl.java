package net.syscon.elite.repository.impl;

import com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.repository.CustodyStatusRecord;
import net.syscon.util.IQueryBuilder;
import net.syscon.util.QueryUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CustodyStatusRepositoryImpl extends RepositoryBase implements CustodyStatusRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, FieldMapper> custodyStatusRecordMapping =
            new ImmutableMap.Builder<String, FieldMapper>()
                    .put("offender_id_display", new FieldMapper("offender_id_display"))
                    .put("agy_loc_id", new FieldMapper("agy_loc_id"))
                    .put("booking_status", new FieldMapper("booking_status"))
                    .put("active_flag", new FieldMapper("active_flag"))
                    .put("direction_code", new FieldMapper("direction_code"))
                    .put("movement_reason_code", new FieldMapper("movement_reason_code"))
                    .put("movement_type", new FieldMapper("movement_type"))
                    .build();

    @Override
    public List<CustodyStatusRecord> listCustodyStatusRecords(String query, String orderByField, Order order) {
        String sql = getQueryBuilder("LIST_CUSTODY_STATUSES")
                        .addQuery(query)
                        .build();

        logger.info(sql);

        List<CustodyStatusRecord> results = jdbcTemplate.query(sql, getCustodyStatusRowMapper(sql));

        if ("locationId".equals(orderByField)) {
            results.sort(new CustodyStatusRecordLocationComparator(order));
        }

        return results;
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

    private IQueryBuilder getQueryBuilder(String queryName) {
        return queryBuilderFactory.getQueryBuilder(getQuery(queryName), custodyStatusRecordMapping);
    }

    private class CustodyStatusRecordLocationComparator implements Comparator<CustodyStatusRecord> {

        private final Order order;

        public CustodyStatusRecordLocationComparator(Order order) {
            this.order = order;
        }

        @Override
        public int compare(CustodyStatusRecord a, CustodyStatusRecord b) {
            return order == Order.ASC ?
                    a.getAgy_loc_id().compareTo(b.getAgy_loc_id()) :
                    b.getAgy_loc_id().compareTo(a.getAgy_loc_id());
        }
    }
}

