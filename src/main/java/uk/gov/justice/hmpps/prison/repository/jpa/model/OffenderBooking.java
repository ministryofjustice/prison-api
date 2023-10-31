package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString.Exclude;
import lombok.With;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.type.YesNoConverter;
import uk.gov.justice.hmpps.prison.api.model.BookingAdjustment;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.api.model.LegalStatus;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentDetail;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentValues;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.support.BookingAdjustmentType;
import uk.gov.justice.hmpps.prison.api.support.SentenceAdjustmentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.KeyDateValues;
import uk.gov.justice.hmpps.prison.service.support.NonDtoReleaseDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer.filterSentenceTerms;

@Getter
@Setter
@RequiredArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_BOOKINGS")
@With
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "booking-with-summary",
        attributeNodes = {
            @NamedAttributeNode(value = "offender"),
            @NamedAttributeNode(value = "location"),
            @NamedAttributeNode(value = "assignedLivingUnit"),
        }
    ),
    @NamedEntityGraph(
        name = "booking-with-sentence-summary",
        attributeNodes = {
            @NamedAttributeNode("releaseDetail"),
            @NamedAttributeNode(value = "sentences", subgraph = "sentence-details"),
        },
        subgraphs = {
            @NamedSubgraph(
                name = "sentence-details",
                attributeNodes = {
                    @NamedAttributeNode("calculationType"),
                    @NamedAttributeNode(value = "courtCase", subgraph = "court"),
                }
            ),
            @NamedSubgraph(
                name = "court",
                attributeNodes = {
                    @NamedAttributeNode("caseStatus"),
                    @NamedAttributeNode("legalCaseType"),
                    @NamedAttributeNode(value = "agencyLocation", subgraph = "court-location"),
                }
            ),
            @NamedSubgraph(
                name = "court-location",
                attributeNodes = {
                    @NamedAttributeNode("type"),
                    @NamedAttributeNode("courtType"),

                }
            ),
        }
    ),
    @NamedEntityGraph(
        name = "booking-with-livingUnits",
        attributeNodes = {
            @NamedAttributeNode(value = "offender"),
            @NamedAttributeNode(value = "location"),
            @NamedAttributeNode(value = "assignedLivingUnit", subgraph = "agency-internal-location-details"),
        },
        subgraphs = {
            @NamedSubgraph(
                name = "agency-internal-location-details",
                attributeNodes = {
                    @NamedAttributeNode("livingUnit"),
                }
            ),
        }
    )
})
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
    private List<OffenderHealthProblem> offenderHealthProblems = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 1000)
    private List<CourtOrder> courtOrders = new ArrayList<>();

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
    @Convert(converter = YesNoConverter.class)
    private boolean active = false;

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<ExternalMovement> externalMovements = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
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
    private List<SentenceCalculation> sentenceCalculations = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 1000)
    private List<KeyDateAdjustment> keyDateAdjustments = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 1000)
    private List<SentenceAdjustment> sentenceAdjustments = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 1000)
    private List<SentenceTerm> terms = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    @BatchSize(size = 1000)
    private List<OffenderSentence> sentences = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<OffenderImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    @Exclude
    private List<OffenderAlert> alerts = new ArrayList<>();

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

    public boolean isOut() {
        return inOutStatus.equals("OUT");
    }

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
                    .sentenceExpiryCalculatedDate(sc.getSedCalculatedDate())
                    .sentenceExpiryOverrideDate(sc.getSedOverridedDate())
                    .postRecallReleaseDate(sc.getPrrdCalculatedDate())
                    .postRecallReleaseOverrideDate(sc.getPrrdOverridedDate())
                    .licenceExpiryDate(sc.getLicenceExpiryDate())
                    .licenceExpiryCalculatedDate(sc.getLedCalculatedDate())
                    .licenceExpiryOverrideDate(sc.getLedOverridedDate())
                    .homeDetentionCurfewEligibilityDate(sc.getHomeDetentionCurfewEligibilityDate())
                    .homeDetentionCurfewEligibilityOverrideDate(sc.getHdcedOverridedDate())
                    .homeDetentionCurfewEligibilityCalculatedDate(sc.getHdcedCalculatedDate())
                    .paroleEligibilityDate(sc.getParoleEligibilityDate())
                    .paroleEligibilityCalculatedDate(sc.getPedCalculatedDate())
                    .paroleEligibilityOverrideDate(sc.getPedOverridedDate())
                    .homeDetentionCurfewActualDate(sc.getHomeDetentionCurfewActualDate())
                    .actualParoleDate(sc.getActualParoleDate())
                    .releaseOnTemporaryLicenceDate(sc.getRotlOverridedDate())
                    .earlyRemovalSchemeEligibilityDate(sc.getErsedOverridedDate())
                    .tariffEarlyRemovalSchemeEligibilityDate(sc.getTersedOverridedDate())
                    .earlyTermDate(sc.getEarlyTermDate())
                    .midTermDate(sc.getMidTermDate())
                    .lateTermDate(sc.getLateTermDate())
                    .topupSupervisionExpiryDate(sc.getTopupSupervisionExpiryDate())
                    .topupSupervisionExpiryCalculatedDate(sc.getTusedCalculatedDate())
                    .topupSupervisionExpiryOverrideDate(sc.getTusedOverridedDate())
                    .tariffDate(sc.getTariffDate())
                    .dtoPostRecallReleaseDate(sc.getDprrdCalculatedDate())
                    .dtoPostRecallReleaseDateOverride(sc.getDprrdOverridedDate())
                    .nonParoleDate(sc.getNpdCalculatedDate())
                    .nonParoleOverrideDate(sc.getNpdOverridedDate())
                    .nonDtoReleaseDate(sc.getNonDtoReleaseDate())
                    .nonDtoReleaseDateType(sc.getNonDtoReleaseDateType())
                    .releaseDate(getReleaseDate(sentenceCalculation))
                    .confirmedReleaseDate(getConfirmedReleaseDate().orElse(null))
                    .mtdCalculatedDate(sc.getMtdCalculatedDate())
                    .mtdOverrideDate(sc.getMtdOverridedDate())
                    .ltdCalculatedDate(sc.getLtdCalculatedDate())
                    .ltdOverrideDate(sc.getLtdOverridedDate())
                    .etdOverrideDate(sc.getEtdOverridedDate())
                    .etdCalculatedDate(sc.getEtdCalculatedDate())
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

    public void add(final OffenderHealthProblem offenderHealthProblem) {
        offenderHealthProblems.add(offenderHealthProblem);
        offenderHealthProblem.setOffenderBooking(this);
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
        return filterSentenceTerms(getTerms(), filterBySentenceTermCodes);
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
                y -> y.setProfileCode(code)
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
            .filter(pd -> Objects.nonNull(pd.getCode()))
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
            .toList();
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
            .toList();
    }

    public List<OffenderImprisonmentStatus> getImprisonmentStatusesRecentFirst() {
        return imprisonmentStatuses.stream()
            .sorted(Comparator.comparingLong(OffenderImprisonmentStatus::getImprisonStatusSeq)
                .reversed())
            .toList();
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

    public List<SentenceAdjustmentValues> getSentenceAdjustments() {
        List<String> sentenceAdjustmentCodes = Stream.of(
            SentenceAdjustmentType.values()).map(SentenceAdjustmentType::getCode).toList();
        return sentenceAdjustments
            .stream()
            .filter(sa -> sentenceAdjustmentCodes.contains(sa.getSentenceAdjustCode()))
            .map(e -> SentenceAdjustmentValues.builder()
                .type(SentenceAdjustmentType.getByCode(e.getSentenceAdjustCode()))
                .sentenceSequence(e.getSentenceSeq())
                .numberOfDays(e.getAdjustDays())
                .fromDate(e.getAdjustFromDate())
                .toDate(e.getAdjustToDate())
                .active(e.isActive())
                .build()
            )
            .toList();
    }

    public List<BookingAdjustment> getBookingAdjustments() {
        List<String> bookingAdjustmentCodes = Stream.of(
            BookingAdjustmentType.values()).map(BookingAdjustmentType::getCode).toList();
        return keyDateAdjustments
            .stream()
            .filter(sa -> bookingAdjustmentCodes.contains(sa.getSentenceAdjustCode()))
            .map(e -> BookingAdjustment.builder()
                .type(BookingAdjustmentType.getByCode(e.getSentenceAdjustCode()))
                .numberOfDays(e.getAdjustDays())
                .fromDate(e.getAdjustFromDate())
                .toDate(e.getAdjustToDate())
                .active(e.isActive())
                .build()
            )
            .toList();
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
