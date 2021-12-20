package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.annotations.Type;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.api.model.LegalStatus;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentDetail;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.KeyDateValues;
import uk.gov.justice.hmpps.prison.service.support.NonDtoReleaseDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

@Getter
@Setter
@RequiredArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_BOOKINGS")
@BatchSize(size = 25)
public class OffenderBooking extends AuditableEntity {

    @SequenceGenerator(name = "OFFENDER_BOOK_ID", sequenceName = "OFFENDER_BOOK_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_BOOK_ID")
    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Column(name = "BOOKING_NO")
    private String bookNumber;

    @Column(name = "BOOKING_TYPE")
    private String bookingType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_ID", nullable = false)
    @Exclude
    private Offender offender;

    @OneToMany(mappedBy = "id.offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<OffenderProfileDetail> profileDetails = new ArrayList<>();

    @OrderColumn(name = "MILITARY_SEQ")
    @ListIndexBase(1)
    @OneToMany(mappedBy = "bookingAndSequence.offenderBooking", cascade = CascadeType.ALL)
    @Exclude
    private List<OffenderMilitaryRecord> militaryRecords;

    @OrderColumn(name = "CASE_SEQ")
    @ListIndexBase(1)
    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<OffenderCourtCase> courtCases = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<CourtOrder> courtOrders = new ArrayList<>();

    @ListIndexBase(1)
    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Exclude
    private List<OffenderPropertyContainer> propertyContainers;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    @Exclude
    private AgencyLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATE_AGY_LOC_ID")
    @Exclude
    private AgencyLocation createLocation;

    @Setter(AccessLevel.NONE)
    @Column(name = "BOOKING_SEQ", nullable = false)
    private Integer bookingSequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LIVING_UNIT_ID")
    @Exclude
    private AgencyInternalLocation assignedLivingUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNED_STAFF_ID")
    @Exclude
    private Staff assignedStaff;

    @Column(name = "AGENCY_IML_ID")
    private Long livingUnitMv;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Default
    @Type(type="yes_no")
    private boolean active = false;

    @OrderBy("effectiveDate ASC")
    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<OffenderNonAssociationDetail> nonAssociationDetails = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<ExternalMovement> externalMovements = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<OffenderImprisonmentStatus> imprisonmentStatuses = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<OffenderCaseNote> caseNotes = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<OffenderCharge> charges = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<SentenceCalculation> sentenceCalculations = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<KeyDateAdjustment> keyDateAdjustments = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<SentenceAdjustment> sentenceAdjustments = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<SentenceTerm> terms = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<OffenderSentence> sentences = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<OffenderImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<OffenderAlert> alerts = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 25)
    private List<OffenderIepLevel> iepLevels = new ArrayList<>();


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_OFFENDER_ID", nullable = false)
    @Exclude
    private Offender rootOffender;

    @Column(name = "BOOKING_STATUS")
    private String bookingStatus;

    @Column(name = "STATUS_REASON")
    private String statusReason;

    @Column(name = "DISCLOSURE_FLAG", nullable = false)
    @Default
    private String disclosureFlag = "Y";

    @Column(name = "COMMUNITY_ACTIVE_FLAG", nullable = false)
    @Default
    private String communityActiveFlag = "N";

    @Column(name = "SERVICE_FEE_FLAG", nullable = false)
    @Default
    private String serviceFeeFlag = "N";

    @Column(name = "COMM_STATUS")
    private String commStatus;

    @Column(name = "YOUTH_ADULT_CODE", nullable = false)
    private String youthAdultCode;

    @Column(name = "BOOKING_BEGIN_DATE", nullable = false)
    private LocalDateTime bookingBeginDate;

    @Column(name = "BOOKING_END_DATE")
    private LocalDateTime bookingEndDate;

    @Column(name = "IN_OUT_STATUS", nullable = false)
    private String inOutStatus;

    @Column(name = "ADMISSION_REASON")
    private String admissionReason;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private ReleaseDetail releaseDetail;

