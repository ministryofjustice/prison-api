package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.InmateAlertService;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.support.AssessmentDto;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private AuthenticationFacade authenticationFacade;
    @Mock
    private KeyWorkerAllocationRepository keyWorkerAllocationRepository;
    @Mock
    private Environment env;

    private InmateService serviceToTest;

    @Before
    public void init() {
        serviceToTest = new InmateServiceImpl(repository, caseLoadService, inmateAlertService,
                bookingService, userRepository, authenticationFacade,
                keyWorkerAllocationRepository, env, "WING", 100);
    }

    @Test
    public void testGetAssessments() {

        List<AssessmentDto> alerts = Arrays.asList(
                // need to ensure we OrderBy(Order.ASC, "assessmentCode") then By(Order.DESC, "assessmentDate,assessmentSeq")
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 8)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.MAY,   2)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.MAY,   1)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE4").assessmentDate(LocalDate.of(2018, Month.MAY,   7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE4").assessmentDate(LocalDate.of(2018, Month.MAY,   6)).cellSharingAlertFlag(true).build()
        );
        List<AssessmentDto> data = alerts;
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
        Mockito.when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10","OFFENDER11"), "THECODE", Collections.emptySet())).thenReturn(data);

        final List<Assessment> assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10","OFFENDER11"), "THECODE");

        assertThat(assessments).hasSize(2); // 1 per offender
        assertThat(assessments).extracting("bookingId","assessmentCode","assessmentDate").contains(
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 5)),
                Tuple.tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY,   7))
        );
    }

    @Test
    public void testGetInmatesCSRAs() {
        // Ensure Ordering is same as from repository
        List<AssessmentDto> data = Arrays.asList(
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).reviewSupLevelType("STANDARD").reviewSupLevelTypeDesc("Standard").build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).reviewSupLevelType("LOW").reviewSupLevelTypeDesc("Low").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.APRIL, 3)).cellSharingAlertFlag(true).reviewSupLevelType("MED").reviewSupLevelTypeDesc("Medium").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(false).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build()
        );
        Mockito.when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10", "OFFENDER11"), null, Collections.emptySet())).thenReturn(data);

        final List<Assessment> assessments = serviceToTest.getInmatesCSRAs(Arrays.asList("OFFENDER10", "OFFENDER11"));

        assertThat(assessments).hasSize(2); // 1 per offender
        assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate", "classification").contains(
                Tuple.tuple(10L, "CODE3", LocalDate.of(2018, Month.APRIL, 5), "High"),
                Tuple.tuple(11L, "CODE1", LocalDate.of(2018, Month.MAY, 7), "Standard")
        );
    }
}
