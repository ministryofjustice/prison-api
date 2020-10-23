package uk.gov.justice.hmpps.prison.repository.v1;

import com.google.common.collect.ImmutableMap;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;

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
    private static final ImmutableMap<Integer, String> SQL_EXCEPTION_MAP = ImmutableMap.<Integer, String>builder()
            .put(20002, IDENTIFIER_INCONSISTANCY)
            .put(20003, NO_OFFENDER_IDENTIFIER)
            .put(20004, VISITOR_ISSUE)
            .put(20005, OUT_OR_IN_TRANSIT)
            .put(20006, LOCKED)
            .put(20007, LEAD_VISITOR_ISSUE)
            .put(20009, INSUFFICIENT_FUNDS)
            .put(20010, NOT_DIGITAL_PRISON)
            .put(20011, NO_TRUST_ACCOUNT)
            .put(20013, FINANCE_EXCEPTION)
            .put(20014, SLOT_NOT_AVAIILABLE)
            .put(20015, OVERLAPPING_VISIT)
            .put(20016, NO_VO_PVO_BALANCE)
            .put(20017, NOT_IN_SPECIFIED_PRISON)
            .put(20018, INVALID_TRANSACTION_TYPE)
            .put(20020, SUB_ACCNT_NOT_EXIST)
            .put(20021, NO_SUB_ACCT_BALANCE)
            .put(20022, HOLD_AMOUNT_NOT_EXCEED)
            .put(20023, HOLD_DATE_IN_PAST)
            .put(20024, HOLD_NOT_FOUND)
            .put(20025, VISIT_CANCELLED)
            .put(20026, VISIT_COMPLETED)
            .put(20027, VISIT_NOT_FOUND)
            .put(20028, INVALID_CANC_CODE)
            .put(20029, CLOSED_VISITS)
            .put(20030, INVALID_RELATIONSHIP_TYPE)
            .put(20031, INVALID_CONTACT_ID)
            .put(20032, INVALID_CITY_CODE)
            .put(20033, INVALID_COUNTY_CODE)
            .put(20034, INVALID_COUNTRY_CODE)
            .put(20035, DUPLICATE_CONTACT)
            .put(20036, OFFENDER_STILL_IN_SPECIFIED_PRISON)
            .put(20037, OFFENDER_NEVER_AT_PRISON)
            .put(20038, OFFENDER_TRANSFERRED)
            .put(20039, FINANCIAL_SETUP_ERROR)
            .put(20040, SUB_ACCOUNT_NOT_EQUAL_TO_CURRENT_BALANCE)
            .put(20041, DISBURSEMENT_ONLY)
            .put(20042, RECEIPT_ONLY).build();



    protected DataAccessException customTranslate(final String task, final String sql, final SQLException sqlEx) {
        final var errorCode = sqlEx.getErrorCode();

        final var dataAccessException = translate(errorCode, sqlEx);
        return dataAccessException.orElseGet(() -> super.customTranslate(task, sql, sqlEx));
    }

    private Optional<DataAccessException> translate(final int errorCode, final SQLException sqlEx) {
        if (errorCode == 20001) throw new EmptyResultDataAccessException(OFFENDER_NOT_FOUND, 1, sqlEx);
        if (errorCode == 20012) throw new EmptyResultDataAccessException(PRISON_NOT_FOUND, 1, sqlEx);
        if (errorCode == 20019) throw new DuplicateKeyException(DUPLICATE_POST, sqlEx);

        final var msg = SQL_EXCEPTION_MAP.get(errorCode);
        return Optional.ofNullable(msg).map(m -> new InvalidDataAccessApiUsageException(m, sqlEx));
    }
}
