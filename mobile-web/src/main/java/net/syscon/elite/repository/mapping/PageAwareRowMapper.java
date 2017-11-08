package net.syscon.elite.repository.mapping;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PageAwareRowMapper<T> implements RowMapper<T> {
    private RowMapper<T> wrappedRowMapper;
    private long totalRecords;
    private boolean recordCountSet;

    public PageAwareRowMapper(RowMapper<T> wrappedRowMapper) {
        this.wrappedRowMapper = wrappedRowMapper;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (!recordCountSet) {
            Object val = rs.getObject("RECORD_COUNT");

            totalRecords = ((Number) ObjectUtils.defaultIfNull(val, 0)).longValue();

            recordCountSet = true;
        }

        return wrappedRowMapper.mapRow(rs, rowNum);
    }

    public long getTotalRecords() {
        return totalRecords;
    }
}