    public void addSentenceCalculation(final SentenceCalculation sc) {
        sentenceCalculations.add(sc);
        sc.setOffenderBooking(this);
    }

    public Optional<SentenceCalculation> getLatestCalculation() {
        return sentenceCalculations.stream().max(Comparator.comparing(SentenceCalculation::getId));
    }

    public LocalDate getReleaseDate(Optional<SentenceCalculation> sentenceCalculation) {
        return sentenceCalculation.map(
                sc -> deriveKeyDates(new KeyDateValues(
                    sc.getArdCalculatedDate(),
                    sc.getArdOverridedDate(),
                    sc.getCrdCalculatedDate(),
                    sc.getCrdOverridedDate(),
                    sc.getNpdCalculatedDate(),
                    sc.getNpdOverridedDate(),
                    sc.getPrrdCalculatedDate(),
                    sc.getPrrdOverridedDate(),
                    sc.getActualParoleDate(),
                    sc.getHomeDetentionCurfewActualDate(),
                    sc.getMidTermDate(),
                    getConfirmedReleaseDate().orElse(null)))
            ).orElse(deriveKeyDates(new KeyDateValues(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                getConfirmedReleaseDate().orElse(null))))
            .releaseDate();
    }

    public LocalDate getReleaseDate() {
        return getReleaseDate(getLatestCalculation());
    }

    public SentenceCalcDates getSentenceCalcDates(Optional<SentenceCalculation> sentenceCalculation) {
        return sentenceCalculation.map(
                sc -> SentenceCalcDates.sentenceCalcDatesBuilder()
                    .bookingId(getBookingId())
                    .sentenceStartDate(getSentenceStartDate().orElse(null))
                    .effectiveSentenceEndDate(sc.getEffectiveSentenceEndDate())
                    .additionalDaysAwarded(getAdditionalDaysAwarded())
                    .automaticReleaseDate(sc.getArdCalculatedDate())
                    .automaticReleaseOverrideDate(sc.getArdOverridedDate())
                    .conditionalReleaseDate(sc.getCrdCalculatedDate())
                    .conditionalReleaseOverrideDate(sc.getCrdOverridedDate())
                    .sentenceExpiryDate(sc.getSentenceExpiryDate())
                    .postRecallReleaseDate(sc.getPrrdCalculatedDate())
                    .postRecallReleaseOverrideDate(sc.getPrrdOverridedDate())
                    .licenceExpiryDate(sc.getLicenceExpiryDate())
                    .homeDetentionCurfewEligibilityDate(sc.getHomeDetentionCurfewEligibilityDate())
                    .paroleEligibilityDate(sc.getParoleEligibilityDate())
                    .homeDetentionCurfewActualDate(sc.getHomeDetentionCurfewActualDate())
                    .actualParoleDate(sc.getActualParoleDate())
                    .releaseOnTemporaryLicenceDate(sc.getRotlOverridedDate())
                    .earlyRemovalSchemeEligibilityDate(sc.getErsedOverridedDate())
                    .tariffEarlyRemovalSchemeEligibilityDate(sc.getTersedOverridedDate())
                    .earlyTermDate(sc.getEarlyTermDate())
                    .midTermDate(sc.getMidTermDate())
                    .lateTermDate(sc.getLateTermDate())
                    .topupSupervisionExpiryDate(sc.getTopupSupervisionExpiryDate())
                    .tariffDate(sc.getTariffDate())
                    .dtoPostRecallReleaseDate(sc.getDprrdCalculatedDate())
                    .dtoPostRecallReleaseDateOverride(sc.getDprrdOverridedDate())
                    .nonParoleDate(sc.getNpdCalculatedDate())
                    .nonParoleOverrideDate(sc.getNpdOverridedDate())
                    .nonDtoReleaseDate(sc.getNonDtoReleaseDate())
                    .nonDtoReleaseDateType(sc.getNonDtoReleaseDateType())
                    .releaseDate(getReleaseDate(sentenceCalculation))
                    .confirmedReleaseDate(getConfirmedReleaseDate().orElse(null))
                    .build())
            .orElse(
                SentenceCalcDates.sentenceCalcDatesBuilder()
                    .bookingId(getBookingId())
                    .sentenceStartDate(getSentenceStartDate().orElse(null))
                    .additionalDaysAwarded(getAdditionalDaysAwarded())
                    .releaseDate(getReleaseDate(sentenceCalculation))
                    .confirmedReleaseDate(getConfirmedReleaseDate().orElse(null))
                    .build());
    }

