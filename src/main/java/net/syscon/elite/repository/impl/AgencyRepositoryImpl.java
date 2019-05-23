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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
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
    public Page<Agency> getAgencies(final String orderByField, final Order order, final long offset, final long limit) {
        final var initialSql = getQuery("GET_AGENCIES");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder
                .addRowCount()
                .addOrderBy(order, orderByField)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<Agency>(AGENCY_ROW_MAPPER);

        final var agencies = jdbcTemplate.query(
                sql,
                createParams("offset", offset, "limit", limit),
                paRowMapper);

        return new Page<>(agencies, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public List<Agency> getAgenciesByType(final String agencyType) {
       return jdbcTemplate.query(
               getQuery("GET_AGENCIES_BY_TYPE"),
                createParams("agencyType", agencyType, "activeFlag", "Y", "excludeIds", List.of("OUT", "TRN")),
                AGENCY_ROW_MAPPER);
    }

    @Override
    @Cacheable("findAgenciesByUsername")
    public List<Agency> findAgenciesByUsername(final String username) {
        final var initialSql = getQuery("FIND_AGENCIES_BY_USERNAME");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder.addOrderBy(true, "agencyId").build();

        return jdbcTemplate.query(
                sql,
                createParams("username", username, "caseloadType", "INST", "caseloadFunction", "GENERAL"),
                AGENCY_ROW_MAPPER);
    }

    @Override
    public List<Agency> findAgenciesForCurrentCaseloadByUsername(final String username) {
        final var initialSql = getQuery("FIND_AGENCIES_BY_CURRENT_CASELOAD");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder.build();
        return jdbcTemplate.query(
                sql,
                createParams("username", username),
                AGENCY_ROW_MAPPER);
    }

    @Override
    public List<Agency> findAgenciesByCaseload(final String caseload) {
        final var initialSql = getQuery("FIND_AGENCIES_BY_CASELOAD");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder.build();
        return jdbcTemplate.query(
                sql,
                createParams("caseloadId", caseload),
                AGENCY_ROW_MAPPER);
    }


    @Override
    public Optional<Agency> getAgency(final String agencyId, final boolean activeOnly) {
        final var initialSql = getQuery("GET_AGENCY");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, AGENCY_ROW_MAPPER);

        final var sql = builder.build();

        Agency agency;

        final var activeFlag = activeOnly ? "Y" : null;

        try {
            agency = jdbcTemplate.queryForObject(sql, createParams("agencyId", agencyId, "activeFlag", activeFlag), AGENCY_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            agency = null;
        }

        return Optional.ofNullable(agency);
    }

    @Override
    public List<Location> getAgencyLocations(final String agencyId, final List<String> eventTypes, final String sortFields, final Order sortOrder) {
        final String initialSql;

        if (eventTypes.isEmpty()) {
            initialSql = getQuery("GET_AGENCY_LOCATIONS");
        } else {
            initialSql = getQuery("GET_AGENCY_LOCATIONS_FOR_EVENT_TYPE");
        }

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);

        final var sql = builder.addOrderBy(sortOrder, sortFields).build();

        return jdbcTemplate.query(
                sql,
                createParams("agencyId", agencyId, "eventTypes", eventTypes),
                LOCATION_ROW_MAPPER);
    }

    @Override
    public List<Location> getAgencyLocationsBooked(final String agencyId, final LocalDate bookedOnDay, final TimeSlot bookedOnPeriod) {
        final var params = createParams("agencyId", agencyId);

        final var initialSql = getQuery("GET_AGENCY_LOCATIONS_FOR_EVENTS_BOOKED");
        setupDates(params, bookedOnDay, bookedOnPeriod);

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, LOCATION_ROW_MAPPER);
        final var sql = builder.build();

        return jdbcTemplate.query(sql, params, LOCATION_ROW_MAPPER);
    }

    private void setupDates(final MapSqlParameterSource params, final LocalDate bookedOnDay, final TimeSlot bookedOnPeriod) {
        var start = bookedOnDay.atStartOfDay();
        var end = bookedOnDay.plusDays(1).atStartOfDay();
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
        final var periodStart = DateTimeConverter.fromLocalDateTime(start);
        final var periodEnd = DateTimeConverter.fromLocalDateTime(end);
        params.addValue("periodStart", periodStart);
        params.addValue("periodEnd", periodEnd);
    }

    @Override
    public List<PrisonContactDetail> getPrisonContactDetails(final String agencyId) {
        final var sql = getQuery("FIND_PRISON_ADDRESSES_PHONE_NUMBERS");

        final var outerJoinResults = jdbcTemplate.query(sql, createParams("agencyId", agencyId), ADDRESS_ROW_MAPPER);

        return mapResultsToPrisonContactDetailsList(groupAddresses(outerJoinResults).values());
    }

    @VisibleForTesting
    List<PrisonContactDetail> mapResultsToPrisonContactDetailsList(final Collection<List<Address>> groupedResults) {
        return groupedResults.stream().map(this::mapResultsToPrisonContactDetails)
                .sorted(Comparator.comparing(PrisonContactDetail::getAgencyId))
                .collect(Collectors.toList());

    }

    private PrisonContactDetail mapResultsToPrisonContactDetails(final List<Address> addressList) {
        final var telephones = addressList.stream().map(a ->
                Telephone.builder().number(a.getPhoneNo()).type(a.getPhoneType()).ext(a.getExtNo()).build()).collect(Collectors.toList());

        final var address = addressList.stream().findAny().get();
        return PrisonContactDetail.builder().agencyId(address.getAgencyId())
                .addressType(address.getAddressType())
                .phones(telephones)
                .locality(address.getLocality())
                .postCode(address.getPostalCode())
                .premise(address.getPremise())
                .city(address.getCity())
                .country(address.getCountry()).build();
    }

    private Map<String, List<Address>> groupAddresses(final List<Address> list) {
        return list.stream().collect(groupingBy(Address::getAgencyId));
    }
}
