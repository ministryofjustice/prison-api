package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.CreatePersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareCounterDto;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeeds;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderHealthProblem;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderHealthProblemRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bookings API service interface.
 */
@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class HealthService {
    private final OffenderBookingRepository offenderBookingRepository;
    private final InmateRepository inmateRepository;
    private final ReferenceCodeRepository<HealthProblemCode> healthProblemCodeReferenceCodeRepository;
    private final ReferenceCodeRepository<HealthProblemStatus> healthProblemStatusReferenceCodeRepository;
    private final OffenderHealthProblemRepository offenderHealthProblemRepository;
    private final int maxBatchSize;

    public HealthService(final OffenderBookingRepository offenderBookingRepository,
                         final InmateRepository inmateRepository,
                         final ReferenceCodeRepository<HealthProblemCode> healthProblemCodeReferenceCodeRepository,
                         final ReferenceCodeRepository<HealthProblemStatus> healthProblemStatusReferenceCodeRepository,
                         OffenderHealthProblemRepository offenderHealthProblemRepository,
                         @Value("${batch.max.size:1000}") final int maxBatchSize
    ) {
        this.offenderBookingRepository = offenderBookingRepository;
        this.inmateRepository = inmateRepository;
        this.healthProblemCodeReferenceCodeRepository = healthProblemCodeReferenceCodeRepository;
        this.healthProblemStatusReferenceCodeRepository = healthProblemStatusReferenceCodeRepository;
        this.offenderHealthProblemRepository = offenderHealthProblemRepository;
        this.maxBatchSize = maxBatchSize;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public PersonalCareNeeds getPersonalCareNeeds(final Long bookingId, final List<String> problemTypes) {
        final var problemTypesMap = QueryParamHelper.splitTypes(problemTypes);

        final var personalCareNeeds = inmateRepository.findPersonalCareNeeds(bookingId, problemTypesMap.keySet());
        final var returnList = personalCareNeeds.stream().filter((personalCareNeed) -> {
            final var subTypes = problemTypesMap.get(personalCareNeed.getProblemType());
            // will be null if not in map, otherwise will be empty if type in map with no sub type set
            return subTypes != null && (subTypes.isEmpty() || subTypes.contains(personalCareNeed.getProblemCode()));
        }).toList();
        return new PersonalCareNeeds(returnList);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<PersonalCareNeeds> getPersonalCareNeeds(final List<String> offenderNos, final List<String> problemTypes) {
        final var problemTypesMap = QueryParamHelper.splitTypes(problemTypes);

        // firstly need to exclude any problem sub types not interested in
        final var personalCareNeeds = Lists.partition(offenderNos, maxBatchSize)
            .stream()
            .map(offenders -> inmateRepository.findPersonalCareNeeds(offenders, problemTypesMap.keySet()))
            .flatMap(List::stream);

        // then transform list into map where keys are the offender no and values list of needs for the offender
        final var map = personalCareNeeds.filter((personalCareNeed) -> {
            final var subTypes = problemTypesMap.get(personalCareNeed.getProblemType());
            // will be null if not in map, otherwise will be empty if type in map with no sub type set
            return subTypes != null && (subTypes.isEmpty() || subTypes.contains(personalCareNeed.getProblemCode()));
        }).collect(Collectors.toMap(
            PersonalCareNeed::getOffenderNo,
            List::of,
            (a, b) -> Stream.of(a, b).flatMap(Collection::stream).toList(),
            TreeMap::new));

        // then convert back into list where every entry is for a single offender
        return map.entrySet().stream().map(e -> new PersonalCareNeeds(e.getKey(), e.getValue())).toList();
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<PersonalCareCounterDto> countPersonalCareNeedsByOffenderNoAndProblemTypeBetweenDates(
        final List<String> offenderNos,
        final String problemType,
        final LocalDate startDate,
        final LocalDate endDate) {
        final var personalCareNeeds = offenderHealthProblemRepository.findAllByOffenderBookingOffenderNomsIdInAndProblemTypeCodeAndStartDateAfterAndStartDateBefore(offenderNos, problemType, startDate, endDate);

        return personalCareNeeds.stream().collect(
                Collectors.groupingBy(o -> o.getOffenderBooking().getOffender().getNomsId()))
            .entrySet().stream().map(e -> new PersonalCareCounterDto(e.getKey(), e.getValue().size())).toList();
    }

    @Transactional
    @VerifyBookingAccess
    public void addPersonalCareNeed(final Long bookingId, final CreatePersonalCareNeed createPersonalCareNeed) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        final var caseloadType = offenderBooking.getLocation().getType().getCode();
        final var problemCode = healthProblemCodeReferenceCodeRepository.findById(HealthProblemCode.pk(createPersonalCareNeed.getProblemCode())).orElseThrow(EntityNotFoundException.withId(createPersonalCareNeed.getProblemCode()));
        final var problemStatus = healthProblemStatusReferenceCodeRepository.findById(HealthProblemStatus.pk(createPersonalCareNeed.getProblemStatus())).orElseThrow(EntityNotFoundException.withId(createPersonalCareNeed.getProblemStatus()));

        final var offenderHealthProblem = OffenderHealthProblem.builder()
            .offenderBooking(offenderBooking)
            .caseloadType(caseloadType)
            .problemType(problemCode.getProblemType())
            .problemCode(problemCode)
            .problemStatus(problemStatus)
            .commentText(createPersonalCareNeed.getCommentText())
            .startDate(createPersonalCareNeed.getStartDate())
            .endDate(createPersonalCareNeed.getEndDate())
            .build();

        offenderBooking.add(offenderHealthProblem);
    }
}
