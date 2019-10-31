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
                getQuery("OFFENDER_IDS"),
                createParams("offenderNo", offenderNumber),
                String.class);
    }

    private void deleteOffenderBookings(final String offenderId) {

        log.debug("Deleting all offender booking data for offender ID: '{}'", offenderId);

        jdbcTemplate.queryForList(
                getQuery("OFFENDER_BOOKING_IDS"),
                createParams("offenderId", offenderId),
                String.class)
                .forEach(this::deleteOffenderBooking);
    }

    private void deleteOffenderBooking(final String bookId) {

        log.debug("Deleting all offender booking data for book ID: '{}'", bookId);

        executeNamedSqlWithBookingId("DELETE_OFFENDER_COURSE_ATTENDANCES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_PRG_PRF_PAY_BANDS",bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_PROGRAM_PROFILES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_MEDICAL_TREATMENTS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_HEALTH_PROBLEMS", bookId);
        executeNamedSqlWithBookingId("DELETE_INCIDENT_CASE_PARTIES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_CURFEWS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_LANGUAGES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_VISIT_VISITORS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_VISITS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_VISIT_BALANCES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_OIC_SANCTIONS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_CONTACT_PERSONS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_KEY_WORKERS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_PHYSICAL_ATTRIBUTES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_IND_SCHEDULES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_SENT_CALCULATIONS", bookId);
        executeNamedSqlWithBookingId("DELETE_COURT_EVENTS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_CHARGES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_SENTENCE_TERMS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_SENTENCES", bookId);
        executeNamedSqlWithBookingId("DELETE_ORDERS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_CASES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_RELEASE_DETAILS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_ALERTS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_CASE_NOTES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_ASSESSMENTS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_PROFILE_DETAILS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_KEY_DATE_ADJUSTS", bookId);
        executeNamedSqlWithBookingId("DELETE_BED_ASSIGNMENT_HISTORIES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_IMPRISON_STATUSES", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_IEP_LEVELS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_EXTERNAL_MOVEMENTS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_PRG_OBLIGATIONS", bookId);
        executeNamedSqlWithBookingId("DELETE_OFFENDER_BOOKING_DETAILS", bookId);
    }

    private void deleteOffenderData(final String offenderId) {

        log.debug("Deleting all (non-booking) offender data for offender ID: '{}'", offenderId);

        executeNamedSqlWithOffenderId("DELETE_OFFENDER_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("DELETE_GL_TRANSACTIONS", offenderId);
        executeNamedSqlWithOffenderId("DELETE_OFFENDER_SUB_ACCOUNTS", offenderId);
        executeNamedSqlWithOffenderId("DELETE_OFFENDER_TRUST_ACCOUNTS", offenderId);
        executeNamedSqlWithOffenderId("DELETE_OFFENDER_IDENTIFIERS", offenderId);

        var bookingRowsDeleted = executeNamedSqlWithOffenderId("DELETE_OFFENDER_BOOKINGS", offenderId);

        executeNamedSqlWithOffenderId("DELETE_OFFENDER", offenderId);

        log.info("Deleted {} bookings for offender ID: {}", bookingRowsDeleted, offenderId);
    }

    private int executeNamedSqlWithOffenderId(final String sql, final String id) {
        return jdbcTemplate.update(getQuery(sql), createParams("offenderId", id));
    }

    private void executeNamedSqlWithBookingId(final String sql, final String id) {
        jdbcTemplate.update(getQuery(sql), createParams("bookId", id));
    }
}
