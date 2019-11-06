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

        deleteAgencyIncidents(bookId);
        deleteOffenderCases(bookId);
        deleteOffenderContactPersons(bookId);
        deleteOffenderCSIPReports(bookId);
        deleteOffenderCurfews(bookId);
        deleteOffenderGangAffiliations(bookId);
        deleteOffenderHealthProblems(bookId);
        deleteOffenderLIDSKeyDates(bookId);
        deleteOffenderNonAssociations(bookId);
        deleteOffenderRehabDecisions(bookId);
        deleteOffenderSentCalculations(bookId);
        deleteOffenderSubstanceUses(bookId);
        deleteOffenderVisits(bookId);
        deleteOffenderVisitBalances(bookId);
        deleteOffenderVisitOrders(bookId);
        deleteOffenderVSCSentences(bookId);
        executeNamedSqlWithBookingId("OD_DELETE_BED_ASSIGNMENT_HISTORIES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_CASE_ASSOCIATED_PERSONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_INCIDENT_CASE_PARTIES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_IWP_DOCUMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_ALERTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_ASSESSMENT_ITEMS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_ASSESSMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_BOOKING_AGY_LOCS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_BOOKING_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_BOOKING_EVENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_ASSOCIATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_OFFICERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_DATA_CORRECTIONS_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_DISCHARGE_BALANCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_EDUCATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_EMPLOYMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_EXTERNAL_MOVEMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_FINE_PAYMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_FIXED_TERM_RECALLS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IDENTIFYING_MARKS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IEP_LEVELS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IMAGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IMPRISON_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_INTER_MVMT_LOCATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_KEY_WORKERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LANGUAGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_MILITARY_RECORDS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_NO_PAY_PERIODS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_OGRS3_RISK_PREDICTORS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PAY_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PHYSICAL_ATTRIBUTES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PPTY_CONTAINERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PROFILES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PROFILE_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_RELEASE_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_RELEASE_DETAILS_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_RESTRICTIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_RISK_PREDICTORS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SUPERVISING_COURTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TEAM_ASSIGNMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TEAM_ASSIGN_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TEST_SELECTIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TMP_REL_SCHEDULES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_TRUST_ACCOUNTS_TEMP", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VSC_SENT_CALCULATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_TASK_ASSIGNMENT_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_WORKFLOW_HISTORY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VSC_ERROR_LOGS", bookId);
    }

    private void deleteOffenderCases(final String bookId) {
        deleteOrders(bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_IDENTIFIERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASES", bookId);
    }

    private void deleteOrders(final String bookId) {
        deleteOffenderSentences(bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_REORDER_SENTENCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_ORDER_PURPOSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_ORDERS", bookId);
        deleteCourtEventsAndOffenderCharges(bookId);
    }

    private void deleteOffenderSentences(final String bookId) {
        deleteOffenderCaseNotes(bookId);
        deleteOffenderSentConditions(bookId);
        deleteOffenderSentenceAdjusts(bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LICENCE_CONDITIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LICENCE_RECALLS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LICENCE_SENTENCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_CHARGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_TERMS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_UA_EVENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCES", bookId);
    }

    private void deleteOffenderSentConditions(final String bookId) {
        deleteOffenderPrgObligations(bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENT_COND_STATUSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENT_CONDITIONS", bookId);
    }

    private void deleteOffenderPrgObligations(final String bookId) {
        deleteOffenderMovementApps(bookId);
        deleteOffenderProgramProfiles(bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PRG_OBLIGATION_HTY", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PRG_OBLIGATIONS", bookId);
    }

    private void deleteOffenderMovementApps(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IND_SCH_SENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_IND_SCHEDULES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_MOVEMENT_APPS", bookId);
    }

    private void deleteOffenderSentenceAdjusts(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENTENCE_ADJUSTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_KEY_DATE_ADJUSTS", bookId);
    }

    private void deleteOffenderProgramProfiles(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_COURSE_ATTENDANCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_EXCLUDE_ACTS_SCHDS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PRG_PRF_PAY_BANDS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PROGRAM_PROFILES", bookId);
    }

    private void deleteOffenderCaseNotes(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_NOTE_SENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFF_CASE_NOTE_RECIPIENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CASE_NOTES", bookId);
    }

    private void deleteCourtEventsAndOffenderCharges(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_COURT_EVENT_CHARGES_BY_EVENT_ID", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_COURT_EVENT_CHARGES_BY_OFFENDER_CHARGE_ID", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_COURT_EVENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CHARGES", bookId);
    }

    private void deleteAgencyIncidents(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_OIC_SANCTIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OIC_HEARING_RESULTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OIC_HEARING_COMMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OIC_HEARING_NOTICES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OIC_HEARINGS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_AGY_INC_INV_STATEMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_AGY_INC_INVESTIGATIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_AGENCY_INCIDENT_CHARGES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_AGENCY_INCIDENT_PARTIES", bookId);
    }

    private void deleteOffenderContactPersons(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_PERSON_RESTRICTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CONTACT_PERSONS", bookId);
    }

    private void deleteOffenderCSIPReports(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CSIP_FACTORS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CSIP_INTVW", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CSIP_PLANS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CSIP_ATTENDEES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CSIP_REVIEWS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CSIP_REPORTS", bookId);
    }

    private void deleteCurfewAddresses(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_CURFEW_ADDRESS_OCCUPANTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_CURFEW_ADDRESSES", bookId);
    }

    private void deleteHDCRequestReferrals(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_HDC_PROB_STAFF_RESPONSES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_PROB_STAFF_COMMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_BOARD_DECISIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_GOVERNOR_DECISIONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_REQUEST_REFERRALS", bookId);
    }

    private void deleteHDCStatusTrackings(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_HDC_STATUS_REASONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_STATUS_TRACKINGS", bookId);
    }

    private void deleteOffenderCurfews(final String bookId) {
        deleteCurfewAddresses(bookId);
        deleteHDCRequestReferrals(bookId);
        deleteHDCStatusTrackings(bookId);
        executeNamedSqlWithBookingId("OD_DELETE_HDC_PRISON_STAFF_COMMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_CURFEWS", bookId);
    }

    private void deleteOffenderGangAffiliations(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_GANG_INVESTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_GANG_EVIDENCES", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_GANG_AFFILIATIONS", bookId);
    }

    private void deleteOffenderHealthProblems(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_MEDICAL_TREATMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_HEALTH_PROBLEMS", bookId);
    }

    private void deleteOffenderLIDSKeyDates(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LIDS_REMAND_DAYS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_LIDS_KEY_DATES", bookId);
    }

    private void deleteOffenderNonAssociations(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_NA_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_NON_ASSOCIATIONS", bookId);
    }

    private void deleteOffenderRehabDecisions(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_REHAB_PROVIDERS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_REHAB_DECISIONS", bookId);
    }

    private void deleteOffenderSentCalculations(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_HDC_CALC_EXCLUSION_REASONS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SENT_CALCULATIONS", bookId);
    }

    private void deleteOffenderSubstanceUses(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SUBSTANCE_DETAILS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SUBSTANCE_TREATMENTS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_SUBSTANCE_USES", bookId);
    }

    private void deleteOffenderVisits(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISIT_VISITORS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISITS", bookId);
    }

    private void deleteOffenderVisitBalances(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISIT_BALANCE_ADJS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISIT_BALANCES", bookId);
    }

    private void deleteOffenderVisitOrders(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VO_VISITORS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VISIT_ORDERS", bookId);
    }

    private void deleteOffenderVSCSentences(final String bookId) {
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VSC_SENTENCE_TERMS", bookId);
        executeNamedSqlWithBookingId("OD_DELETE_OFFENDER_VSC_SENTENCES", bookId);
    }

    private void deleteOffenderBeneficiaries(final String offenderId) {
        executeNamedSqlWithOffenderId("OD_DELETE_BENEFICIARY_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_BENEFICIARIES", offenderId);
    }

    private void deleteOffenderDeductions(final String offenderId) {
        deleteOffenderBeneficiaries(offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_ADJUSTMENT_TXNS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_DEDUCTION_RECEIPTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_DEDUCTIONS", offenderId);
    }

    private void deleteOffenderTransactions(final String offenderId) {
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_TRANSACTION_DETAILS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_TRANSACTIONS", offenderId);
    }

    private void deleteOffenderData(final String offenderId) {

        log.debug("Deleting all (non-booking) offender data for offender ID: '{}'", offenderId);

        deleteAddresses(offenderId);
        deleteOffenderFinances(offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_BANK_CHEQUE_BENEFICIARIES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_GL_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_DAMAGE_OBLIGATIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_FREEZE_DISBURSEMENTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_IDENTIFIERS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_MINIMUM_BALANCES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_SYSTEM_REPORT_REQUESTS", offenderId);

        executeNamedSqlWithOffenderId("OD_DELETE_MERGE_TRANSACTION_LOGS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_MERGE_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_LOCKED_MODULES", offenderId);

        var bookingRowsDeleted = executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_BOOKINGS", offenderId);

        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER", offenderId);

        log.info("Deleted {} bookings for offender ID: {}", bookingRowsDeleted, offenderId);
    }

    private void deleteAddresses(final String offenderId) {
        executeNamedSqlWithOffenderId("OD_DELETE_ADDRESS_PHONES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_ADDRESS_USAGES", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_ADDRESSES", offenderId);
    }

    private void deleteOffenderFinances(final String offenderId) {
        deleteOffenderTransactions(offenderId);
        deleteOffenderDeductions(offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_SUB_ACCOUNTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_TRUST_ACCOUNTS", offenderId);
        executeNamedSqlWithOffenderId("OD_DELETE_OFFENDER_PAYMENT_PROFILES", offenderId);
    }

    private int executeNamedSqlWithOffenderId(final String sql, final String id) {
        return jdbcTemplate.update(getQuery(sql), createParams("offenderId", id));
    }

    private void executeNamedSqlWithBookingId(final String sql, final String id) {
        jdbcTemplate.update(getQuery(sql), createParams("bookId", id));
    }
}
