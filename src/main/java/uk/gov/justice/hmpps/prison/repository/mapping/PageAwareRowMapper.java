package uk.gov.justice.hmpps.prison.repository.mapping;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.RowMapper;
import uk.gov.justice.hmpps.prison.core.Constants;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PageAwareRowMapper<T> implements RowMapper<T> {
    private RowMapper<T> wrappedRowMapper;
    private long totalRecords;
    private boolean recordCountSet;

    public PageAwareRowMapper(final RowMapper<T> wrappedRowMapper) {
        this.wrappedRowMapper = wrappedRowMapper;
    }


    public T mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        if (!recordCountSet) {
            final var val = rs.getObject(Constants.RECORD_COUNT_COLUMN);

            totalRecords = ((Number) ObjectUtils.defaultIfNull(val, 0)).longValue();

            recordCountSet = true;
        }

        return wrappedRowMapper.mapRow(rs, rowNum);
    }

    public long getTotalRecords() {
        return totalRecords;
    }
}
