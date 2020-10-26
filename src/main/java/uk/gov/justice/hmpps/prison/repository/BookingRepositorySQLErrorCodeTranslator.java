package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import java.sql.SQLException;

public class BookingRepositorySQLErrorCodeTranslator extends SQLErrorCodeSQLExceptionTranslator {
    public static final int SQL_ERROR_OFFENDER_NOT_MATCHED = 20000;
    public static final int SQL_ERROR_MULTIPLE_OFFENDERS_MATCHED = 20001;
    public static final int SQL_ERROR_OFFENDER_ALREADY_EXISTS = 20002;
    public static final int SQL_ERROR_INVALID_LAST_NAME = 20003;
    public static final int SQL_ERROR_INVALID_FIRST_NAME = 20004;
    public static final int SQL_ERROR_INVALID_GIVEN_NAME = 20005;
    public static final int SQL_ERROR_INVALID_DATE_OF_BIRTH = 20006;
    public static final int SQL_ERROR_INVALID_GENDER = 20007;
    public static final int SQL_ERROR_INVALID_TITLE = 20008;
    public static final int SQL_ERROR_INVALID_SUFFIX = 20009;
    public static final int SQL_ERROR_INVALID_ETHNICITY = 20010;
    public static final int SQL_ERROR_INVALID_EXTERNAL_ID_TYPE = 20011;
    public static final int SQL_ERROR_INVALID_PNC_NUMBER = 20012;
    public static final int SQL_ERROR_DUPLICATE_PNC_NUMBER = 20013;

    public static final int SQL_ERROR_OFFENDER_NOT_FOUND = 20100;
    public static final int SQL_ERROR_OFFENDER_ACTIVE_BOOKING = 20101;
    public static final int SQL_ERROR_INVALID_IMPRISONMENT_STATUS = 20102;
    public static final int SQL_ERROR_INVALID_ADMISSION_REASON = 20103;
    public static final int SQL_ERROR_INVALID_FROM_AGENCY_LOCATION = 20104;
    public static final int SQL_ERROR_COPYING_BOOKING_DATA = 20105;
    public static final int SQL_ERROR_NO_PREVIOUS_BOOKING = 20106;
    public static final int SQL_ERROR_INVALID_LIVING_UNIT_ID = 20107;
    public static final int SQL_ERROR_MISSING_NOMS_ID = 20108;
    public static final int SQL_ERROR_INVALID_TO_AGENCY_LOCATION = 20109;

    @Override
    protected DataAccessException customTranslate(final String task, final String sql, final SQLException sqlEx) {
        final var errorCode = sqlEx.getErrorCode();

        if (errorCode == SQL_ERROR_MISSING_NOMS_ID) {
            throw new InvalidDataAccessApiUsageException("Missing offender number.", sqlEx);
        } else if ((errorCode == SQL_ERROR_OFFENDER_NOT_MATCHED) || (errorCode == SQL_ERROR_OFFENDER_NOT_FOUND)) {
            throw new EmptyResultDataAccessException("No matching offender found.", 1);
        } else if (errorCode == SQL_ERROR_MULTIPLE_OFFENDERS_MATCHED) {
            throw new IncorrectResultSizeDataAccessException("Multiple matching offenders found.", 1);
        } else if (errorCode == SQL_ERROR_OFFENDER_ALREADY_EXISTS) {
            throw new DataIntegrityViolationException("Offender already exists.", sqlEx);
        } else if (errorCode == SQL_ERROR_OFFENDER_ACTIVE_BOOKING) {
            throw new DataIntegrityViolationException("Offender already has active booking.", sqlEx);
        } else if (errorCode == SQL_ERROR_NO_PREVIOUS_BOOKING) {
            throw new EmptyResultDataAccessException("No previous booking found for offender.", 1);
        } else if (errorCode == SQL_ERROR_INVALID_FROM_AGENCY_LOCATION) {
            throw new InvalidDataAccessApiUsageException("Invalid 'from' agency location.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_TO_AGENCY_LOCATION) {
            throw new InvalidDataAccessApiUsageException("Invalid 'to' agency location.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_LIVING_UNIT_ID) {
            throw new DataRetrievalFailureException("Reception location not found for agency.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_ADMISSION_REASON) {
            throw new InvalidDataAccessApiUsageException("Invalid admission reason.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_IMPRISONMENT_STATUS) {
            throw new InvalidDataAccessApiUsageException("Invalid or missing imprisonment status.", sqlEx);
        } else if (errorCode == SQL_ERROR_COPYING_BOOKING_DATA) {
            throw new NonTransientDataAccessResourceException("Error copying booking data.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_LAST_NAME) {
            throw new InvalidDataAccessApiUsageException("Invalid last name.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_FIRST_NAME) {
            throw new InvalidDataAccessApiUsageException("Invalid first name.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_GIVEN_NAME) {
            throw new InvalidDataAccessApiUsageException("Invalid given name.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_DATE_OF_BIRTH) {
            throw new InvalidDataAccessApiUsageException("Invalid date of birth.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_GENDER) {
            throw new InvalidDataAccessApiUsageException("Invalid gender.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_TITLE) {
            throw new InvalidDataAccessApiUsageException("Invalid title.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_SUFFIX) {
            throw new InvalidDataAccessApiUsageException("Invalid suffix.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_ETHNICITY) {
            throw new InvalidDataAccessApiUsageException("Invalid ethnicity.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_EXTERNAL_ID_TYPE) {
            throw new InvalidDataAccessApiUsageException("Invalid external identifier type.", sqlEx);
        } else if (errorCode == SQL_ERROR_INVALID_PNC_NUMBER) {
            throw new InvalidDataAccessApiUsageException("Invalid PNC number.", sqlEx);
        } else if (errorCode == SQL_ERROR_DUPLICATE_PNC_NUMBER) {
            throw new InvalidDataAccessApiUsageException("Duplicate PNC number.", sqlEx);
        }

        return super.customTranslate(task, sql, sqlEx);
    }
}