    public SentenceCalcDates getSentenceCalcDates() {
        return getSentenceCalcDates(getLatestCalculation());
    }

    public record DerivedKeyDates(NonDtoReleaseDate nonDtoReleaseDate, LocalDate releaseDate) {
    }

    public static DerivedKeyDates deriveKeyDates(final KeyDateValues keyDateValues) {

        // Determine non-DTO release date
        final var nonDtoReleaseDate = SentenceCalculation.deriveNonDtoReleaseDate(keyDateValues).orElse(null);

        // Determine offender release date
        final var releaseDate = deriveOffenderReleaseDate(keyDateValues, nonDtoReleaseDate);

        return new DerivedKeyDates(nonDtoReleaseDate, releaseDate);
    }

    /**
     * Offender release date is determined according to algorithm.
     * <p>
     * 1. If there is a confirmed release date, the offender release date is the confirmed release date.
     * <p>
     * 2. If there is no confirmed release date for the offender, the offender release date is either the actual
     * parole date or the home detention curfew actual date.
     * <p>
     * 3. If there is no confirmed release date, actual parole date or home detention curfew actual date for the
     * offender, the release date is the latter of the nonDtoReleaseDate or midTermDate value (if either or both
     * are present).
     *
     * @param keyDateValues     a set of key date values used to determine the non-deterministic release date
     * @param nonDtoReleaseDate derived non-deterministic release date information
     * @return releaseDate
     */
    private static LocalDate deriveOffenderReleaseDate(final KeyDateValues keyDateValues, final NonDtoReleaseDate nonDtoReleaseDate) {

        final LocalDate releaseDate;

        if (Objects.nonNull(keyDateValues.confirmedReleaseDate())) {
            releaseDate = keyDateValues.confirmedReleaseDate();
        } else if (Objects.nonNull(keyDateValues.actualParoleDate())) {
            releaseDate = keyDateValues.actualParoleDate();
        } else if (Objects.nonNull(keyDateValues.homeDetentionCurfewActualDate())) {
            releaseDate = keyDateValues.homeDetentionCurfewActualDate();
        } else {
            final var midTermDate = keyDateValues.midTermDate();

            if (Objects.nonNull(nonDtoReleaseDate)) {
                if (Objects.isNull(midTermDate)) {
                    releaseDate = nonDtoReleaseDate.getReleaseDate();
                } else {
                    releaseDate = midTermDate.isAfter(nonDtoReleaseDate.getReleaseDate()) ? midTermDate : nonDtoReleaseDate.getReleaseDate();
                }
            } else {
                releaseDate = midTermDate;
            }
        }

        return releaseDate;
    }


    public Optional<LocalDate> getConfirmedReleaseDate() {
        return Optional.ofNullable(releaseDetail != null ? releaseDetail.getReleaseDate() != null ? releaseDetail.getReleaseDate() : releaseDetail.getAutoReleaseDate() : null);
    }

    public Optional<LocalDate> getSentenceStartDate() {
        return sentences.stream()
            .filter(s -> "A".equals(s.getStatus()))
            .flatMap(s -> s.getTerms().stream())
            .min(Comparator.comparing(SentenceTerm::getStartDate))
            .map(SentenceTerm::getStartDate);
    }

    public List<OffenderSentenceTerms> getActiveFilteredSentenceTerms(List<String> filterBySentenceTermCodes) {
        final var sentenceTermCodes = (filterBySentenceTermCodes == null || filterBySentenceTermCodes.isEmpty()) ? List.of("IMP") : filterBySentenceTermCodes;
        return getTerms()
            .stream()
            .filter(term -> "A".equals(term.getOffenderSentence().getStatus()))
            .filter(term -> sentenceTermCodes.contains(term.getSentenceTermCode()))
            .map(SentenceTerm::getSentenceSummary)
            .collect(toList());
    }

