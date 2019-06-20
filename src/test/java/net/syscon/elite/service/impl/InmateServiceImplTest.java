package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.AssessmentDto;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    private TelemetryClient telemetryClient;

    private InmateService serviceToTest;

    @Before
    public void init() {
        serviceToTest = new InmateServiceImpl(repository, caseLoadService, inmateAlertService, null,
                bookingService, userService, userRepository, authenticationFacade,
                keyWorkerAllocationRepository, env, telemetryClient, "WING", 100);
    }

    @Test
    public void testGetAssessments() {

        final var data = Arrays.asList(
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
        when(repository.findAssessments(Collections.singletonList(10L), null, Collections.emptySet())).thenReturn(data);

        final var assessments = serviceToTest.getAssessments(10L);

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

        final var data = Arrays.asList(
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(false).assessmentCreateLocation("MDI").approvalDate(LocalDate.of(2018, Month.MAY, 5)).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).assessmentCreateLocation("LEI").approvalDate(LocalDate.of(2018, Month.JUNE, 5)).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(LocalDate.of(2018, Month.OCTOBER, 5)).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY,   7)).cellSharingAlertFlag(true).assessmentCreateLocation("EXI").build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY,   6)).cellSharingAlertFlag(true).build()
        );
        when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10","OFFENDER11"), "THECODE", Collections.emptySet(), true)).thenReturn(data);

        final var assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10", "OFFENDER11"), "THECODE", true);

        assertThat(assessments).hasSize(2); // 1 per offender
        assertThat(assessments).extracting("bookingId","assessmentCode","assessmentDate", "assessmentAgencyId", "approvalDate").containsExactly(
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 5), "MDI", LocalDate.of(2018, Month.MAY,   5)),
                Tuple.tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY,   7), "EXI", null)
        );
    }

    @Test
    public void testGetInmatesAssessmentsByCodeIncludingHistorical() {

        final var data = Arrays.asList(
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(false).assessmentCreateLocation("MDI").approvalDate(LocalDate.of(2018, Month.MAY, 5)).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).assessmentCreateLocation("LEI").approvalDate(LocalDate.of(2018, Month.JUNE, 5)).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(LocalDate.of(2018, Month.OCTOBER, 5)).build(),
                AssessmentDto.builder().bookingId(9L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2016, Month.APRIL, 1)).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(LocalDate.of(2016, Month.OCTOBER, 5)).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY,   7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY,   6)).cellSharingAlertFlag(true).build()
        );
        when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10","OFFENDER11"), "THECODE", Collections.emptySet(), false)).thenReturn(data);

        final var assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10", "OFFENDER11"), "THECODE", false);

        assertThat(assessments).hasSize(6);
        assertThat(assessments).extracting("bookingId","assessmentCode","assessmentDate").containsExactly(
                Tuple.tuple(9L, "THECODE", LocalDate.of(2016, Month.APRIL, 1)),
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 5)),
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 4)),
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 1)),
                Tuple.tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY,   7)),
                Tuple.tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY,   6))
        );
    }

    @Test
    public void testAllCodes() {
        // Ensure Ordering is same as from repository
        final var data = Arrays.asList(
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).reviewSupLevelType("STANDARD").reviewSupLevelTypeDesc("Standard").build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).reviewSupLevelType("LOW").reviewSupLevelTypeDesc("Low").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.APRIL, 3)).cellSharingAlertFlag(true).reviewSupLevelType("MED").reviewSupLevelTypeDesc("Medium").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(false).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build()
        );
        when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10", "OFFENDER11"), null, Collections.emptySet(), true)).thenReturn(data);

        final var assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10", "OFFENDER11"), null, true);

        assertThat(assessments).hasSize(2); // 1 per offender
        assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate", "classification").contains(
                Tuple.tuple(10L, "CODE3", LocalDate.of(2018, Month.APRIL, 5), "High"),
                Tuple.tuple(11L, "CODE1", LocalDate.of(2018, Month.MAY, 7), "Standard")
        );
    }

    @Test
    public void testCreateCategorisation() {

        final var catDetail = CategorisationDetail.builder().bookingId(-5L).category("D").committee("GOV").comment("comment").build();

        when(bookingService.getLatestBookingByBookingId(1234L)).thenReturn(OffenderSummary.builder().agencyLocationId("CDI").bookingId(-5L).build());
        when(userService.getUserByUsername("ME")).thenReturn(UserDetail.builder().staffId(444L).username("ME").build());
        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");

        serviceToTest.createCategorisation(1234L, catDetail);

        assertThat(catDetail.getNextReviewDate()).isEqualTo(LocalDate.now().plusMonths(6));
        Mockito.verify(repository, Mockito.times(1)).insertCategory(catDetail, "CDI", 444L, "ME");
    }

    @Test
    public void testCreateCategorisationWithReviewDateSpecified() {

        final var catDetail = CategorisationDetail.builder().bookingId(-5L).category("D").committee("GOV").comment("comment")
                .nextReviewDate(LocalDate.of(2019, 4, 1)).build();

        when(bookingService.getLatestBookingByBookingId(1234L)).thenReturn(OffenderSummary.builder().agencyLocationId("CDI").bookingId(-5L).build());
        when(userService.getUserByUsername("ME")).thenReturn(UserDetail.builder().staffId(444L).username("ME").build());
        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");

        serviceToTest.createCategorisation(1234L, catDetail);

        Mockito.verify(repository, Mockito.times(1)).insertCategory(catDetail, "CDI", 444L, "ME");
    }

    @Test
    public void testGetOffenderCategorisationsBatching() {

        var setOf150Longs = Stream.iterate(1L, n -> n + 1)
                .limit(150)
                .collect(Collectors.toSet());

        ArgumentCaptor<String> agencyArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<Long>> bookingIdsArgument = ArgumentCaptor.forClass(List.class);

        final var catDetail1 = OffenderCategorise.builder().bookingId(-5L).category("D").build();
        final var catDetail2 = OffenderCategorise.builder().bookingId(-4L).category("B").build();
        final var catDetail3 = OffenderCategorise.builder().bookingId(-3L).category("C").build();

        var listOf100Longs = Stream.iterate(1L, n -> n + 1)
                .limit(100)
                .collect(Collectors.toList());

        var listOf50Longs = Stream.iterate(101L, n -> n + 1)
                .limit(50)
                .collect(Collectors.toList());

        when(repository.getOffenderCategorisations(listOf100Longs, "LEI")).thenReturn(Collections.singletonList(catDetail1));
        when(repository.getOffenderCategorisations(listOf50Longs, "LEI")).thenReturn(ImmutableList.of(catDetail2, catDetail3));

        final var results = serviceToTest.getOffenderCategorisations("LEI", setOf150Longs);

        assertThat(results).hasSize(3);

        Mockito.verify(repository, Mockito.times(2)).getOffenderCategorisations(bookingIdsArgument.capture(), agencyArgument.capture());
        var capturedArguments = bookingIdsArgument.getAllValues();
        assertThat(capturedArguments.get(0)).containsAll(listOf100Longs);
        assertThat(capturedArguments.get(1)).containsAll(listOf50Longs);
    }

    @Test
    public void testGetBasicInmateDetailsByBookingIdsBatching() {

        var setOf150Longs = Stream.iterate(1L, n -> n + 1)
                .limit(150)
                .collect(Collectors.toSet());

        ArgumentCaptor<String> agencyArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<Long>> bookingIdsArgument = ArgumentCaptor.forClass(List.class);

        final var detail1 = InmateBasicDetails.builder().bookingId(-5L).lastName("D").build();
        final var detail2 = InmateBasicDetails.builder().bookingId(-4L).lastName("B").build();
        final var detail3 = InmateBasicDetails.builder().bookingId(-3L).lastName("C").build();

        var listOf100Longs = Stream.iterate(1L, n -> n + 1)
                .limit(100)
                .collect(Collectors.toList());

        var listOf50Longs = Stream.iterate(101L, n -> n + 1)
                .limit(50)
                .collect(Collectors.toList());

        when(repository.getBasicInmateDetailsByBookingIds("LEI", listOf100Longs)).thenReturn(Collections.singletonList(detail1));
        when(repository.getBasicInmateDetailsByBookingIds("LEI", listOf50Longs)).thenReturn(ImmutableList.of(detail2, detail3));

        final var results = serviceToTest.getBasicInmateDetailsByBookingIds("LEI", setOf150Longs);

        assertThat(results).hasSize(3);

        Mockito.verify(repository, Mockito.times(2)).getBasicInmateDetailsByBookingIds(agencyArgument.capture(), bookingIdsArgument.capture());
        var capturedArguments = bookingIdsArgument.getAllValues();
        assertThat(capturedArguments.get(0)).containsAll(listOf100Longs);
        assertThat(capturedArguments.get(1)).containsAll(listOf50Longs);
    }


    @Test
    public void testMappingForOffenderDetailsAreCorrect() {
        final var offenderNumbers = Set.of("A123");
        final var caseLoadsIds = Set.of("1");

        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");
        when(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(caseLoadsIds);
        when(repository.getBasicInmateDetailsForOffenders(offenderNumbers, false, caseLoadsIds))
                 .thenReturn(List.of(InmateBasicDetails.builder()
                         .lastName("LAST NAME")
                         .firstName("FIRST NAME")
                         .middleName("MIDDLE NAME")
                         .build()));

        final var offenders = serviceToTest.getBasicInmateDetailsForOffenders(offenderNumbers);

        assertThat(offenders)
                .containsExactly(InmateBasicDetails.builder()
                        .lastName("Last Name")
                        .firstName("First Name")
                        .middleName("Middle Name")
                        .build());
    }

    @Test
    public void testThatAnExceptionIsThrown_whenAStandardUserWithNoActiveCaseloadsRequestsInmateDetails() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");
        when(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(Collections.emptySet());

        Assertions.assertThatThrownBy(() -> {
             serviceToTest.getBasicInmateDetailsForOffenders(Set.of("A123"));
        })
       .isInstanceOf(BadRequestException.class)
       .hasMessageContaining("User has not active caseloads");
    }

    @Test
    public void testThatGetBasicInmateDetailsForOffenders_isCalledWithCorrectParameters() {
        final var caseLoad = Set.of("LEI");

        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");
        when(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(caseLoad);

        serviceToTest.getBasicInmateDetailsForOffenders(Set.of("A123"));

        Mockito.verify(repository).getBasicInmateDetailsForOffenders(Set.of("A123"), false, caseLoad);
        Mockito.verify(caseLoadService).getCaseLoadIdsForUser("ME", false);
     }
}
