package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.util.IQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Repository
@Slf4j
public class OffenderCurfewRepositoryImpl extends RepositoryBase implements OffenderCurfewRepository {

    private static final Map<String, FieldMapper> FIELD_MAP = Collections.singletonMap("AGENCY_LOCATION_ID", new FieldMapper("agencyLocationId"));

    @Override
    public Collection<Long> offendersWithoutCurfewApprovalStatus(String agencyFilterClause) {
        String initialSql = getQuery( "OFFENDERS_WITHOUT_CURFEW_APPROVAL_STATUS");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, FIELD_MAP);

        String sql = builder
                .addQuery(agencyFilterClause)
                .build();

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                (rs, rowNum) -> rs.getLong("OFFENDER_BOOK_ID"));
    }
}
