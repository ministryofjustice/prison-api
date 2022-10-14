package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.CreatePersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareCounterDto;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeeds;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderHealthProblem;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderHealthProblemRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HealthServiceImplTest {

    @Mock
    private InmateRepository inmateRepository;

    @Mock
    private ReferenceCodeRepository<HealthProblemCode> healthProblemCodeReferenceCodeRepository;
    @Mock
    private ReferenceCodeRepository<HealthProblemStatus> healthProblemStatusReferenceCodeRepository;
    @Mock
    private OffenderHealthProblemRepository offenderHealthProblemRepository;
    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    private HealthService serviceToTest;

    @BeforeEach
    public void init() {
        serviceToTest = new HealthService(
            offenderBookingRepository,
            inmateRepository,
            healthProblemCodeReferenceCodeRepository,
            healthProblemStatusReferenceCodeRepository,
            offenderHealthProblemRepository, 100
        );
    }

    @DisplayName("get personal care needs by problem type and subtype")
    @Test
    public void getPersonalCareNeedsByProblemTypeAndSubtype() {
        final var problemTypes = List.of("DISAB+RM", "DISAB+RC", "MATSTAT");
        final var personalCareNeedsAll = List.of(
            PersonalCareNeed.builder().problemType("DISAB").problemCode("MI").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
            PersonalCareNeed.builder().problemType("DISAB").problemCode("RM").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
            PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build()
        );
        final var personalCareNeeds = new PersonalCareNeeds(
            List.of(
                PersonalCareNeed.builder().problemType("DISAB").problemCode("RM").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
                PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build()
            )
        );

        when(inmateRepository.findPersonalCareNeeds(anyLong(), anySet())).thenReturn(personalCareNeedsAll);

        final var response = serviceToTest.getPersonalCareNeeds(1L, problemTypes);

        verify(inmateRepository).findPersonalCareNeeds(1L, Set.of("DISAB", "MATSTAT"));
        assertThat(response).isEqualTo(personalCareNeeds);
    }

    @DisplayName("get personal care needs split by offender")
    @Test
    public void getPersonalCareNeedsSplitByOffender() {
        final var problemTypes = List.of("DISAB+RM", "DISAB+RC", "MATSTAT");

        final var aaMat = PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9")
            .startDate(LocalDate.parse("2010-06-21")).offenderNo("A1234AA").build();
        final var aaDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RM")
            .startDate(LocalDate.parse("2010-06-21")).offenderNo("A1234AA").build();
        final var abDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RC")
            .startDate(LocalDate.parse("2010-06-22")).offenderNo("A1234AB").build();
        final var acDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RM")
            .startDate(LocalDate.parse("2010-06-22")).offenderNo("A1234AC").build();
        final var adDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("ND")
            .startDate(LocalDate.parse("2010-06-24")).offenderNo("A1234AD").build();

        when(inmateRepository.findPersonalCareNeeds(anyList(), anySet())).thenReturn(
            List.of(aaMat, aaDisab, abDisab, acDisab, adDisab));

        final var response = serviceToTest.getPersonalCareNeeds(List.of("A1234AA"), problemTypes);

        verify(inmateRepository).findPersonalCareNeeds(List.of("A1234AA"), Set.of("DISAB", "MATSTAT"));
        assertThat(response).containsExactly(
            new PersonalCareNeeds("A1234AA", List.of(aaMat, aaDisab)),
            new PersonalCareNeeds("A1234AB", List.of(abDisab)),
            new PersonalCareNeeds("A1234AC", List.of(acDisab)));
    }

    @DisplayName("count personal care needs between dates split by offender")
    @Test
    public void countPersonalCareNeedsSplitByOffender() {
        final var problemType = "DISAB";

        final var abDisab = OffenderHealthProblem.builder()
            .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AB").build()).build())
            .caseloadType("CASELOAD_TYPE")
            .problemType(new HealthProblemType(problemType, "Desc"))
            .startDate(LocalDate.parse("2022-06-21"))
            .build();

        final var aaDisab1 = OffenderHealthProblem.builder()
            .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AA").build()).build())
            .caseloadType("CASELOAD_TYPE")
            .problemType(new HealthProblemType(problemType, "Desc"))
            .startDate(LocalDate.parse("2022-06-21"))
            .build();

        final var aaDisab2 = OffenderHealthProblem.builder()
            .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AA").build()).build())
            .caseloadType("CASELOAD_TYPE")
            .problemType(new HealthProblemType(problemType, "Desc"))
            .startDate(LocalDate.parse("2022-06-22"))
            .build();
        final var aaDisab3 = OffenderHealthProblem.builder()
            .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AA").build()).build())
            .caseloadType("CASELOAD_TYPE")
            .problemType(new HealthProblemType(problemType, "Desc"))
            .startDate(LocalDate.parse("2022-06-22"))
            .build();
        final var aaDisab4 = OffenderHealthProblem.builder()
            .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AA").build()).build())
            .caseloadType("CASELOAD_TYPE")
            .problemType(new HealthProblemType(problemType, "Desc"))
            .startDate(LocalDate.parse("2022-06-22"))
            .build();

        when(offenderHealthProblemRepository.findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateAfterAndStartDateBefore(anyList(),any(), any(), any(), any())).thenReturn(
            List.of(abDisab, aaDisab1, aaDisab2, aaDisab3, aaDisab4));

        final var response = serviceToTest.countPersonalCareNeedsByOffenderNoAndProblemTypeBetweenDates(
            List.of("A1234AA", "A1234AB"),
            problemType,
            LocalDate.of(2022, 02, 02),
            LocalDate.of(2022, 03, 03));

        verify(offenderHealthProblemRepository).findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateAfterAndStartDateBefore(
            List.of("A1234AA", "A1234AB"),
            1,
            problemType,
            LocalDate.of(2022, 02, 02),
            LocalDate.of(2022, 03, 03));
        assertThat(response).containsExactly(
            new PersonalCareCounterDto("A1234AA", 4),
            new PersonalCareCounterDto("A1234AB", 1));

    }


    @Nested
    class AddPersonalCareNeed {
        private static final HealthProblemType PROBLEM_TYPE = new HealthProblemType("DISAB", null);
        private static final HealthProblemCode PROBLEM_CODE = new HealthProblemCode("D", null, PROBLEM_TYPE);

        private static final HealthProblemStatus PROBLEM_STATUS = new HealthProblemStatus("ON", null);

        private OffenderBooking booking = OffenderBooking.builder()
            .bookingId(1L)
            .active(true)
            .bookingSequence(1)
            .location(AgencyLocation.builder().id("MDI").type(AgencyLocationType.PRISON_TYPE).build())
            .offender(Offender.builder().nomsId("any noms id").build())
            .build();
        private OffenderBooking notActiveBooking = OffenderBooking.builder()
            .bookingId(1L)
            .active(false)
            .bookingSequence(1)
            .location(AgencyLocation.builder().id("MDI").type(AgencyLocationType.PRISON_TYPE).build())
            .offender(Offender.builder().nomsId("any noms id").build())
            .build();


        private final CreatePersonalCareNeed personalCareNeed = CreatePersonalCareNeed.builder()
            .problemCode("D")
            .problemStatus("ON")
            .commentText("Disability")
            .startDate(LocalDate.of(2021, 1, 1))
            .endDate(LocalDate.of(2022, 9, 28))
            .build();


        @DisplayName("add personal care need")
        @Test
        void canAddPersonalCareNeed() {
            when(offenderBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(healthProblemCodeReferenceCodeRepository.findById(HealthProblemCode.pk("D"))).thenReturn(Optional.of(PROBLEM_CODE));
            when(healthProblemStatusReferenceCodeRepository.findById(HealthProblemStatus.pk("ON"))).thenReturn(Optional.of(PROBLEM_STATUS));


            serviceToTest.addPersonalCareNeed(1L, personalCareNeed);
            assertThat(booking.getOffenderHealthProblems().get(0)).usingRecursiveComparison().isEqualTo(OffenderHealthProblem
                .builder()
                .caseloadType("INST")
                .commentText("Disability")
                .offenderBooking(booking)
                .problemCode(PROBLEM_CODE)
                .problemType(PROBLEM_TYPE)
                .problemStatus(PROBLEM_STATUS)
                .startDate(LocalDate.of(2021, 1, 1))
                .endDate(LocalDate.of(2022, 9, 28))
                .build());
        }

        @DisplayName("booking id is invalid")
        @Test
        void invalidOffenderBookingId() {
            when(offenderBookingRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceToTest.addPersonalCareNeed(1L, personalCareNeed))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Resource with id [1] not found.");
        }

        @DisplayName("problem status is invalid")
        @Test
        void invalidProblemStatus() {
            when(offenderBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(healthProblemCodeReferenceCodeRepository.findById(HealthProblemCode.pk("D"))).thenReturn(Optional.of(PROBLEM_CODE));
            when(healthProblemStatusReferenceCodeRepository.findById(HealthProblemStatus.pk("ON"))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceToTest.addPersonalCareNeed(1L, personalCareNeed))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Resource with id [ON] not found.");
        }

        @DisplayName("problem code is missing")
        @Test
        void missingProblemCode() {
            when(offenderBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(healthProblemCodeReferenceCodeRepository.findById(HealthProblemCode.pk("D"))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceToTest.addPersonalCareNeed(1L, personalCareNeed))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Resource with id [D] not found.");
        }
        @DisplayName("not active booking")
        @Test
        void notActiveBooking() {
            when(offenderBookingRepository.findById(1L)).thenReturn(Optional.of(notActiveBooking));
            assertThatThrownBy(() -> serviceToTest.addPersonalCareNeed(1L, personalCareNeed))
                .isInstanceOf(BadRequestException.class);
                }

    }

}
