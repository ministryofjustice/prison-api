package net.syscon.elite.repository;


import net.syscon.elite.service.support.OffenderCurfew;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)

public class OffenderCurfewRepositoryTest {

    @Autowired
    private OffenderCurfewRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void shouldReturnOffenderBookIdsWhereLatestCurfewRecordsHaveNullApprovalStatus_LEI_only() {
        Collection<Long> results = repository.offendersWithoutCurfewApprovalStatus("agencyLocationId:eq:'LEI'");
        assertThat(results).containsOnly(-3L, -4L, -6L, -7L);
    }

    @Test
    public void shouldReturnOffenderBookIdsWhereLatestCurfewRecordsHaveNullApprovalStatus_BXI_only() {
        Collection<Long> results = repository.offendersWithoutCurfewApprovalStatus("agencyLocationId:eq:'BXI'");
        assertThat(results).containsOnly(-36L);
    }

    @Test
    public void shouldReturnAllOffenderCurfewForAgency() {
        Collection<OffenderCurfew> results = repository.offenderCurfews(Collections.singleton("LEI"));
        assertThat(results).containsOnly(
                offenderCurfew(1, -1, "2018-01-01", null, "2018-03-01"),
                offenderCurfew(2, -1, "2018-02-01", "APPROVED", "2018-03-01"),
                offenderCurfew(3, -1, "2018-02-01", "REJECTED", "2018-03-01"),
                offenderCurfew(4, -2, "2018-01-01", "APPROVED", "2018-03-01"),
                offenderCurfew(5, -2, "2018-02-01", null, null),
                offenderCurfew(6, -2, "2018-02-01", "APPROVED", null),
                offenderCurfew(7, -3, "2018-01-01", "REJECTED", null),
                offenderCurfew(8, -3, "2018-02-01", "REJECTED", null),
                offenderCurfew(9, -3, "2018-02-01", null, null),
                offenderCurfew(10, -4, "2018-01-01", null, null),
                offenderCurfew(11, -5, "2018-01-01", "APPROVED", null),
                offenderCurfew(12, -6, "2018-01-01", "APPROVED", "2018-04-01"),
                offenderCurfew(13, -6, null, null, null),
                offenderCurfew(14, -6, "2018-02-01", "APPROVED", null),
                offenderCurfew(15, -7, null, "REJECTED", null),
                offenderCurfew(16, -7, "2018-01-01", "REJECTED", null),
                offenderCurfew(17, -7, "2018-01-01", "REJECTED", "2018-04-01"),
                offenderCurfew(18, -7, null, null, null),
                offenderCurfew(19, -7, "2018-02-01", "REJECTED", null));
    }

    private OffenderCurfew offenderCurfew(long offenderCurfewId, long offenderBookId, String assessmentDate, String approvalStatus, String ardCrdDate) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .assessmentDate(toLocalDate(assessmentDate))
                .approvalStatus(approvalStatus)
                .ardCrdDate(toLocalDate(ardCrdDate))
                .build();
    }

    private LocalDate toLocalDate(String string) {
        if (string == null) return null;
        return LocalDate.parse(string);
    }
}
