package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.OffenderDeletionRepository;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class OffenderDeletionRepositoryImpl extends RepositoryBase implements OffenderDeletionRepository {

    @Value("${data.compliance.db.enable.parallel.hints:true}")
    private boolean enableParallelHints;

    /**
     * Deletes all data associated with an offender with the given offender number,
     * with the exception of the GL_TRANSACTIONS (General Ledger) table - associated rows
     * are anonymised by setting offender_id and offender_book_id to null.
     *
     * @return Set of offender_ids of the deleted offender aliases.
     */
    @Override
    public Set<String> deleteOffender(final String offenderNumber) {

        log.info("Deleting all data for offender: '{}'", offenderNumber);

        final Set<String> offenderIds = offenderIdsFor(offenderNumber);

        if (offenderIds.isEmpty()) {
            throw EntityNotFoundException.withId(offenderNumber);
        }

        if (enableParallelHints) {
            getJdbcTemplateBase().execute(getQuery("OD_ENABLE_PARALLEL_HINTS"));
        }

        deleteOffenderBookings(offenderIds);
        deleteOffenderData(offenderIds);

        log.info("Deleted {} offender records with offenderNumber: '{}'", offenderIds.size(), offenderNumber);

        return offenderIds;
    }

    private Set<String> offenderIdsFor(final String offenderNumber) {
        return new HashSet<>(jdbcTemplate.queryForList(
                getQuery("OD_OFFENDER_IDS"),
                createParams("offenderNo", offenderNumber),
                String.class));
    }

    private void deleteOffenderBookings(final Set<String> offenderIds) {

        log.debug("Deleting all offender booking data for offender ID: '{}'", offenderIds);

        final var bookIds = new HashSet<>(jdbcTemplate.queryForList(
                getQuery("OD_OFFENDER_BOOKING_IDS"),
                createParams("offenderIds", offenderIds),
                String.class));

        if (!bookIds.isEmpty()) {
            deleteOffenderBooking(bookIds);
        }

        executeNamedSqlWithOffenderIdsAndBookingIds("OD_ANONYMISE_GL_TRANSACTIONS", offenderIds, bookIds);
        executeNamedSqlWithOffenderIdsAndBookingIds("OD_DELETE_OFFENDER_BELIEFS", offenderIds, bookIds);
    }

    private void deleteOffenderBooking(final Set<String> bookIds) {

        log.debug("Deleting all offender booking data for book ID: '{}'", bookIds);

        deleteContactDetailsByBookIds(bookIds);
        deleteWorkFlows(bookIds);
        deleteAgencyIncidents(bookIds);
        deleteOffenderCases(bookIds);
        deleteOffenderContactPersons(bookIds);
        deleteOffenderCSIPReports(bookIds);
        deleteOffenderCurfews(bookIds);
        deleteOffenderGangAffiliations(bookIds);
        deleteOffenderHealthProblems(bookIds);
        deleteOffenderLIDSKeyDates(bookIds);
        deleteOffenderNonAssociations(bookIds);
        deleteOffenderRehabDecisions(bookIds);
        deleteOffenderSentCalculations(bookIds);
        deleteOffenderSubstanceUses(bookIds);
        deleteOffenderVisits(bookIds);
        deleteOffenderVisitBalances(bookIds);
        deleteOffenderVisitOrders(bookIds);
        deleteOffenderVSCSentences(bookIds);
        deleteIncidentCases(bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_BED_ASSIGNMENT_HISTORIES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_CASE_ASSOCIATED_PERSONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_IWP_DOCUMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_ALERTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_ASSESSMENT_ITEMS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_ASSESSMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_BOOKING_AGY_LOCS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_BOOKING_DETAILS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_BOOKING_EVENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CASE_ASSOCIATIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CASE_OFFICERS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_DATA_CORRECTIONS_HTY", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_DISCHARGE_BALANCES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_EDUCATIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_EMPLOYMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_EXTERNAL_MOVEMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_FINE_PAYMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_FIXED_TERM_RECALLS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_IDENTIFYING_MARKS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_IEP_LEVELS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_IMAGES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_IMPRISON_STATUSES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_INTER_MVMT_LOCATIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_KEY_WORKERS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_LANGUAGES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_MILITARY_RECORDS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_NO_PAY_PERIODS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_OGRS3_RISK_PREDICTORS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PAY_STATUSES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PHYSICAL_ATTRIBUTES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PPTY_CONTAINERS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PROFILES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PROFILE_DETAILS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_RELEASE_DETAILS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_RELEASE_DETAILS_HTY", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_RESTRICTIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_RISK_PREDICTORS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SUPERVISING_COURTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_TEAM_ASSIGNMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_TEAM_ASSIGN_HTY", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_TEST_SELECTIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_TMP_REL_SCHEDULES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_TRUST_ACCOUNTS_TEMP", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VSC_ERROR_LOGS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VSC_SENT_CALCULATIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_TASK_ASSIGNMENT_HTY", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_WORKFLOW_HISTORY", bookIds);
    }

    private void deleteContactDetailsByBookIds(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_INTERNET_ADDRESSES_BY_BOOK_IDS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_PHONES_BY_BOOK_IDS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_ADDRESS_USAGES_BY_BOOK_IDS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_ADDRESSES_BY_BOOK_IDS", bookIds);
    }

    private void deleteWorkFlows(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_WORK_FLOW_LOGS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_WORK_FLOWS", bookIds);
    }

    private void deleteOffenderCases(final Set<String> bookIds) {
        deleteOrders(bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CASE_IDENTIFIERS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CASE_STATUSES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CASES", bookIds);
    }

    private void deleteOrders(final Set<String> bookIds) {
        deleteOffenderSentences(bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_REORDER_SENTENCES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_ORDER_PURPOSES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_ORDERS", bookIds);
        deleteCourtEventsAndOffenderCharges(bookIds);
    }

    private void deleteOffenderSentences(final Set<String> bookIds) {
        deleteOffenderCaseNotes(bookIds);
        deleteOffenderSentConditions(bookIds);
        deleteOffenderSentenceAdjusts(bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_LICENCE_CONDITIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_LICENCE_RECALLS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_LICENCE_SENTENCES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENTENCE_CHARGES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENTENCE_STATUSES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENTENCE_TERMS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENTENCE_UA_EVENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENTENCES", bookIds);
    }

    private void deleteOffenderCaseNotes(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CASE_NOTE_SENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFF_CASE_NOTE_RECIPIENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CASE_NOTES", bookIds);
    }

    private void deleteOffenderSentConditions(final Set<String> bookIds) {
        deleteOffenderPrgObligations(bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENT_COND_STATUSES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENT_CONDITIONS", bookIds);
    }

    private void deleteOffenderPrgObligations(final Set<String> bookIds) {
        deleteOffenderMovementApps(bookIds);
        deleteOffenderProgramProfiles(bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PRG_OBLIGATION_HTY", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PRG_OBLIGATIONS", bookIds);
    }

    private void deleteOffenderMovementApps(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_IND_SCH_SENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_IND_SCHEDULES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_MOVEMENT_APPS", bookIds);
    }

    private void deleteOffenderProgramProfiles(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_COURSE_ATTENDANCES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_EXCLUDE_ACTS_SCHDS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PRG_PRF_PAY_BANDS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PROGRAM_PROFILES", bookIds);
    }

    private void deleteOffenderSentenceAdjusts(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENTENCE_ADJUSTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_KEY_DATE_ADJUSTS", bookIds);
    }

    private void deleteCourtEventsAndOffenderCharges(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_LINK_CASE_TXNS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_COURT_EVENT_CHARGES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_COURT_EVENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CHARGES", bookIds);
    }

    private void deleteAgencyIncidents(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_OIC_SANCTIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OIC_HEARING_RESULTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OIC_HEARING_COMMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OIC_HEARING_NOTICES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OIC_HEARINGS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_AGY_INC_INV_STATEMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_AGY_INC_INVESTIGATIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_AGENCY_INCIDENT_CHARGES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_AGENCY_INCIDENT_PARTIES", bookIds);
    }

    private void deleteOffenderContactPersons(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_PERSON_RESTRICTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CONTACT_PERSONS", bookIds);
    }

    private void deleteOffenderCSIPReports(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CSIP_FACTORS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CSIP_INTVW", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CSIP_PLANS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CSIP_ATTENDEES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CSIP_REVIEWS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CSIP_REPORTS", bookIds);
    }

    private void deleteOffenderCurfews(final Set<String> bookIds) {
        deleteCurfewAddresses(bookIds);
        deleteHDCRequestReferrals(bookIds);
        deleteHDCStatusTrackings(bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_PRISON_STAFF_COMMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_CURFEWS", bookIds);
    }

    private void deleteCurfewAddresses(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_CURFEW_ADDRESS_OCCUPANTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_CURFEW_ADDRESSES", bookIds);
    }

    private void deleteHDCRequestReferrals(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_PROB_STAFF_RESPONSES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_PROB_STAFF_COMMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_BOARD_DECISIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_GOVERNOR_DECISIONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_REQUEST_REFERRALS", bookIds);
    }

    private void deleteHDCStatusTrackings(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_STATUS_REASONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_STATUS_TRACKINGS", bookIds);
    }

    private void deleteOffenderGangAffiliations(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_GANG_INVESTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_GANG_EVIDENCES", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_GANG_AFFILIATIONS", bookIds);
    }

    private void deleteOffenderHealthProblems(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_MEDICAL_TREATMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_HEALTH_PROBLEMS", bookIds);
    }

    private void deleteOffenderLIDSKeyDates(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_LIDS_REMAND_DAYS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_LIDS_KEY_DATES", bookIds);
    }

    private void deleteOffenderNonAssociations(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_NA_DETAILS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_NON_ASSOCIATIONS", bookIds);
    }

    private void deleteOffenderRehabDecisions(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_REHAB_PROVIDERS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_REHAB_DECISIONS", bookIds);
    }

    private void deleteOffenderSentCalculations(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_HDC_CALC_EXCLUSION_REASONS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SENT_CALCULATIONS", bookIds);
    }

    private void deleteOffenderSubstanceUses(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SUBSTANCE_DETAILS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SUBSTANCE_TREATMENTS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_SUBSTANCE_USES", bookIds);
    }

    private void deleteOffenderVisits(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VISIT_VISITORS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VISITS", bookIds);
    }

    private void deleteOffenderVisitBalances(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VISIT_BALANCE_ADJS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VISIT_BALANCES", bookIds);
    }

    private void deleteOffenderVisitOrders(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VO_VISITORS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VISIT_ORDERS", bookIds);
    }

    private void deleteOffenderVSCSentences(final Set<String> bookIds) {
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VSC_SENTENCE_TERMS", bookIds);
        executeNamedSqlWithBookingIds("OD_DELETE_OFFENDER_VSC_SENTENCES", bookIds);
    }

    private Set<String> incidentCaseIdsFor(final Set<String> bookIds) {
        return new HashSet<>(jdbcTemplate.queryForList(
                getQuery("OD_INCIDENT_CASES"),
                createParams("bookIds", bookIds),
                String.class));
    }

    private void deleteIncidentCases(final Set<String> bookIds) {

        final var incidentCaseIds = incidentCaseIdsFor(bookIds);

        if (!incidentCaseIds.isEmpty()) {
            executeNamedSqlWithIncidentCaseIds("OD_DELETE_INCIDENT_CASE_PARTIES", incidentCaseIds);
            executeNamedSqlWithIncidentCaseIds("OD_DELETE_INCIDENT_CASE_RESPONSES", incidentCaseIds);
            executeNamedSqlWithIncidentCaseIds("OD_DELETE_INCIDENT_CASE_QUESTIONS", incidentCaseIds);
            executeNamedSqlWithIncidentCaseIds("OD_DELETE_INCIDENT_QUESTIONNAIRE_HTY", incidentCaseIds);
            executeNamedSqlWithIncidentCaseIds("OD_DELETE_INCIDENT_CASE_REQUIREMENTS", incidentCaseIds);
            executeNamedSqlWithIncidentCaseIds("OD_DELETE_INCIDENT_CASES", incidentCaseIds);
        }
    }

    private void deleteOffenderData(final Set<String> offenderIds) {

        log.debug("Deleting all (non-booking) offender data for offender ID: '{}'", offenderIds);

        deleteContactDetailsByOffenderIds(offenderIds);
        deleteOffenderFinances(offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_BANK_CHEQUE_BENEFICIARIES", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_DAMAGE_OBLIGATIONS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_FREEZE_DISBURSEMENTS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_IDENTIFIERS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_MINIMUM_BALANCES", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_SYSTEM_REPORT_REQUESTS", offenderIds);

        executeNamedSqlWithOffenderIds("OD_DELETE_MERGE_TRANSACTION_LOGS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_MERGE_TRANSACTIONS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_LOCKED_MODULES", offenderIds);

        var bookingRowsDeleted = executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_BOOKINGS", offenderIds);

        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER", offenderIds);

        log.info("Deleted {} bookings for offender ID: {}", bookingRowsDeleted, offenderIds);
    }

    private void deleteContactDetailsByOffenderIds(final Set<String> offenderIds) {
        executeNamedSqlWithOffenderIds("OD_DELETE_INTERNET_ADDRESSES_BY_OFFENDER_IDS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_PHONES_BY_OFFENDER_IDS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_ADDRESS_USAGES_BY_OFFENDER_IDS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_ADDRESSES_BY_OFFENDER_IDS", offenderIds);
    }

    private void deleteOffenderFinances(final Set<String> offenderIds) {
        deleteOffenderTransactions(offenderIds);
        deleteOffenderDeductions(offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_SUB_ACCOUNTS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_TRUST_ACCOUNTS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_PAYMENT_PROFILES", offenderIds);
    }

    private void deleteOffenderTransactions(final Set<String> offenderIds) {
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_TRANSACTION_DETAILS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_TRANSACTIONS", offenderIds);
    }

    private void deleteOffenderDeductions(final Set<String> offenderIds) {
        deleteOffenderBeneficiaries(offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_ADJUSTMENT_TXNS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_DEDUCTION_RECEIPTS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_DEDUCTIONS", offenderIds);
    }

    private void deleteOffenderBeneficiaries(final Set<String> offenderIds) {
        executeNamedSqlWithOffenderIds("OD_DELETE_BENEFICIARY_TRANSACTIONS", offenderIds);
        executeNamedSqlWithOffenderIds("OD_DELETE_OFFENDER_BENEFICIARIES", offenderIds);
    }

    private int executeNamedSqlWithOffenderIds(final String sql, final Set<String> ids) {
        return jdbcTemplate.update(getQuery(sql), createParams("offenderIds", ids));
    }

    private void executeNamedSqlWithBookingIds(final String sql, final Set<String> ids) {
        jdbcTemplate.update(getQuery(sql), createParams("bookIds", ids));
    }

    private int executeNamedSqlWithOffenderIdsAndBookingIds(final String sql,
                                                            final Set<String> offenderIds,
                                                            final Set<String> bookIds) {
        return jdbcTemplate.update(getQuery(sql), createParams("offenderIds", offenderIds, "bookIds", bookIds.isEmpty() ? null : bookIds));
    }

    private void executeNamedSqlWithIncidentCaseIds(final String sql, final Set<String> ids) {
        jdbcTemplate.update(getQuery(sql), createParams("incidentCaseIds", ids));
    }
}
