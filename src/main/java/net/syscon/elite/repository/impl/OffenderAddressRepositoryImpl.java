package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.AddressDto;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.repository.OffenderAddressRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import static java.util.stream.Collectors.toList;

@Repository
public class OffenderAddressRepositoryImpl extends RepositoryBase implements OffenderAddressRepository {

    private static final StandardBeanPropertyRowMapper<AddressDto> ADDRESS_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(AddressDto.class);

    private static final StandardBeanPropertyRowMapper<Telephone> PHONE_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Telephone.class);

    @Override
    public List<AddressDto> getAddresses(final String offenderNumber) {

        final var initialSql = getQuery("GET_OFFENDER_ADDRESSES");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, ADDRESS_ROW_MAPPER);

        final var sql = builder.build();

        final var addresses =  jdbcTemplate.query(
                                sql,
                                createParams("offenderNo", offenderNumber),
                                ADDRESS_ROW_MAPPER);

        return addresses.stream().map(address ->
                address.toBuilder()
                        .phones(getPhones(address.getAddressId()))
                        .build())
                .collect(toList());
    }

    private List<Telephone> getPhones(final Long addressId) {

        final var initialSql = getQuery("GET_PHONES_FOR_ADDRESS");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PHONE_ROW_MAPPER);
        final var sql = builder.build();

        return jdbcTemplate.query(
                sql,
                createParams("addressId", addressId),
                PHONE_ROW_MAPPER);
    }
}
