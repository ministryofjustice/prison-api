package net.syscon.elite.repository.v1;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class NomisV1SQLErrorCodeTranslator extends SQLErrorCodeSQLExceptionTranslator {
    private static final String OFFENDER_NOT_FOUND = "Offender Not Found";
    private static final String LOCKED = "Resource Locked";
    private static final String INSUFFICIENT_FUNDS = "Insufficient Funds";
    private static final String NOT_DIGITAL_PRISON = "Not a Digital Prison";
    private static final String NO_TRUST_ACCOUNT = "Offender Has No Trust Account at Prison";
    private static final String OUT_OR_IN_TRANSIT = "Offender is out or in transit";
    private static final String NO_OFFENDER_IDENTIFIER = "No Offender Identifier provided";
    private static final String IDENTIFIER_INCONSISTANCY = "Offender Identifier inconsistancy";
    private static final String PRISON_NOT_FOUND = "Prison Not Found";
    private static final String FINANCE_EXCEPTION = "Finance Exception";
    private static final String VISITOR_ISSUE = "Issue with visitors - check cause";
    private static final String LEAD_VISITOR_ISSUE = "Issue with lead visitor - check cause";
    private static final String SLOT_NOT_AVAIILABLE = "Visit slot is not available";
    private static final String OVERLAPPING_VISIT = "Overlapping visit";
    private static final String NO_VO_PVO_BALANCE = "Offender has no VO or PVO balance";
    private static final String NOT_IN_SPECIFIED_PRISON = "Offender not in specified prison";
    private static final String INVALID_TRANSACTION_TYPE = "Invalid transaction type";
    private static final String DUPLICATE_POST = "Duplicate post";
    private static final String HOLD_DATE_IN_PAST = "Hold date should be greater than current date";
    private static final String HOLD_AMOUNT_NOT_EXCEED = "Hold Amount Cannot exceed the Balance Amount";
    private static final String NO_SUB_ACCT_BALANCE = "There is no balance available in the Sub account";
    private static final String SUB_ACCNT_NOT_EXIST = "Sub account does not exist";
    private static final String HOLD_NOT_FOUND = "Hold not found";
    private static final String CLOSED_VISITS = "Offender requires closed visits";
    private static final String VISIT_COMPLETED = "Visit completed";
    private static final String VISIT_CANCELLED = "Visit already cancelled";
    private static final String VISIT_NOT_FOUND = "Visit not found";
    private static final String INVALID_CANC_CODE = "Invalid cancellation code";
    private static final String INVALID_RELATIONSHIP_TYPE = "Invalid relationship type";
    private static final String INVALID_CONTACT_ID = "Invalid Contact Id";
    private static final String INVALID_COUNTRY_CODE = "Invalid country code";
    private static final String INVALID_COUNTY_CODE = "Invalid county code";
    private static final String INVALID_CITY_CODE = "Invalid city code";
    private static final String DUPLICATE_CONTACT = "Duplicate Contact";

    private static final String RECEIPT_ONLY = "Only receipt transaction types allowed";
    private static final String DISBURSEMENT_ONLY = "Only disbursement transaction types allowed";
    private static final String SUB_ACCOUNT_NOT_EQUAL_TO_CURRENT_BALANCE = "Sum of sub account balances not equal to current balance";
    private static final String FINANCIAL_SETUP_ERROR = "Financial setup error";
    private static final String OFFENDER_TRANSFERRED = "Offender being transferred";
    private static final String OFFENDER_NEVER_AT_PRISON = "Offender never at prison";
    private static final String OFFENDER_STILL_IN_SPECIFIED_PRISON = "Offender still in specified prison";

    // Map user defined database exceptions to DAOExceptions
    private static final Map<Integer, DataAccessException> SQL_EXCEPTION_MAP = new HashMap<>();

    static {
        SQL_EXCEPTION_MAP.put(20001, new EmptyResultDataAccessException(OFFENDER_NOT_FOUND, 1));
        SQL_EXCEPTION_MAP.put(20002, new InvalidDataAccessApiUsageException(IDENTIFIER_INCONSISTANCY));
        SQL_EXCEPTION_MAP.put(20003, new InvalidDataAccessApiUsageException(NO_OFFENDER_IDENTIFIER));
        SQL_EXCEPTION_MAP.put(20004, new InvalidDataAccessApiUsageException(VISITOR_ISSUE));
        SQL_EXCEPTION_MAP.put(20005, new InvalidDataAccessApiUsageException(OUT_OR_IN_TRANSIT));
        SQL_EXCEPTION_MAP.put(20006, new InvalidDataAccessApiUsageException(LOCKED));
        SQL_EXCEPTION_MAP.put(20007, new InvalidDataAccessApiUsageException(LEAD_VISITOR_ISSUE));
        SQL_EXCEPTION_MAP.put(20009, new InvalidDataAccessApiUsageException(INSUFFICIENT_FUNDS));
        SQL_EXCEPTION_MAP.put(20010, new InvalidDataAccessApiUsageException(NOT_DIGITAL_PRISON));
        SQL_EXCEPTION_MAP.put(20011, new InvalidDataAccessApiUsageException(NO_TRUST_ACCOUNT));
        SQL_EXCEPTION_MAP.put(20012, new InvalidDataAccessApiUsageException(PRISON_NOT_FOUND));
        SQL_EXCEPTION_MAP.put(20013, new InvalidDataAccessApiUsageException(FINANCE_EXCEPTION));
        SQL_EXCEPTION_MAP.put(20014, new InvalidDataAccessApiUsageException(SLOT_NOT_AVAIILABLE));
        SQL_EXCEPTION_MAP.put(20015, new InvalidDataAccessApiUsageException(OVERLAPPING_VISIT));
        SQL_EXCEPTION_MAP.put(20016, new InvalidDataAccessApiUsageException(NO_VO_PVO_BALANCE));
        SQL_EXCEPTION_MAP.put(20017, new InvalidDataAccessApiUsageException(NOT_IN_SPECIFIED_PRISON));
        SQL_EXCEPTION_MAP.put(20018, new InvalidDataAccessApiUsageException(INVALID_TRANSACTION_TYPE));
        SQL_EXCEPTION_MAP.put(20019, new DuplicateKeyException(DUPLICATE_POST));
        SQL_EXCEPTION_MAP.put(20020, new InvalidDataAccessApiUsageException(SUB_ACCNT_NOT_EXIST));
        SQL_EXCEPTION_MAP.put(20021, new InvalidDataAccessApiUsageException(NO_SUB_ACCT_BALANCE));
        SQL_EXCEPTION_MAP.put(20022, new InvalidDataAccessApiUsageException(HOLD_AMOUNT_NOT_EXCEED));
        SQL_EXCEPTION_MAP.put(20023, new InvalidDataAccessApiUsageException(HOLD_DATE_IN_PAST));
        SQL_EXCEPTION_MAP.put(20024, new InvalidDataAccessApiUsageException(HOLD_NOT_FOUND));
        SQL_EXCEPTION_MAP.put(20025, new InvalidDataAccessApiUsageException(VISIT_CANCELLED));
        SQL_EXCEPTION_MAP.put(20026, new InvalidDataAccessApiUsageException(VISIT_COMPLETED));
        SQL_EXCEPTION_MAP.put(20027, new InvalidDataAccessApiUsageException(VISIT_NOT_FOUND));
        SQL_EXCEPTION_MAP.put(20028, new InvalidDataAccessApiUsageException(INVALID_CANC_CODE));
        SQL_EXCEPTION_MAP.put(20029, new InvalidDataAccessApiUsageException(CLOSED_VISITS));
        SQL_EXCEPTION_MAP.put(20030, new InvalidDataAccessApiUsageException(INVALID_RELATIONSHIP_TYPE));
        SQL_EXCEPTION_MAP.put(20031, new InvalidDataAccessApiUsageException(INVALID_CONTACT_ID));
        SQL_EXCEPTION_MAP.put(20032, new InvalidDataAccessApiUsageException(INVALID_CITY_CODE));
        SQL_EXCEPTION_MAP.put(20033, new InvalidDataAccessApiUsageException(INVALID_COUNTY_CODE));
        SQL_EXCEPTION_MAP.put(20034, new InvalidDataAccessApiUsageException(INVALID_COUNTRY_CODE));
        SQL_EXCEPTION_MAP.put(20035, new InvalidDataAccessApiUsageException(DUPLICATE_CONTACT));
        SQL_EXCEPTION_MAP.put(20036, new InvalidDataAccessApiUsageException(OFFENDER_STILL_IN_SPECIFIED_PRISON));
        SQL_EXCEPTION_MAP.put(20037, new InvalidDataAccessApiUsageException(OFFENDER_NEVER_AT_PRISON));
        SQL_EXCEPTION_MAP.put(20038, new InvalidDataAccessApiUsageException(OFFENDER_TRANSFERRED));
        SQL_EXCEPTION_MAP.put(20039, new InvalidDataAccessApiUsageException(FINANCIAL_SETUP_ERROR));
        SQL_EXCEPTION_MAP.put(20040, new InvalidDataAccessApiUsageException(SUB_ACCOUNT_NOT_EQUAL_TO_CURRENT_BALANCE));
        SQL_EXCEPTION_MAP.put(20041, new InvalidDataAccessApiUsageException(DISBURSEMENT_ONLY));
        SQL_EXCEPTION_MAP.put(20042, new InvalidDataAccessApiUsageException(RECEIPT_ONLY));

    }

    @Override
    protected DataAccessException customTranslate(final String task, final String sql, final SQLException sqlEx) {
        final var errorCode = sqlEx.getErrorCode();

        var dataAccessException = SQL_EXCEPTION_MAP.get(errorCode);
        if (dataAccessException != null) {
            dataAccessException.initCause(sqlEx);
            throw dataAccessException;
        }

        return super.customTranslate(task, sql, sqlEx);
    }
}