    public void add(final OffenderMilitaryRecord omr) {
        militaryRecords.add(omr);
        omr.setBookingAndSequence(new BookingAndSequence(this, militaryRecords.size()));
    }

    public void add(final OffenderCourtCase courtCase) {
        courtCases.add(courtCase);
        courtCase.setOffenderBooking(this);
    }

    public void add(final ProfileType profileType, final ProfileCode code) {
        profileDetails.stream()
            .filter(pd -> profileType.equals(pd.getId().getType()))
            .max(Comparator.comparing(op -> op.getId().getSequence()))
            .ifPresentOrElse(
                y -> y.setCode(code)
                , () -> profileDetails.add(OffenderProfileDetail.builder()
                    .id(new PK(this, profileType, 1))
                    .caseloadType("INST")
                    .code(code)
                    .listSequence(profileType.getListSequence())
                    .build()));
    }

    public Optional<OffenderCourtCase> getCourtCaseBy(final Long courtCaseId) {
        return courtCases == null ? Optional.empty() : courtCases.stream().filter(Objects::nonNull).filter(cc -> cc.getId().equals(courtCaseId)).findFirst();
    }

    public List<OffenderCourtCase> getActiveCourtCases() {
        return courtCases.stream().filter(offenderCourtCase -> offenderCourtCase != null && offenderCourtCase.isActive()).toList();
    }

    public List<OffenderPropertyContainer> getActivePropertyContainers() {
        return propertyContainers.stream().filter(OffenderPropertyContainer::isActive).toList();
    }

    public List<OffenderProfileDetail> getActiveProfileDetails() {
        return profileDetails.stream()
            .filter(pd -> {
                final var profileType = pd.getId().getType();
                return profileType.getCategory().equals("PI") && (profileType.isActive() || profileType.getType().equals("RELF"));
            })
            .collect(
                Collectors.groupingBy(pd -> pd.getId().getType())
            ).entrySet().stream()
            .flatMap(pd -> pd.getValue().stream()
                .max(Comparator.comparing(op -> op.getId().getSequence()))
                .stream())
            .collect(Collectors.toList());
    }

    public void incBookingSequence() {
        if (bookingSequence == null) bookingSequence = 0;
        bookingSequence = bookingSequence + 1;
    }

    public ExternalMovement addExternalMovement(final ExternalMovement externalMovement) {
        externalMovement.setMovementSequence(getNextMovementSequence());
        externalMovement.setOffenderBooking(this);
        externalMovements.add(externalMovement);
        return externalMovement;
    }

    public Optional<ExternalMovement> getLastMovement() {
        return getMovementsRecentFirst().stream().findFirst();
    }


    public OffenderImprisonmentStatus setImprisonmentStatus(final OffenderImprisonmentStatus offenderImprisonmentStatus, final LocalDateTime effectiveFrom) {
        setPreviousImprisonmentStatusToInactive(effectiveFrom);
        offenderImprisonmentStatus.setImprisonStatusSeq(getNextImprisonmentStatusSequence());
        offenderImprisonmentStatus.setOffenderBooking(this);
        offenderImprisonmentStatus.makeActive();
        offenderImprisonmentStatus.setEffectiveDate(effectiveFrom.toLocalDate());
        offenderImprisonmentStatus.setEffectiveTime(effectiveFrom);
        imprisonmentStatuses.add(offenderImprisonmentStatus);
        return offenderImprisonmentStatus;
    }

    public Optional<OffenderImprisonmentStatus> getActiveImprisonmentStatus() {
        return getImprisonmentStatusesRecentFirst().stream().filter(OffenderImprisonmentStatus::isActiveLatestStatus).findFirst();
    }

