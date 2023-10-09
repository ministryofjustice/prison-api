package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.Alias;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.PhysicalCharacteristic;
import uk.gov.justice.hmpps.prison.api.model.PhysicalMark;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation;
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustments;
import uk.gov.justice.hmpps.prison.api.model.RecallCalc;
import uk.gov.justice.hmpps.prison.api.model.SecondaryLanguage;
import uk.gov.justice.hmpps.prison.api.support.AssessmentStatusType;
import uk.gov.justice.hmpps.prison.api.support.CategoryInformationType;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderLanguage;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderLanguageRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.support.AssessmentDto;
import uk.gov.justice.hmpps.prison.service.support.InmateDto;
import uk.gov.justice.hmpps.prison.service.support.InmatesHelper;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.REL;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ACTIVE_ONLY;
import static uk.gov.justice.hmpps.prison.service.support.InmatesHelper.deriveClassification;
import static uk.gov.justice.hmpps.prison.service.support.InmatesHelper.deriveClassificationCode;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class InmateService {
    private final InmateRepository repository;
    private final CaseLoadService caseLoadService;
    private final BookingService bookingService;
    private final AgencyService agencyService;
    private final UserService userService;
    private final InmateAlertService inmateAlertService;
    private final ReferenceDomainService referenceDomainService;
    private final AuthenticationFacade authenticationFacade;
    private final int maxBatchSize;
    private final OffenderAssessmentService offenderAssessmentService;
    private final OffenderLanguageRepository offenderLanguageRepository;
    private final OffenderRepository offenderRepository;
    private final ExternalMovementRepository externalMovementRepository;
    private final OffenderImageRepository offenderImageRepository;
    private final HealthService healthService;
    private final TelemetryClient telemetryClient;

    public InmateService(final InmateRepository repository,
                         final CaseLoadService caseLoadService,
                         final InmateAlertService inmateAlertService,
                         final ReferenceDomainService referenceDomainService,
                         final BookingService bookingService,
                         final AgencyService agencyService,
                         final HealthService healthService,
                         final UserService userService,
                         final AuthenticationFacade authenticationFacade,
                         final TelemetryClient telemetryClient,
                         @Value("${batch.max.size:1000}") final int maxBatchSize,
                         final OffenderAssessmentService offenderAssessmentService,
                         final OffenderLanguageRepository offenderLanguageRepository,
                         final OffenderRepository offenderRepository,
                         final ExternalMovementRepository externalMovementRepository,
                         final OffenderImageRepository offenderImageRepository
                         ) {
        this.repository = repository;
        this.caseLoadService = caseLoadService;
        this.inmateAlertService = inmateAlertService;
        this.referenceDomainService = referenceDomainService;
        this.healthService = healthService;
        this.telemetryClient = telemetryClient;
        this.bookingService = bookingService;
        this.agencyService = agencyService;
        this.authenticationFacade = authenticationFacade;
        this.maxBatchSize = maxBatchSize;
        this.userService = userService;
        this.offenderAssessmentService = offenderAssessmentService;
        this.offenderLanguageRepository = offenderLanguageRepository;
        this.offenderRepository = offenderRepository;
        this.externalMovementRepository = externalMovementRepository;
        this.offenderImageRepository = offenderImageRepository;
    }

    public List<InmateDto> findInmatesByLocation(final String username, final String agencyId, final List<Long> locations) {
        final var caseLoadIds = getUserCaseloadIds(username);

        return repository.findInmatesByLocation(agencyId, locations, caseLoadIds);
    }

    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(final Set<String> offenders, final boolean active) {
        final var canViewAllOffenders = isViewAllOffenders();
        final var caseloads = canViewAllOffenders ? Set.<String>of() : loadCaseLoadsOrThrow();

        log.info("getBasicInmateDetailsForOffenders, {} offenders, {} caseloads, canViewAllOffenders {}", offenders.size(), caseloads.size(), canViewAllOffenders);

        final var results = new ArrayList<InmateBasicDetails>();
        Lists.partition(Lists.newArrayList(offenders), maxBatchSize).forEach(offenderList ->
                results.addAll(
                        repository.getBasicInmateDetailsForOffenders(new HashSet<>(offenderList), canViewAllOffenders, caseloads, active)
                                .stream()
                                .map(offender ->
                                        offender.toBuilder()
                                                .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
                                                .middleName(WordUtils.capitalizeFully(offender.getMiddleName()))
                                                .lastName(WordUtils.capitalizeFully(offender.getLastName()))
                                                .build()
                                ).toList()
                ));

        log.info("getBasicInmateDetailsForOffenders, {} records returned", results.size());
        return results;
    }

    private boolean isViewAllOffenders() {
        return authenticationFacade.isOverrideRole("SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA");
    }

    private Set<String> loadCaseLoadsOrThrow() {
        final var caseloads = caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false);
        if (CollectionUtils.isEmpty(caseloads)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User has not active caseloads.");
        }

        return caseloads;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public InmateDetail findInmate(final Long bookingId, final boolean extraInfo, final boolean csraSummary) {
        final var inmate = repository.findInmate(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        return getOffenderDetails(inmate, extraInfo, csraSummary);
    }

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @Transactional // route to primary in live so that we can get the latest data after a trigger
    public InmateDetail findOffender(final String offenderNo, final boolean extraInfo, final boolean csraSummary) {
        final var inmate = repository.findOffender(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
        return getOffenderDetails(inmate, extraInfo, csraSummary);
    }

    private InmateDetail getOffenderDetails(final InmateDetail inmate, final boolean extraInfo, final boolean csraSummary) {
        if (inmate.getBookingId() == null) {
            offenderRepository.findById(inmate.getOffenderId())
                .ifPresent(offender -> inmate.setPhysicalAttributes(PhysicalAttributes.builder()
                    .sexCode(offender.getGender().getCode())
                    .gender(offender.getGender().getDescription())
                    .raceCode(offender.getEthnicity() != null ? offender.getEthnicity().getCode() : null)
                    .ethnicity(offender.getEthnicity() != null ? offender.getEthnicity().getDescription() : null)
                    .build()));
        }

        if (extraInfo) {
            inmate.setIdentifiers(repository.getOffenderIdentifiersByOffenderId(inmate.getOffenderId()));
        }

        if (inmate.getBookingId() != null) {
            final var bookingId = inmate.getBookingId();
            inmate.deriveStatus();
            inmate.splitStatusReason();

            final var languages = offenderLanguageRepository.findByOffenderBookId(bookingId);
            getFirstPreferredSpokenLanguage(languages).ifPresent(offenderLanguage -> {
                inmate.setLanguage(offenderLanguage.getReferenceCode().getDescription());
                inmate.setInterpreterRequired("Y".equalsIgnoreCase(offenderLanguage.getInterpreterRequestedFlag()));
            });
            getFirstPreferredWrittenLanguage(languages).ifPresent(offenderLanguage -> inmate.setWrittenLanguage(offenderLanguage.getReferenceCode().getDescription()));

            inmate.setPhysicalAttributes(getPhysicalAttributes(bookingId));
            inmate.setPhysicalCharacteristics(getPhysicalCharacteristics(bookingId));
            inmate.setProfileInformation(getProfileInformation(bookingId));
            repository.findAssignedLivingUnit(bookingId).ifPresent(assignedLivingUnit -> {
                assignedLivingUnit.setAgencyName(LocationProcessor.formatLocation(assignedLivingUnit.getAgencyName()));
                inmate.setAssignedLivingUnit(assignedLivingUnit);
            });
            setAlertsFields(inmate);
            setAssessmentsFields(bookingId, inmate, csraSummary);

            try {
                inmate.setPhysicalMarks(getPhysicalMarks(bookingId));
            } catch (final Exception e) {
                // TODO: Hack for now to make sure there wasn't a reason this was removed.
            }
            if (extraInfo) {
                inmate.setAliases(repository.findInmateAliases(bookingId, "createDate", Order.ASC, 0, 100).getItems());
                inmate.setSentenceDetail(bookingService.getBookingSentenceCalcDates(bookingId));
                inmate.setPersonalCareNeeds(healthService.getPersonalCareNeeds(bookingId, List.of("DISAB", "MATSTAT", "PHY", "PSYCH", "SC")).getPersonalCareNeeds());

                repository.getImprisonmentStatus(bookingId).ifPresent(status -> {
                    inmate.setLegalStatus(status.getLegalStatus());
                    inmate.setImprisonmentStatus(status.getImprisonmentStatus());
                    inmate.setImprisonmentStatusDescription(status.getDescription());
                });

                final var offenceHistory = bookingService.getActiveOffencesForBooking(bookingId, true);
                final var sentenceTerms = bookingService.getOffenderSentenceTerms(bookingId, null);
                inmate.setOffenceHistory(offenceHistory);
                inmate.setSentenceTerms(sentenceTerms);
                inmate.setRecall(RecallCalc.calculate(bookingId, inmate.getLegalStatus(), offenceHistory, sentenceTerms));

                if (!"IN".equals(inmate.getInOutStatus())) {
                    externalMovementRepository.findFirstByOffenderBooking_BookingIdOrderByMovementSequenceDesc(inmate.getBookingId())
                        .filter(externalMovement -> externalMovement.getFromAgency() != null)
                        .ifPresentOrElse(
                            lastMovement -> {
                                inmate.setLatestLocationId(lastMovement.getFromAgency().getId());
                                if (REL.getCode().equals(inmate.getLastMovementTypeCode())) {
                                    inmate.setLocationDescription(calculateReleaseLocationDescription(lastMovement));
                                }
                            },
                            () -> inmate.setLocationDescription("Outside")
                        );
                }

                if (inmate.getLocationDescription() ==  null) {
                    inmate.setLocationDescription(inmate.getAssignedLivingUnit().getAgencyName());
                }
                if (inmate.getLatestLocationId() ==  null) {
                    inmate.setLatestLocationId(inmate.getAssignedLivingUnit().getAgencyId());
                }
            }
        }
        return inmate;
    }

    public static String calculateReleaseLocationDescription(final ExternalMovement lastMovement) {
        return REL.getCode().equals(lastMovement.getMovementType().getCode())
                ? "Outside - released from " + lastMovement.getFromAgency().getDescription()
                : "Outside - " + lastMovement.getMovementType().getDescription();
    }

    private Optional<OffenderLanguage> getFirstPreferredSpokenLanguage(final List<OffenderLanguage> languages) {
        return languages
                .stream()
                .filter(l -> "PREF_SPEAK".equals(l.getType()) && l.getReferenceCode() != null)
                .sorted(Comparator.comparing(right -> right.getReferenceCode().getDescription()))
                .reduce((first, second) -> second);
    }

    private Optional<OffenderLanguage> getFirstPreferredWrittenLanguage(final List<OffenderLanguage> languages) {
        return languages
                .stream()
                .filter(l -> "PREF_WRITE".equals(l.getType()) && l.getReferenceCode() != null)
                .sorted(Comparator.comparing(right -> right.getReferenceCode().getDescription()))
                .reduce((first, second) -> second);
    }

    private void setAssessmentsFields(final Long bookingId, final InmateDetail inmate, final boolean csraSummary) {
        final var assessments = getAllAssessmentsOrdered(bookingId);
        if (!CollectionUtils.isEmpty(assessments)) {
            inmate.setAssessments(filterAssessmentsByCode(assessments));
            findCsra(assessments).ifPresent(csra -> inmate.setCsra(csra.getClassification()));
            findCategory(assessments).ifPresent(category -> {
                inmate.setCategory(category.getClassification());
                inmate.setCategoryCode(category.getClassificationCode());
            });
        }
        if (csraSummary) {
            final var currentCsraClassification = offenderAssessmentService.getCurrentCsraClassification(inmate.getOffenderNo());
            if (currentCsraClassification != null) {
                inmate.setCsraClassificationCode(currentCsraClassification.getClassificationCode());
                inmate.setCsraClassificationDate(currentCsraClassification.getClassificationDate());
            }
        }
    }

    private List<Assessment> getAllAssessmentsOrdered(final Long bookingId) {
        final var assessmentsDto = repository.findAssessments(Collections.singletonList(bookingId), null, Collections.emptySet());

        return assessmentsDto.stream().map(this::createAssessment).toList();
    }

    /**
     * @param assessments input list, ordered by date,seq desc
     * @return The latest assessment for each code.
     */
    private List<Assessment> filterAssessmentsByCode(final List<Assessment> assessments) {

        // this map preserves date order within code
        final var mapOfAssessments = assessments.stream().collect(Collectors.groupingBy(Assessment::getAssessmentCode));
        final List<Assessment> assessmentsFiltered = new ArrayList<>();
        // get latest assessment for each code
        mapOfAssessments.forEach((assessmentCode, assessment) -> assessmentsFiltered.add(assessment.get(0)));
        return assessmentsFiltered;
    }

    private void setAlertsFields(final InmateDetail inmate) {
        final var bookingId = inmate.getBookingId();
        final var inmateAlertPage = inmateAlertService.getInmateAlerts(bookingId, "", null, 0, 1000);
        final var items = inmateAlertPage.getItems();
        if (inmateAlertPage.getTotalRecords() > inmateAlertPage.getPageLimit()) {
            items.addAll(inmateAlertService.getInmateAlerts(bookingId, "", null, 1000, inmateAlertPage.getTotalRecords()).getItems());
        }
        final Set<String> alertTypes = new HashSet<>();
        final var activeAlertCount = new AtomicInteger(0);
        items.stream().filter(Alert::isActive).forEach(a -> {
            activeAlertCount.incrementAndGet();
            alertTypes.add(a.getAlertType());
        });
        inmate.setAlerts(items);
        inmate.setAlertsCodes(new ArrayList<>(alertTypes));
        inmate.setActiveAlertCount(activeAlertCount.longValue());
        inmate.setInactiveAlertCount(items.size() - activeAlertCount.longValue());
    }

    /**
     * Get assessments, latest per code, order not important.
     *
     * @param bookingId tacit
     * @return latest assessment of each code for the offender
     */
    @VerifyBookingAccess
    public List<Assessment> getAssessments(final Long bookingId) {
        final var assessmentsDto = repository.findAssessments(Collections.singletonList(bookingId), null, Collections.emptySet());

        // this map preserves date order within code
        final var mapOfAssessments = assessmentsDto.stream().collect(Collectors.groupingBy(AssessmentDto::getAssessmentCode));
        final List<Assessment> assessments = new ArrayList<>();
        // get latest assessment for each code
        mapOfAssessments.forEach((assessmentCode, assessment) -> assessments.add(createAssessment(assessment.get(0))));
        return assessments;
    }

    @VerifyBookingAccess
    public List<PhysicalMark> getPhysicalMarks(final Long bookingId) {
        return repository.findPhysicalMarks(bookingId);
    }

    @VerifyBookingAccess
    public ReasonableAdjustments getReasonableAdjustments(final Long bookingId, final List<String> treatmentCodes) {
        return new ReasonableAdjustments(repository.findReasonableAdjustments(bookingId, treatmentCodes));
    }

    @VerifyBookingAccess
    public List<ProfileInformation> getProfileInformation(final Long bookingId) {
        return repository.getProfileInformation(bookingId);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public ImageDetail getMainBookingImage(final Long bookingId) {
        return offenderImageRepository.findLatestByBookingId(bookingId)
            .map(OffenderImage::transform)
            .orElseThrow(EntityNotFoundException.withMessage(String.format("No Image found for booking Id %d", bookingId)));
    }

    @VerifyBookingAccess
    public List<PhysicalCharacteristic> getPhysicalCharacteristics(final Long bookingId) {
        return repository.findPhysicalCharacteristics(bookingId);
    }

    @VerifyBookingAccess
    public PhysicalAttributes getPhysicalAttributes(final Long bookingId) {
        final var physicalAttributes = repository.findPhysicalAttributes(bookingId).orElse(null);
        if (physicalAttributes != null && physicalAttributes.getHeightCentimetres() != null) {
            physicalAttributes.setHeightMetres(BigDecimal.valueOf(physicalAttributes.getHeightCentimetres()).movePointLeft(2));
        }
        return physicalAttributes;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<OffenderIdentifier> getOffenderIdentifiers(final Long bookingId, @Nullable final String identifierType) {
        return repository.getOffenderIdentifiers(bookingId)
                .stream()
                .filter(i -> identifierType == null || identifierType.equalsIgnoreCase(i.getType()))
                .toList();
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(@NotNull final String identifierType, @NotNull final String identifierValue) {
        return repository.getOffenderIdentifiersByTypeAndValue(identifierType, identifierValue);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @Transactional // route to primary in live so that we can get the latest data after a trigger
    public InmateDetail getBasicInmateDetail(final Long bookingId) {
        return repository.getBasicInmateDetail(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    @VerifyAgencyAccess
    public List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(final String caseload, final Set<Long> bookingIds) {
        final List<InmateBasicDetails> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bookingIds)) {
            final var batch = Lists.partition(new ArrayList<>(bookingIds), maxBatchSize);
            batch.forEach(offenderBatch -> {
                final var offenderList = repository.getBasicInmateDetailsByBookingIds(caseload, offenderBatch);
                results.addAll(offenderList);
            });
        }
        return results;
    }

    /**
     * @param bookingId      tacit
     * @param assessmentCode tacit
     * @return Latest assessment of given code if any
     */
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Optional<Assessment> getInmateAssessmentByCode(final Long bookingId, final String assessmentCode) {
        final var assessmentForCodeType = repository.findAssessments(Collections.singletonList(bookingId), assessmentCode, Collections.emptySet());

        Assessment assessment = null;

        if (!CollectionUtils.isEmpty(assessmentForCodeType)) {
            assessment = createAssessment(assessmentForCodeType.get(0));
        }

        return Optional.ofNullable(assessment);
    }

    public List<Assessment> getInmatesAssessmentsByCode(final List<String> offenderNos, final String assessmentCode, final boolean latestOnly, final boolean activeOnly, final boolean csra,
                                                        final boolean mostRecentOnly) {
        final List<Assessment> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(offenderNos)) {
            final Set<String> caseLoadIds = authenticationFacade.isOverrideRole("SYSTEM_USER", "VIEW_ASSESSMENTS", "VIEW_PRISONER_DATA")
                    ? Collections.emptySet()
                    : caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false);

            final var batch = Lists.partition(offenderNos, maxBatchSize);
            batch.forEach(offenderBatch -> {
                final var assessments = repository.findAssessmentsByOffenderNo(offenderBatch, assessmentCode, caseLoadIds, latestOnly, activeOnly);

                InmatesHelper.createMapOfBookings(assessments).values().forEach(assessmentForBooking -> {

                    if (mostRecentOnly) {
                        if(!csra){
                           results.add(createAssessment(assessmentForBooking.get(0)));
                        }else {
                            final var firstAssessment = createAssessment(
                                assessmentForBooking
                                    .stream().filter(assessmentDto -> isCalculatedCsra(assessmentDto) || isReviewedCsra(assessmentDto))
                                    .findFirst()
                                    .orElse(assessmentForBooking.get(0))
                            );
                            if (validCsra(firstAssessment)) {
                                results.add(firstAssessment);
                            }
                        }
                    } else {
                        assessmentForBooking.stream().map(this::createAssessment).filter(a -> !csra || validCsra(a)).forEach(results::add);
                    }
                });
            });
        }
        return results;
    }

    private boolean validCsra(final Assessment firstAssessment) {
        return (firstAssessment.isCellSharingAlertFlag() && !"PEND".equals(firstAssessment.getClassificationCode()));
    }

    private boolean isReviewedCsra(final AssessmentDto assessmentDto) {
        return (assessmentDto.getReviewSupLevelType() != null && !"PEND".equals(assessmentDto.getReviewSupLevelTypeDesc()));
    }

    private boolean isCalculatedCsra(final AssessmentDto assessmentDto) {
        return (assessmentDto.getCalcSupLevelType() != null && !"PEND".equals(assessmentDto.getCalcSupLevelTypeDesc()))
            && assessmentDto.getOverridedSupLevelType() == null;
    }


    @VerifyAgencyAccess
    public List<OffenderCategorise> getOffenderCategorisations(final String agencyId, final Set<Long> bookingIds, final boolean latestOnly) {
        return doGetOffenderCategorisations(agencyId, bookingIds, latestOnly);
    }

    @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA','SYSTEM_USER')")
    public List<OffenderCategorise> getOffenderCategorisationsSystem(final Set<Long> bookingIds, final boolean latestOnly) {
        return doGetOffenderCategorisations(null, bookingIds, latestOnly);
    }

    private List<OffenderCategorise> doGetOffenderCategorisations(final String agencyId, final Set<Long> bookingIds, final boolean latestOnly) {
        final List<OffenderCategorise> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bookingIds)) {
            final var batch = Lists.partition(new ArrayList<>(bookingIds), maxBatchSize);
            batch.forEach(offenderBatch -> {
                final var categorisations = repository.getOffenderCategorisations(offenderBatch, agencyId, latestOnly);
                results.addAll(categorisations);
            });
        }
        return results;
    }

    private Optional<Assessment> findCategory(final List<Assessment> assessmentsForOffender) {
        return assessmentsForOffender.stream().filter(a -> "CATEGORY".equals(a.getAssessmentCode())).findFirst();
    }

    private Optional<Assessment> findCsra(final List<Assessment> assessmentsForOffender) {
        return assessmentsForOffender.stream().filter(Assessment::isCellSharingAlertFlag).findFirst();
    }

    private Assessment createAssessment(final AssessmentDto assessmentDto) {
        return Assessment.builder()
                .bookingId(assessmentDto.getBookingId())
                .offenderNo(assessmentDto.getOffenderNo())
                .assessmentCode(assessmentDto.getAssessmentCode())
                .assessmentDescription(assessmentDto.getAssessmentDescription())
                .classification(deriveClassification(assessmentDto))
                .classificationCode(deriveClassificationCode(assessmentDto))
                .assessmentDate(assessmentDto.getAssessmentDate())
                .cellSharingAlertFlag(assessmentDto.isCellSharingAlertFlag())
                .nextReviewDate(assessmentDto.getNextReviewDate())
                .approvalDate(assessmentDto.getApprovalDate())
                .assessmentAgencyId(assessmentDto.getAssessmentCreateLocation())
                .assessmentStatus(assessmentDto.getAssessStatus())
                .assessmentSeq(assessmentDto.getAssessmentSeq())
                .assessmentComment(assessmentDto.getAssessCommentText())
                .assessorId(assessmentDto.getAssessStaffId())
                .assessorUser(assessmentDto.getCreationUser())
                .build();
    }

    @VerifyAgencyAccess
    public List<OffenderCategorise> getCategory(final String agencyId, final CategoryInformationType type, final LocalDate date) {
        return switch (type) {
            case UNCATEGORISED -> repository.getUncategorised(agencyId);
            case CATEGORISED -> repository.getApprovedCategorised(agencyId, ObjectUtils.defaultIfNull(date, LocalDate.now().minusMonths(1)));
            case RECATEGORISATIONS -> repository.getRecategorise(agencyId, ObjectUtils.defaultIfNull(date, LocalDate.now().plusMonths(2)));
        };
    }

    @VerifyBookingAccess
    @PreAuthorize("hasAnyRole('SYSTEM_USER','CREATE_CATEGORISATION','CREATE_RECATEGORISATION')")
    @Transactional
    public Map<String, Long> createCategorisation(final Long bookingId, final CategorisationDetail categorisationDetail) {
        validate(categorisationDetail);
        final var userDetail = userService.getUserByUsername(authenticationFacade.getCurrentUsername());
        final var currentBooking = bookingService.getLatestBookingByBookingId(bookingId);
        final var responseKeyMap = repository.insertCategory(categorisationDetail, currentBooking.getAgencyLocationId(), userDetail.getStaffId(), userDetail.getUsername());

        // Log event
        telemetryClient.trackEvent("CategorisationCreated", ImmutableMap.of("bookingId", bookingId.toString(), "category", categorisationDetail.getCategory()), null);
        return responseKeyMap;
    }

    @VerifyBookingAccess
    @PreAuthorize("hasAnyRole('SYSTEM_USER','CREATE_CATEGORISATION','CREATE_RECATEGORISATION')")
    @Transactional
    public void updateCategorisation(final Long bookingId, final CategorisationUpdateDetail detail) {
        validate(detail);
        repository.updateCategory(detail);

        // Log event
        telemetryClient.trackEvent("CategorisationUpdated", ImmutableMap.of("bookingId", bookingId.toString(), "seq", detail.getAssessmentSeq().toString()), null);
    }

    @VerifyBookingAccess
    @PreAuthorize("hasAnyRole('SYSTEM_USER','APPROVE_CATEGORISATION')")
    @Transactional
    public void approveCategorisation(final Long bookingId, final CategoryApprovalDetail detail) {
        validate(detail);
        repository.approveCategory(detail);

        // Log event
        telemetryClient.trackEvent("CategorisationApproved", ImmutableMap.of("bookingId", bookingId.toString(), "category", detail.getCategory()), null);
    }

    @VerifyBookingAccess
    @PreAuthorize("hasAnyRole('SYSTEM_USER','APPROVE_CATEGORISATION')")
    @Transactional
    public void rejectCategorisation(final Long bookingId, final CategoryRejectionDetail detail) {
        validate(detail);
        repository.rejectCategory(detail);

        // Log event
        telemetryClient.trackEvent("CategorisationRejected", ImmutableMap.of("bookingId", bookingId.toString(), "seq", detail.getAssessmentSeq().toString()), null);
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_USER')")
    public void setCategorisationInactive(final Long bookingId, final AssessmentStatusType status) {
        final var count = repository.setCategorisationInactive(bookingId, status);

        // Log event
        telemetryClient.trackEvent("CategorisationSetInactive", ImmutableMap.of(
                "bookingId", bookingId.toString(),
                "count", String.valueOf(count),
                "status", String.valueOf(status)), null);
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_USER')")
    public void updateCategorisationNextReviewDate(final Long bookingId, final LocalDate nextReviewDate) {
        repository.updateActiveCategoryNextReviewDate(bookingId, nextReviewDate);

        // Log event
        telemetryClient.trackEvent("CategorisationNextReviewDateUpdated", ImmutableMap.of("bookingId", bookingId.toString()), null);
    }

    private void validate(final CategorisationDetail detail) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.CATEGORY.getDomain(),
                    detail.getCategory(), false);
        } catch (final EntityNotFoundException ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Category not recognised.");
        }
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.ASSESSMENT_COMMITTEE_CODE.getDomain(),
                    detail.getCommittee(), false);
        } catch (final EntityNotFoundException ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Committee Code not recognised.");
        }
        if (StringUtils.isNotBlank(detail.getPlacementAgencyId())) {
            try {
                agencyService.getAgency(detail.getPlacementAgencyId(), ACTIVE_ONLY, "INST", false, false);
            } catch (final EntityNotFoundException ex) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Placement agency id not recognised.");
            }
        }
    }

    private void validate(final CategorisationUpdateDetail detail) {
        if (detail.getCategory() != null) {
            try {
                referenceDomainService.getReferenceCodeByDomainAndCode(uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.CATEGORY.getDomain(),
                        detail.getCategory(), false);
            } catch (final EntityNotFoundException ex) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Category not recognised.");
            }
        }
        if (detail.getCommittee() != null) {
            try {
                referenceDomainService.getReferenceCodeByDomainAndCode(uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.ASSESSMENT_COMMITTEE_CODE.getDomain(), detail.getCommittee(), false);
            } catch (final EntityNotFoundException ex) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Committee Code not recognised.");
            }
        }
    }

    private void validate(final CategoryApprovalDetail detail) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.CATEGORY.getDomain(), detail.getCategory(), false);
        } catch (final EntityNotFoundException ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Category not recognised.");
        }
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.ASSESSMENT_COMMITTEE_CODE.getDomain(), detail.getReviewCommitteeCode(), false);
        } catch (final EntityNotFoundException ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Committee Code not recognised.");
        }
        if (StringUtils.isNotBlank(detail.getApprovedPlacementAgencyId())) {
            try {
                agencyService.getAgency(detail.getApprovedPlacementAgencyId(), ACTIVE_ONLY, "INST", false, false);
            } catch (final EntityNotFoundException ex) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Review placement agency id not recognised.");
            }
        }
    }

    private void validate(final CategoryRejectionDetail detail) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.ASSESSMENT_COMMITTEE_CODE.getDomain(), detail.getReviewCommitteeCode(), false);
        } catch (final EntityNotFoundException ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Committee Code not recognised.");
        }
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Page<Alias> findInmateAliases(final Long bookingId, final String orderBy, final Order order, final long offset, final long limit) {
        final var defaultOrderBy = StringUtils.defaultString(StringUtils.trimToNull(orderBy), "createDate");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.DESC);

        return repository.findInmateAliases(bookingId, defaultOrderBy, sortOrder, offset, limit);
    }

    @VerifyBookingAccess
    public List<SecondaryLanguage> getSecondaryLanguages(final Long bookingId) {
        return offenderLanguageRepository
                .findByOffenderBookId(bookingId)
                .stream()
                .filter(lang -> "SEC".equalsIgnoreCase(lang.getType()))
                .map(lang -> SecondaryLanguage
                        .builder()
                        .bookingId(lang.getOffenderBookId())
                        .code(lang.getCode())
                        .description(lang.getReferenceCode() != null ? lang.getReferenceCode().getDescription() : null)
                        .canRead("Y".equalsIgnoreCase(lang.getReadSkill()))
                        .canWrite("Y".equalsIgnoreCase(lang.getWriteSkill()))
                        .canSpeak("Y".equalsIgnoreCase(lang.getSpeakSkill()))
                        .build()
                ).toList();
    }

    private Set<String> getUserCaseloadIds(final String username) {
        return caseLoadService.getCaseLoadIdsForUser(username, false);
    }
}
