package uk.gov.justice.hmpps.prison.repository;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.personalofficer.PersonalOfficer;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class PersonalOfficerRepository {
    private static final String GET_ALLOCATION_HISTORY_SQL = """
        SELECT
            OCO.CASE_AGY_LOC_ID     AGENCY_ID,
            O.OFFENDER_ID_DISPLAY   OFFENDER_NO,
            OCO.CASE_OFFICER_ID     STAFF_ID,
            OCO.USER_ID             USER_ID,
            OCO.CASE_ASSIGNED_TIME  ASSIGNED,
            OCO.CREATE_DATETIME     CREATED,
            OCO.CREATE_USER_ID      CREATED_BY
        FROM OFFENDER_CASE_OFFICERS OCO
            INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OCO.OFFENDER_BOOK_ID
            INNER JOIN OFFENDERS O          ON OB.OFFENDER_ID = O.OFFENDER_ID
        WHERE OCO.CASE_AGY_LOC_ID = :agencyId
        """;

    private static final DataClassByColumnRowMapper<PersonalOfficer> PERSONAL_OFFICER_ROW_MAPPER =
        new DataClassByColumnRowMapper<>(PersonalOfficer.class);


    private final NamedParameterJdbcOperations jdbcTemplate;

    public List<PersonalOfficer> getAllocationHistoryForAgency(String agencyId) {
        Validate.notBlank(agencyId, "Agency id is required.");

        return jdbcTemplate.query(
            GET_ALLOCATION_HISTORY_SQL,
            new MapSqlParameterSource("agencyId", agencyId),
            PERSONAL_OFFICER_ROW_MAPPER
        );
    }
}
