package net.syscon.elite.repository.impl;

import com.google.common.annotations.VisibleForTesting;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.ws.rs.BadRequestException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Agency API repository implementation.
 */
@Repository
public class AgencyRepositoryImpl extends RepositoryBase implements AgencyRepository {
    private static final StandardBeanPropertyRowMapper<Address> ADDRESS_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Address.class);

    private static final StandardBeanPropertyRowMapper<Agency> AGENCY_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Agency.class);

    private static final StandardBeanPropertyRowMapper<Location> LOCATION_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Location.class);

    @Override
    public Page<Agency> getAgencies(String orderByField, Order order, long offset, long limit) {
        String initialSql = getQuery("GET_AGENCIES");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        String sql = builder
                .addRowCount()
                .addOrderBy(order, orderByField)
                .addPagination()
                .build();

        PageAwareRowMapper<Agency> paRowMapper = new PageAwareRowMapper<>(AGENCY_ROW_MAPPER);

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
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        String sql = builder.addOrderBy(true, "agencyId").build();

        return jdbcTemplate.query(
                sql,
                createParams("username", username, "caseloadType", "INST", "caseloadFunction", "GENERAL"),
                AGENCY_ROW_MAPPER);
    }

    @Override
    public List<Agency> findAgenciesForCurrentCaseloadByUsername(String username) {
        String initialSql = getQuery("FIND_AGENCIES_BY_CURRENT_CASELOAD");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        String sql = builder.build();
        return jdbcTemplate.query(
                sql,
                createParams("username", username),
                AGENCY_ROW_MAPPER);
    }

    @Override
    public List<Agency> findAgenciesByCaseload(String caseload) {
        String initialSql = getQuery("FIND_AGENCIES_BY_CASELOAD");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        String sql = builder.build();
        return jdbcTemplate.query(
                sql,
                createParams("caseloadId", caseload),
                AGENCY_ROW_MAPPER);
    }


    @Override
    public Optional<Agency> getAgency(String agencyId) {
        String initialSql = getQuery("GET_AGENCY");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        String sql = builder.build();

        Agency agency;

        try {
            agency = jdbcTemplate.queryForObject(sql, createParams("agencyId", agencyId), AGENCY_ROW_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            agency = null;
        }

        return Optional.ofNullable(agency);
    }

    @Override
    public List<Location> getAgencyLocations(String agencyId, List<String> eventTypes, String sortFields, Order sortOrder) {
        String initialSql;

        if (eventTypes.isEmpty()) {
            initialSql = getQuery("GET_AGENCY_LOCATIONS");
        } else {
            initialSql = getQuery("GET_AGENCY_LOCATIONS_FOR_EVENT_TYPE");
        }

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);

        String sql = builder.addOrderBy(sortOrder, sortFields).build();

        return jdbcTemplate.query(
                sql,
                createParams("agencyId", agencyId, "eventTypes", eventTypes),
                LOCATION_ROW_MAPPER);
    }

    @Override
    @Cacheable(value = "getAgencyLocationsBooked", key = "#agencyId + '-' + #bookedOnDay + '-' + #bookedOnPeriod")
    public List<Location> getAgencyLocationsBooked(String agencyId, LocalDate bookedOnDay, TimeSlot bookedOnPeriod) {
        final MapSqlParameterSource params = createParams("agencyId", agencyId);

        String initialSql = getQuery("GET_AGENCY_LOCATIONS_FOR_EVENTS_BOOKED");
        setupDates(params, bookedOnDay, bookedOnPeriod);

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);
        String sql = builder.build();

        return jdbcTemplate.query(sql, params, LOCATION_ROW_MAPPER);
    }

    private void setupDates(MapSqlParameterSource params, LocalDate bookedOnDay, TimeSlot bookedOnPeriod) {
        LocalDateTime start = bookedOnDay.atStartOfDay();
        LocalDateTime end = bookedOnDay.plusDays(1).atStartOfDay();
        if (bookedOnPeriod != null) {
            switch (bookedOnPeriod) {
                case AM:
                    end = bookedOnDay.atTime(12, 0);
                    break;
                case PM:
                    start = bookedOnDay.atTime(12, 0);
                    end = bookedOnDay.atTime(17, 0);
                    break;
                case ED:
                    start = bookedOnDay.atTime(17, 0);
                    break;
                default:
                    throw new BadRequestException("Unrecognised timeslot: " + bookedOnPeriod);
            }
        }
        end = end.minus(1, ChronoUnit.SECONDS);
        Timestamp periodStart =  DateTimeConverter.fromLocalDateTime(start);
        Timestamp periodEnd =  DateTimeConverter.fromLocalDateTime(end);
        params.addValue("periodStart", periodStart);
        params.addValue("periodEnd", periodEnd);
    }

    @Override
    public List<PrisonContactDetail> getPrisonContactDetails(String agencyId) {
        String sql = getQuery("FIND_PRISON_ADDRESSES_PHONE_NUMBERS");

        final List<Address> outerJoinResults = jdbcTemplate.query(sql, createParams("agencyId", agencyId), ADDRESS_ROW_MAPPER);

        return mapResultsToPrisonContactDetailsList(groupAddresses(outerJoinResults).values());
    }

    @VisibleForTesting
    List<PrisonContactDetail> mapResultsToPrisonContactDetailsList(Collection<List<Address>> groupedResults) {
        return groupedResults.stream().map(this::mapResultsToPrisonContactDetails)
                .sorted(Comparator.comparing(PrisonContactDetail::getAgencyId))
                .collect(Collectors.toList());

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
