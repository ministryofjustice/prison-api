package net.syscon.elite.repository.mapping;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PageableAwareRowMapper<T> implements RowMapper<T> {
    private RowMapper<T> wrappedRowMapper;
    private long recordCount;
    private boolean recordCountSet;

    public PageableAwareRowMapper(RowMapper<T> wrappedRowMapper) {
        this.wrappedRowMapper = wrappedRowMapper;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (!recordCountSet) {
            Object val = rs.getObject("RECORD_COUNT");

            if (val instanceof BigDecimal) {
                recordCount = ((BigDecimal) ObjectUtils.defaultIfNull(val, BigDecimal.ZERO)).longValue();
            } else {
                recordCount = (Long) ObjectUtils.defaultIfNull(val, 0);
            }
            recordCountSet = true;
        }

        return wrappedRowMapper.mapRow(rs, rowNum);
    }

    public long getRecordCount() {
        return recordCount;
    }
}
