package net.syscon.elite.service.impl;

import lombok.val;
import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseloadToAgencyMappingService;
import net.syscon.elite.service.OffenderCurfewService;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDate;

import static net.syscon.elite.repository.OffenderCurfewRepositoryTest.createNewCurfewForBookingId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Integration tests for the OffenderCurfewServiceImpl + OffenderCurfewRepositoryImpl combination. These tests
 * seem necessary because the desired service behaviour relies upon interactions between the service and database triggers.
 */
@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class OffenderCurfewServiceIntegrationTest {

    private static final long OFFENDER_BOOKING_ID = -51L;

    @Autowired
    private OffenderCurfewRepository offenderCurfewRepository;

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    @Mock
    private BookingService bookingService;

    @Mock
    private CaseloadToAgencyMappingService caseloadToAgencyMappingService;

    @Mock
    private ReferenceDomainService referenceDomainService;

    private OffenderCurfewService offenderCurfewService;

    private static final LocalDate checksPassedDate = LocalDate.of(2019, 5, 1);
    private static final LocalDate approvalStatusDate = LocalDate.of(2019, 5, 2);

    @Before
    public void configureService() {
        offenderCurfewService = new OffenderCurfewServiceImpl(
                offenderCurfewRepository,
                caseloadToAgencyMappingService,
                bookingService,
                referenceDomainService,
                Clock.systemUTC());

        when(referenceDomainService.isReferenceCodeActive(anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void givenHdcChecksPassed_whenApproved_thenCurfewStateCanBeRetrieved() {
        val curfewId = createNewCurfewForBookingId(OFFENDER_BOOKING_ID, jdbcTemplate);

        offenderCurfewService.setHdcChecks(
                OFFENDER_BOOKING_ID,
                HdcChecks
                        .builder()
                        .passed(true)
                        .date(checksPassedDate)
                        .build());

        assertThat(offenderCurfewService.getLatestHomeDetentionCurfew(OFFENDER_BOOKING_ID))
                .isEqualTo(
                        HomeDetentionCurfew
                                .builder()
                                .id(curfewId)
                                .passed(true)
                                .checksPassedDate(checksPassedDate)
                        .build()
                );

        offenderCurfewService.setApprovalStatus(
                OFFENDER_BOOKING_ID,
                ApprovalStatus
                        .builder()
                        .approvalStatus("APPROVED")
                        .date(approvalStatusDate)
                        .build());

        assertThat(offenderCurfewService.getLatestHomeDetentionCurfew(OFFENDER_BOOKING_ID))
                .isEqualTo(
                        HomeDetentionCurfew
                                .builder()
                                .id(curfewId)
                                .passed(true)
                                .checksPassedDate(checksPassedDate)
                                .approvalStatus("APPROVED")
                                .approvalStatusDate(approvalStatusDate)
                                .build()
                );
    }

    @Test
    public void givenHdcChecksPassed_whenRejected_thenCurfewStateCanBeRetrieved() {
        val curfewId = createNewCurfewForBookingId(OFFENDER_BOOKING_ID, jdbcTemplate);

        offenderCurfewService.setHdcChecks(
                OFFENDER_BOOKING_ID,
                HdcChecks
                        .builder()
                        .passed(true)
                        .date(checksPassedDate)
                        .build());

        assertThat(offenderCurfewService.getLatestHomeDetentionCurfew(OFFENDER_BOOKING_ID))
                .isEqualTo(
                        HomeDetentionCurfew
                                .builder()
                                .id(curfewId)
                                .passed(true)
                                .checksPassedDate(checksPassedDate)
                                .build()
                );

        offenderCurfewService.setApprovalStatus(
                OFFENDER_BOOKING_ID,
                ApprovalStatus
                        .builder()
                        .approvalStatus("REJECTED")
                        .refusedReason("OTHER")
                        .date(LocalDate.of(2019, 5, 2))
                        .build());

        assertThat(offenderCurfewService.getLatestHomeDetentionCurfew(OFFENDER_BOOKING_ID))
                .isEqualTo(
                        HomeDetentionCurfew
                                .builder()
                                .id(curfewId)
                                .passed(true)
                                .checksPassedDate(checksPassedDate)
                                .approvalStatus("REJECTED")
                                .refusedReason("OTHER")
                                .approvalStatusDate(approvalStatusDate)
                                .build()
                );
    }

    @Test
    public void givenHdcChecksNotPassed_whenRejected_thenCurfewStateCanBeRetrieved() {
        val curfewId = createNewCurfewForBookingId(OFFENDER_BOOKING_ID, jdbcTemplate);

        offenderCurfewService.setHdcChecks(
                OFFENDER_BOOKING_ID,
                HdcChecks
                        .builder()
                        .passed(false)
                        .date(checksPassedDate)
                        .build());

        assertThat(offenderCurfewService.getLatestHomeDetentionCurfew(OFFENDER_BOOKING_ID))
                .isEqualTo(
                        HomeDetentionCurfew
                                .builder()
                                .id(curfewId)
                                .passed(false)
                                .checksPassedDate(checksPassedDate)
                                .build()
                );

        offenderCurfewService.setApprovalStatus(
                OFFENDER_BOOKING_ID,
                ApprovalStatus
                        .builder()
                        .approvalStatus("REJECTED")
                        .refusedReason("OTHER")
                        .date(approvalStatusDate)
                        .build());

        assertThat(offenderCurfewService.getLatestHomeDetentionCurfew(OFFENDER_BOOKING_ID))
                .isEqualTo(
                        HomeDetentionCurfew
                                .builder()
                                .id(curfewId)
                                .passed(false)
                                .checksPassedDate(checksPassedDate)
                                .approvalStatus("REJECTED")
                                .refusedReason("OTHER")
                                .approvalStatusDate(approvalStatusDate)
                                .build()
                );
    }
}
