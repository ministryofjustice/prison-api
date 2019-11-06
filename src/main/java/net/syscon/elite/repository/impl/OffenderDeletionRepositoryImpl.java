package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.OffenderDeletionRepository;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class OffenderDeletionRepositoryImpl extends RepositoryBase implements OffenderDeletionRepository {

    @Override
    public void deleteOffender(final String offenderNumber) {

        log.info("Deleting all data for offender: '{}'", offenderNumber);

        final List<String> offenderIds = offenderIdsFor(offenderNumber);

        if (offenderIds.isEmpty()) {
            throw EntityNotFoundException.withId(offenderNumber);
        }

        offenderIds.forEach(this::deleteOffenderBookings);
        offenderIds.forEach(this::deleteOffenderData);

        log.info("Deleted {} offender records with offenderNumber: '{}'", offenderIds.size(), offenderNumber);
    }

    private List<String> offenderIdsFor(final String offenderNumber) {
        return jdbcTemplate.queryForList(
                getQuery("OD_OFFENDER_IDS"),
                createParams("offenderNo", offenderNumber),
                String.class);
    }

    private void deleteOffenderBookings(final String offenderId) {

        log.debug("Deleting all offender booking data for offender ID: '{}'", offenderId);

        jdbcTemplate.queryForList(
                getQuery("OD_OFFENDER_BOOKING_IDS"),
                createParams("offenderId", offenderId),
                String.class)
                .forEach(this::deleteOffenderBooking);
    }

    private void deleteOffenderBooking(final String bookId) {

        log.debug("Deleting all offender booking data for book ID: '{}'", bookId);

        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_OIC_SANCTIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_EXCLUDE_ACTS_SCHDS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_COURSE_ATTENDANCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PRG_PRF_PAY_BANDS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PROGRAM_PROFILES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_MEDICAL_TREATMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_HEALTH_PROBLEMS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_COURT_EVENT_CHARGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_COURT_EVENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_REHAB_PROVIDERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_REHAB_DECISIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OIC_HEARING_COMMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OIC_HEARING_NOTICES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OIC_HEARING_RESULTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OIC_HEARINGS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_AGENCY_INCIDENT_CHARGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_AGENCY_INCIDENT_PARTIES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_BOARD_DECISIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_GOVERNOR_DECISIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_REQUEST_REFERRALS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_CURFEW_ADDRESSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_STATUS_REASONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_STATUS_TRACKINGS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CURFEWS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_ASSESSMENT_ITEMS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISIT_ORDERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_INTER_MVMT_LOCATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IND_SCH_SENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IND_SCHEDULES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_MOVEMENT_APPS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_OFFICERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LICENCE_CONDITIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_IWP_DOCUMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IMAGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IDENTIFYING_MARKS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LICENCE_SENTENCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_DISCHARGE_BALANCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PROFILES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENT_CONDITIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_EMPLOYMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TEST_SELECTIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_ADJUSTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_EDUCATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PPTY_CONTAINERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TEAM_ASSIGNMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VSC_SENT_CALCULATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_TASK_ASSIGNMENT_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_NA_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_NON_ASSOCIATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_RESTRICTIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_OGRS3_RISK_PREDICTORS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_RELEASE_DETAILS_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VSC_ERROR_LOGS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_NO_PAY_PERIODS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_RISK_PREDICTORS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VSC_SENTENCE_TERMS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VSC_SENTENCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LIDS_KEY_DATES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TEAM_ASSIGN_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_PRISON_STAFF_COMMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SUBSTANCE_USES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SUBSTANCE_TREATMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TRUST_ACCOUNTS_TEMP", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CSIP_REPORTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_FIXED_TERM_RECALLS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_DATA_CORRECTIONS_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PAY_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_GANG_AFFILIATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_FINE_PAYMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_GANG_EVIDENCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_GANG_INVESTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_MILITARY_RECORDS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_ASSOCIATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SUBSTANCE_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LICENCE_RECALLS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_REORDER_SENTENCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_BOOKING_AGY_LOCS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_BOOKING_EVENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TMP_REL_SCHEDULES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LIDS_REMAND_DAYS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_UA_EVENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_CASE_ASSOCIATED_PERSONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SUPERVISING_COURTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_INCIDENT_CASE_PARTIES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LANGUAGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISIT_VISITORS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISITS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISIT_BALANCE_ADJS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISIT_BALANCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PERSON_RESTRICTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CONTACT_PERSONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_KEY_WORKERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PHYSICAL_ATTRIBUTES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_CALC_EXCLUSION_REASONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENT_CALCULATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_CHARGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_ORPHANED_COURT_EVENT_CHARGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CHARGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_TERMS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_NOTE_SENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_ORDERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_IDENTIFIERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_RELEASE_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_ALERTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFF_CASE_NOTE_RECIPIENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_NOTES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_ASSESSMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PROFILE_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_KEY_DATE_ADJUSTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_BED_ASSIGNMENT_HISTORIES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IMPRISON_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IEP_LEVELS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_EXTERNAL_MOVEMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PRG_OBLIGATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_BOOKING_DETAILS", bookId);
    }

    private void deleteOffenderData(final String offenderId) {

        log.debug("Deleting all (non-booking) offender data for offender ID: '{}'", offenderId);

        executeNamedSqlWithOffenderId("OD_DELETE_ADDRESS_PHONES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_ADDRESS_USAGES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_ADDRESSES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_BENEFICIARY_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_BENEFICIARIES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_DEDUCTION_RECEIPTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_ADJUSTMENT_TXNS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_DEDUCTIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_PAYMENT_PROFILES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_MERGE_TRANSACTION_LOGS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_MERGE_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_DAMAGE_OBLIGATIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_SYSTEM_REPORT_REQUESTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_MINIMUM_BALANCES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_FREEZE_DISBURSEMENTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_BANK_CHEQUE_BENEFICIARIES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_LOCKED_MODULES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_GL_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_SUB_ACCOUNTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_TRUST_ACCOUNTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_IDENTIFIERS", offenderId);

        var bookingRowsDeleted = executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_BOOKINGS", offenderId);

        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER", offenderId);

        log.info("Deleted {} bookings for offender ID: {}", bookingRowsDeleted, offenderId);
    }

    private int executeNamedSqlWithOffenderId(final String sql, final String id) {
        return jdbcTemplate.update(getQuery(sql), createParams("offenderId", id));
    }

    private void executeNamedSqlWithBookingId(final String sql, final String id) {
        jdbcTemplate.update(getQuery(sql), createParams("bookId", id));
    }
}
