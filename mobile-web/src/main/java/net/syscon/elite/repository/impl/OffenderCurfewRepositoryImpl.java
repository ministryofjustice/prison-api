package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.support.OffenderCurfew;
import net.syscon.util.IQueryBuilder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Repository
@Slf4j
public class OffenderCurfewRepositoryImpl extends RepositoryBase implements OffenderCurfewRepository {

    private static final Map<String, FieldMapper> FIELD_MAP = Collections.singletonMap("AGENCY_LOCATION_ID", new FieldMapper("agencyLocationId"));

    private static final RowMapper<OffenderCurfew> OFFENDER_CURFEW_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderCurfew.class);

    @Override
    public Collection<OffenderCurfew> offenderCurfews(Set<String> agencyIds) {

        String sql = queryBuilderFactory
                .getQueryBuilder(
                        getQuery( "OFFENDER_CURFEWS"),
                        Collections.emptyMap())
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("agencyLocationIds", agencyIds),
                OFFENDER_CURFEW_ROW_MAPPER);
    }
}