    public List<ExternalMovement> getMovementsRecentFirst() {
        return externalMovements.stream()
            .sorted(Comparator.comparingLong(ExternalMovement::getMovementSequence)
                .reversed())
            .collect(Collectors.toList());
    }

    public List<OffenderImprisonmentStatus> getImprisonmentStatusesRecentFirst() {
        return imprisonmentStatuses.stream()
            .sorted(Comparator.comparingLong(OffenderImprisonmentStatus::getImprisonStatusSeq)
                .reversed())
            .collect(Collectors.toList());
    }

    public Long getNextImprisonmentStatusSequence() {
        return getImprisonmentStatusesRecentFirst().stream().findFirst().map(OffenderImprisonmentStatus::getImprisonStatusSeq).orElse(0L) + 1;
    }

    public Long getNextMovementSequence() {
        return getLastMovement().map(ExternalMovement::getMovementSequence).orElse(0L) + 1;
    }

    public void setPreviousMovementsToInactive() {
        externalMovements.stream().filter(ExternalMovement::isActive).forEach(m -> m.setActive(false));
    }

    public void setPreviousImprisonmentStatusToInactive(final LocalDateTime expiryTime) {
        imprisonmentStatuses.stream().filter(OffenderImprisonmentStatus::isActiveLatestStatus).forEach(m -> m.makeInactive(expiryTime));
    }

    public LegalStatus getLegalStatus() {
        return getActiveImprisonmentStatus().map(
            is -> is.getImprisonmentStatus().getLegalStatus()
        ).orElse(null);
    }

    public String getConvictedStatus() {
        return getActiveImprisonmentStatus().map(
            is -> is.getImprisonmentStatus().getConvictedStatus()
        ).orElse(null);
    }

    public Optional<OffenderImage> getLatestFaceImage() {
        return images.stream()
            .filter(OffenderImage::isActive)
            .filter(i -> "OFF_BKG".equals(i.getImageType()))
            .filter(i -> "FACE".equals(i.getViewType()))
            .filter(i -> "FRONT".equals(i.getOrientationType()))
            .max(Comparator.comparing(OffenderImage::getId));
    }

    public ImageDetail addImage(OffenderImage image) {
        getImages().add(image);
        image.setOffenderBooking(this);
        return image.transform();
    }

    public Optional<OffenderIepLevel> getLatestIepLevel() {
        return iepLevels.stream()
            .max(Comparator.comparing(OffenderIepLevel::getIepDate).thenComparing(OffenderIepLevel::getSequence));
    }

    public OffenderIepLevel addIepLevel(IepLevel iepLevel, String comment, final LocalDateTime iepDateTime, StaffUserAccount staff) {
        final var now = LocalDateTime.now();
        final var offenderIepLevel = OffenderIepLevel.builder()
            .offenderBooking(this)
            .sequence(getLatestIepLevel().map(s -> s.getSequence()+1).orElse(1L))
            .iepLevel(iepLevel)
            .comment(comment)
            .iepDate(iepDateTime != null ? iepDateTime.toLocalDate() : now.toLocalDate())
            .iepDateTime(iepDateTime != null ? iepDateTime : now)
            .staffUser(staff)
            .agencyLocation(getLocation())
            .build();
        iepLevels.add(offenderIepLevel);
        return offenderIepLevel;
    }

    public Optional<PrivilegeSummary> getIepSummary(boolean withDetails) {
        return getLatestIepLevel().map(iep -> PrivilegeSummary.builder()
            .bookingId(getBookingId())
            .iepDate(iep.getIepDate())
            .iepTime(iep.getIepDateTime())
            .iepLevel(iep.getIepLevel().getDescription())
            .daysSinceReview(DAYS.between(iep.getIepDate(), now()))
            .iepDetails(withDetails ? iepLevels.stream().sorted(Comparator.comparing(OffenderIepLevel::getIepDate).thenComparing(OffenderIepLevel::getSequence).reversed())
                .map(OffenderIepLevel::getPrivilageDetail).toList() : Collections.emptyList())
            .build());
    }

