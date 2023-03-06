package uk.gov.justice.hmpps.prison.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.Status;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.justice.hmpps.prison.service.UserService.STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION;

@ActiveProfiles("test")

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaseLoadRepository caseLoadRepository;

    @Test
    public void testFindUserByUsername() {
        final var user = userRepository.findByUsername("ITAG_USER").orElseThrow(EntityNotFoundException.withId("ITAG_USER"));

        assertThat(user.getLastName()).isEqualTo("User");
    }

    @Test
    public void testFindUserByUsernameNotExists() {
        final var user = userRepository.findByUsername("XXXXXXXX");
        assertThat(user).isNotPresent();
    }

    @Test
    public void testFindUsersByCaseload() {

        final var page = userRepository.findUsersByCaseload("LEI", null, new NameFilter(null), Status.ALL, null, new PageRequest("last_name", Order.ASC, 0L, 5L));
        final var items = page.getItems();

        assertThat(items).hasSizeBetween(5, 20);
        assertThat(items).extracting(UserDetail::getUsername).contains("JBRIEN");
    }

    @Test
    public void testFindUsersByCaseloadInactiveOnly() {

        final var page = userRepository.findUsersByCaseload("LEI", null, new NameFilter(null), Status.INACTIVE, null, new PageRequest("last_name", Order.ASC, 0L, 5L));
        final var items = page.getItems();

        assertThat(items).hasSize(0);
    }

    @Test
    public void testFindUsersByCaseloadAndNameFilter() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("User"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUserByFullNameSearch() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("User Api"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting(UserDetail::getUsername).containsExactly("ITAG_USER");
    }

    @Test
    public void testFindUserByFullNameSearchReversed() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("Api User"), Status.ALL, null, new PageRequest());
        assertThat(usersByCaseload.getItems()).extracting(UserDetail::getUsername).containsExactly("ITAG_USER");
    }

    @Test
    public void testFindUserByFullNameSearchWithComma() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("User, Api"), Status.ALL, null, new PageRequest());
        assertThat(usersByCaseload.getItems()).extracting(UserDetail::getUsername).containsExactly("ITAG_USER");
    }

    @Test
    public void testFindUserByFullNameSearchNoresults() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("Other Api"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindUsersByCaseloadAndNameFilterUsername() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", null, new NameFilter("ITAG_USER"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndNameFilterAndAccessRoleFilter() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", List.of("OMIC_ADMIN"), new NameFilter("User"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting("username").contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilter() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", List.of("OMIC_ADMIN"), new NameFilter("User Api"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).extracting(UserDetail::getUsername).contains("ITAG_USER");
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterRoleNotAssigned() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", List.of("ACCESS_ROLE_GENERAL"), new NameFilter("User"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterRoleNotAnAccessRole() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", List.of("WING_OFF"), new NameFilter("User"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }

    @Test
    public void testFindUsersByCaseloadAndAccessRoleFilterNonExistantRole() {

        final var usersByCaseload = userRepository.findUsersByCaseload("LEI", List.of("OMIC_ADMIN_DOESNT_EXIST"), new NameFilter("User"), Status.ALL, null, new PageRequest());

        assertThat(usersByCaseload.getItems()).isEmpty();
    }


    @Test
    public void testUpdateWorkingCaseLoad() {
        // STAFF_USER_ACCOUNTS for genUsername and admUsername have the same staff_id (-1).
        final var genUsername = "PRISON_API_USER";
        final var admUsername = "PRISON_API_USER_ADM";

        assertThat(caseLoadRepository.getWorkingCaseLoadByUsername(genUsername).map(CaseLoad::getCaseLoadId)).contains("LEI");
        assertThat(caseLoadRepository.getWorkingCaseLoadByUsername(admUsername).map(CaseLoad::getCaseLoadId)).contains("CADM_I");
        userRepository.updateWorkingCaseLoad(genUsername, "NWEB");
        assertThat(caseLoadRepository.getWorkingCaseLoadByUsername(genUsername).map(CaseLoad::getCaseLoadId)).contains("NWEB");
        assertThat(caseLoadRepository.getWorkingCaseLoadByUsername(admUsername).map(CaseLoad::getCaseLoadId)).contains("CADM_I");
        // Restore the original so we don't break other tests
        userRepository.updateWorkingCaseLoad(genUsername, "LEI");
    }

    @Test
    public void testGetBasicInmateDetailsByBookingIds() {
        final var users = userRepository.getUserListByUsernames(List.of("JBRIEN", "RENEGADE"));
        assertThat(users).extracting("username", "firstName", "staffId").contains(
            Tuple.tuple("JBRIEN", "Jo", -12L),
            Tuple.tuple("RENEGADE", "Renegade", -11L));
    }
}
