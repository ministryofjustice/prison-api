package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.IQueryBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Agency API repository implementation.
 */
@Repository
public class AgencyRepositoryImpl extends RepositoryBase implements AgencyRepository {
    private final Map<String, FieldMapper> agencyMapping =
            new ImmutableMap.Builder<String, FieldMapper>()
                    .put("AGY_LOC_ID", new FieldMapper("agencyId"))
                    .put("DESCRIPTION", new FieldMapper("description"))
                    .put("AGENCY_LOCATION_TYPE", new FieldMapper("agencyType"))
                    .build();

    private static final RowMapper<Address> ADDRESS_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(Address.class);


    private final Map<String, FieldMapper> locationMapping  =
            new ImmutableMap.Builder<String, FieldMapper>()
                    .put("INTERNAL_LOCATION_ID", new FieldMapper("locationId"))
                    .put("DESCRIPTION", new FieldMapper("description"))
                    .build();
    private final Map<String, FieldMapper> addressMapping =
            new ImmutableMap.Builder<String, FieldMapper>()
                    .put("AGY_LOC_ID", new FieldMapper("agencyId"))
                    .build();
    @Override
    public Page<Agency> getAgencies(String orderByField, Order order, long offset, long limit) {
        String initialSql = getQuery("GET_AGENCIES");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, agencyMapping);

        String sql = builder
                .addRowCount()
                .addOrderBy(order, orderByField)
                .addPagination()
                .build();

        RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);
        PageAwareRowMapper<Agency> paRowMapper = new PageAwareRowMapper<>(agencyRowMapper);

        List<Agency> agencies = jdbcTemplate.query(
                sql,
                createParams("offset", offset, "limit", limit),
                paRowMapper);

        return new Page<>(agencies, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    @Cacheable("findAgenciesByUsername")
    public List<Agency> findAgenciesByUsername(String username) {
        String initialSql = getQuery("FIND_AGENCIES_BY_USERNAME");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, agencyMapping);

        String sql = builder.addOrderBy(true, "agencyId").build();

        RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);

        return jdbcTemplate.query(
                sql,
                createParams("username", username),
                agencyRowMapper);
    }

    @Override
    public Optional<Agency> getAgency(String agencyId) {
        String initialSql = getQuery("GET_AGENCY");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, agencyMapping);

        String sql = builder.build();

        RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);

        Agency agency;

        try {
            agency = jdbcTemplate.queryForObject(sql, createParams("agencyId", agencyId), agencyRowMapper);
        } catch (EmptyResultDataAccessException ex) {
            agency = null;
        }

        return Optional.ofNullable(agency);
    }

    @Override
    public List<Location> getAvailableLocations(String agencyId, String eventType) {
        String sql = getQuery("GET_AVAILABLE_LOCATIONS");
        RowMapper<Location> locationRowMapper = Row2BeanRowMapper.makeMapping(sql, Location.class, locationMapping);
        return jdbcTemplate.query(sql, createParams("agencyId", agencyId, "eventType", eventType), locationRowMapper);
    }

    @Override
    public List<PrisonContactDetail> getPrisonContactDetails(String agencyId) {
        String sql = getQuery("FIND_PRISON_ADDRESSES_PHONE_NUMBERS");

        final List<Address> outerJoinResults = jdbcTemplate.query(sql, createParams("agencyId", agencyId),ADDRESS_ROW_MAPPER);

        return mapResultsToPrisonContactDetailsList(groupAddresses(outerJoinResults).values());
    }

    List<PrisonContactDetail> mapResultsToPrisonContactDetailsList(Collection<List<Address>> groupedResults) {
        return groupedResults.stream().map(this::mapResultsToPrisonContactDetails).collect(Collectors.toList());
    }

    private PrisonContactDetail mapResultsToPrisonContactDetails(List<Address> addressList) {
        final List<Telephone> telephones = addressList.stream().map(a ->
                Telephone.builder().number(a.getPhoneNo()).type(a.getPhoneType()).ext(a.getExtNo()).build()).collect(Collectors.toList());

        final Address address = addressList.stream().findAny().get();
        return PrisonContactDetail.builder().agencyId(address.getAgencyId())
                .addressType(address.getAddressType())
                .phones(telephones)
                .locality(address.getLocality())
                .postCode(address.getPostalCode())
                .premise(address.getPremise())
                .city(address.getCity())
                .country(address.getCountry()).build();
    }

    private Map<String, List<Address>> groupAddresses(List<Address> list) {
        return list.stream().collect(groupingBy(Address::getAgencyId));
    }

}
