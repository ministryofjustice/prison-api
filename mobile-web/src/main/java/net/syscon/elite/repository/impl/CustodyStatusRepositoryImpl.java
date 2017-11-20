package net.syscon.elite.repository.impl;

import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.service.support.CustodyStatusDto;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.IQueryBuilder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
public class CustodyStatusRepositoryImpl extends RepositoryBase implements CustodyStatusRepository {

    private final StandardBeanPropertyRowMapper<CustodyStatusDto> CUSTODY_STATUS_MAPPER = new StandardBeanPropertyRowMapper<>(CustodyStatusDto.class);

    @Override
    public List<CustodyStatusDto> listCustodyStatuses(LocalDate onDate) {
        String sql = getQueryBuilder("LIST_CUSTODY_STATUSES").build();
        return jdbcTemplate.query(sql, createParams("onDate", onDate.format(DateTimeFormatter.ISO_LOCAL_DATE)), CUSTODY_STATUS_MAPPER);
    }

    @Override
    public Optional<CustodyStatusDto> getCustodyStatus(String offenderNo, LocalDate onDate) {
        String sql = getQueryBuilder("GET_CUSTODY_STATUS").build();

        CustodyStatusDto record;

        try {
            record = jdbcTemplate.queryForObject(sql, createParams("offenderNo", offenderNo, "onDate", onDate.format(DateTimeFormatter.ISO_LOCAL_DATE)), CUSTODY_STATUS_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            record = null;
        }

        return Optional.ofNullable(record);
    }

    private IQueryBuilder getQueryBuilder(String queryName) {
        return queryBuilderFactory.getQueryBuilder(getQuery(queryName), CUSTODY_STATUS_MAPPER.getFieldMap());
    }
}