    public List<String> getAlertCodes() {
        return alerts.stream().filter(OffenderAlert::isActive).map(OffenderAlert::getAlertType).collect(Collectors.toSet()).stream().toList();
    }

    public long getActiveAlertCount() {
        return alerts.stream().filter(OffenderAlert::isActive).count();
    }

    public List<OffenderSentence> getLicenceSentences() {
        return sentences.stream()
            .filter(s -> s.getCourtCase() == null)
            .filter(s -> "LICENCE".equals(s.getCalculationType().getCategory()))
            .toList();
    }
    public SentenceAdjustmentDetail getSentenceAdjustmentDetail() {

        return SentenceAdjustmentDetail.builder()
            .additionalDaysAwarded(getAdditionalDaysAwarded())
            .lawfullyAtLarge(getLawfullyAtLarge())
            .unlawfullyAtLarge(getUnlawfullyAtLarge())
            .restoredAdditionalDaysAwarded(getRestoredAdditionalDaysAwarded())
            .specialRemission(getSpecialRemission())
            .recallSentenceRemand(getRecallSentenceRemand())
            .recallSentenceTaggedBail(getRecallSentenceTaggedBail())
            .remand(getRemand())
            .taggedBail(getTaggedBail())
            .unusedRemand(getUnusedRemand())
            .build();
    }

    public Integer getAdditionalDaysAwarded() {
        return getDaysForKeyDateAdjustmentsCode(keyDateAdjustments, "ADA");
    }

    public Integer getLawfullyAtLarge() {
        return getDaysForKeyDateAdjustmentsCode(keyDateAdjustments, "LAL");
    }

    public Integer getUnlawfullyAtLarge() {
        return getDaysForKeyDateAdjustmentsCode(keyDateAdjustments, "UAL");
    }

    public Integer getRestoredAdditionalDaysAwarded() {
        return getDaysForKeyDateAdjustmentsCode(keyDateAdjustments, "RADA");
    }

    public Integer getSpecialRemission() {
        return getDaysForKeyDateAdjustmentsCode(keyDateAdjustments, "SREM");
    }

    public Integer getRecallSentenceRemand() {
        return getDaysForSentenceAdjustmentsCode(sentenceAdjustments, "RSR");
    }

    public Integer getRecallSentenceTaggedBail() {
        return getDaysForSentenceAdjustmentsCode(sentenceAdjustments, "RST");
    }

    public Integer getRemand() {
        return getDaysForSentenceAdjustmentsCode(sentenceAdjustments, "RX");
    }

    public Integer getTaggedBail() {
        return getDaysForSentenceAdjustmentsCode(sentenceAdjustments, "S240A");
    }

    public Integer getUnusedRemand() {
        return getDaysForSentenceAdjustmentsCode(sentenceAdjustments, "UR");
    }

    public static Integer getDaysForKeyDateAdjustmentsCode(final List<KeyDateAdjustment> adjustmentsList, final String code) {
        final var adjustedDays = adjustmentsList
            .stream()
            .filter(adj -> code.equals(adj.getSentenceAdjustCode()))
            .filter(KeyDateAdjustment::isActive)
            .mapToInt(KeyDateAdjustment::getAdjustDays).sum();

        return adjustedDays == 0 ? null : adjustedDays;
    }

    public static Integer getDaysForSentenceAdjustmentsCode(final List<SentenceAdjustment> adjustmentsList, final String code) {
        final var adjustedDays = adjustmentsList
            .stream()
            .filter(adj -> code.equals(adj.getSentenceAdjustCode()))
            .filter(SentenceAdjustment::isActive)
            .mapToInt(SentenceAdjustment::getAdjustDays).sum();

        return adjustedDays == 0 ? null : adjustedDays;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final OffenderBooking that = (OffenderBooking) o;

        return Objects.equals(getBookingId(), that.getBookingId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getBookingId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
            "bookingId = " + bookingId + ", " +
            "bookNumber = " + bookNumber + ", " +
            "bookingSequence = " + bookingSequence + ", " +
            "active = " + active + ", " +
            "inOutStatus = " + inOutStatus + ")";
    }
}
