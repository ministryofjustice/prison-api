package net.syscon.elite.service.impl;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.AssessmentDto;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InmateServiceImplTest {
    @Mock
    private InmateAlertService inmateAlertService;
    @Mock
    private InmateRepository repository;
    @Mock
    private CaseLoadService caseLoadService;
    @Mock
    private BookingService bookingService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private KeyWorkerAllocationRepository keyWorkerAllocationRepository;
    @Mock
    private Environment env;
    @Mock
    private UserSecurityUtils securityUtils;
    @Mock
    private TelemetryClient telemetryClient;

    private InmateService serviceToTest;

    @Before
    public void init() {
        serviceToTest = new InmateServiceImpl(repository, caseLoadService, inmateAlertService,
                bookingService, userService, userRepository, authenticationFacade,
                keyWorkerAllocationRepository, env, securityUtils, telemetryClient,"WING", 100);
    }

    @Test
    public void testGetAssessments() {

        List<AssessmentDto> data = Arrays.asList(
                // need to ensure we OrderBy:
                // Order.DESC, "cellSharingAlertFlag"
                // Order.DESC, "assessmentDate"
                //	Order.DESC, "assessmentSeq"
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE4").assessmentDate(LocalDate.of(2018, Month.MAY,   7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE4").assessmentDate(LocalDate.of(2018, Month.MAY,   6)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.MAY,   2)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.MAY,   1)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 8)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).build()
        );
        Mockito.when(repository.findAssessments(Collections.singletonList(10L), null, Collections.emptySet())).thenReturn(data);

        final List<Assessment> assessments = serviceToTest.getAssessments(10L);

        assertThat(assessments).hasSize(4); // 1 per code
        assertThat(assessments).extracting("bookingId","assessmentCode","assessmentDate").contains(
                Tuple.tuple(10L, "CODE1", LocalDate.of(2018, Month.APRIL, 5)),
                Tuple.tuple(10L, "CODE2", LocalDate.of(2018, Month.APRIL, 8)),
                Tuple.tuple(10L, "CODE3", LocalDate.of(2018, Month.MAY,   2)),
                Tuple.tuple(10L, "CODE4", LocalDate.of(2018, Month.MAY,   7))
                );
    }

    @Test
    public void testGetInmatesAssessmentsByCode() {

        List<AssessmentDto> data = Arrays.asList(
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(false).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY,   7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY,   6)).cellSharingAlertFlag(true).build()
        );
        Mockito.when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10","OFFENDER11"), "THECODE", Collections.emptySet(), true)).thenReturn(data);

        final List<Assessment> assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10","OFFENDER11"), "THECODE", true);

        assertThat(assessments).hasSize(2); // 1 per offender
        assertThat(assessments).extracting("bookingId","assessmentCode","assessmentDate").contains(
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 5)),
                Tuple.tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY,   7))
        );
    }

    @Test
    public void testAllCodes() {
        // Ensure Ordering is same as from repository
        List<AssessmentDto> data = Arrays.asList(
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).reviewSupLevelType("STANDARD").reviewSupLevelTypeDesc("Standard").build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).reviewSupLevelType("LOW").reviewSupLevelTypeDesc("Low").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.APRIL, 3)).cellSharingAlertFlag(true).reviewSupLevelType("MED").reviewSupLevelTypeDesc("Medium").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(false).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build()
        );
        Mockito.when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10", "OFFENDER11"), null, Collections.emptySet(), true)).thenReturn(data);

        final List<Assessment> assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10", "OFFENDER11"), null, true);

        assertThat(assessments).hasSize(2); // 1 per offender
        assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate", "classification").contains(
                Tuple.tuple(10L, "CODE3", LocalDate.of(2018, Month.APRIL, 5), "High"),
                Tuple.tuple(11L, "CODE1", LocalDate.of(2018, Month.MAY, 7), "Standard")
        );
    }

    @Test
    public void testCreateBookingAppointment() {

        final CategorisationDetail catDetail = CategorisationDetail.builder().bookingId(-5L).category("D").committee("GOV").comment("comment").build();

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("ME", "credentials"));

        Mockito.when(bookingService.getLatestBookingByBookingId(1234L)).thenReturn(OffenderSummary.builder().agencyLocationId("CDI").bookingId(-5L).build());
        Mockito.when(userService.getUserByUsername("ME")).thenReturn(UserDetail.builder().staffId(444L).username("ME").build());
        Mockito.when(authenticationFacade.getCurrentUsername()).thenReturn("ME");

        serviceToTest.createCategorisation(1234L, catDetail);

        Mockito.verify(repository, Mockito.times(1)).insertCategory(catDetail, "CDI", 444L, "ME", 1004L);
    }

    @Test
    public void testMappingForOffenderDetailsAreCorrect() {
        final var offenderNumbers = Set.of("A123");
        final var caseLoadsIds = Set.of("1");

        Mockito.when(authenticationFacade.getCurrentUsername()).thenReturn("ME");
        Mockito.when(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(caseLoadsIds);

        Mockito.when(repository.getBasicInmateDetailsForOffenders(offenderNumbers, caseLoadsIds))
                 .thenReturn(List.of(InmateBasicDetails.builder()
                         .lastName("LAST NAME")
                         .firstName("FIRST NAME")
                         .middleName("MIDDLE NAME")
                         .build()));

        final var offenders = serviceToTest.getBasicInmateDetailsForOffenders(offenderNumbers);

        Assertions.assertThat(offenders)
                .containsExactly(InmateBasicDetails.builder()
                        .lastName("Last Name")
                        .firstName("First Name")
                        .middleName("Middle Name")
                        .build());
    }
}
