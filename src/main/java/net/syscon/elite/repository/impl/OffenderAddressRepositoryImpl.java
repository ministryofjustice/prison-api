package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.repository.OffenderAddressRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OffenderAddressRepositoryImpl extends RepositoryBase implements OffenderAddressRepository {

    private static final StandardBeanPropertyRowMapper<OffenderAddress> ADDRESS_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderAddress.class);

    @Override
    public List<OffenderAddress> getAddresses(final String offenderNumber) {

        final var initialSql = getQuery("GET_OFFENDER_ADDRESSES");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, ADDRESS_ROW_MAPPER);

        final var sql = builder.build();

        return jdbcTemplate.query(
                sql,
                createParams("offenderNo", offenderNumber),
                ADDRESS_ROW_MAPPER);
    }
}
