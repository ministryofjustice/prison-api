package net.syscon.elite.service.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.repository.v1.*;
import net.syscon.elite.repository.v1.model.*;
import net.syscon.elite.service.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('SYSTEM_USER','NOMIS_API_V1')")
public class NomisApiV1Service {

    private final BookingV1Repository bookingV1Repository;
    private final OffenderV1Repository offenderV1Repository;
    private final LegalV1Repository legalV1Repository;
    private final FinanceV1Repository financeV1Repository;
    private final AlertV1Repository alertV1Repository;

    public NomisApiV1Service(final BookingV1Repository bookingV1Repository,
                             final OffenderV1Repository offenderV1Repository,
                             final LegalV1Repository legalV1Repository,
                             final FinanceV1Repository financeV1Repository,
                             final AlertV1Repository alertV1Repository) {
        this.bookingV1Repository = bookingV1Repository;
        this.offenderV1Repository = offenderV1Repository;
        this.legalV1Repository = legalV1Repository;
        this.financeV1Repository = financeV1Repository;
        this.alertV1Repository = alertV1Repository;
    }


    public Location getLatestBookingLocation(final String nomsId) {
        return bookingV1Repository.getLatestBooking(nomsId)
                .map(this::buildLocation)
                .orElseThrow(EntityNotFoundException.withId(nomsId));
    }

    private Location buildLocation(final BookingSP l) {
        return Location.builder()
                .establishment(CodeDescription.safeNullBuild(l.getAgyLocId(), l.getAgyLocDesc()))
                .housingLocation(StringUtils.isNotBlank(l.getHousingLocation()) ? new InternalLocation(l.getHousingLocation(), l.getHousingLevels()) : null)
                .build();
    }

    public Bookings getBookings(final String nomsId) {
        final var bookings = bookingV1Repository.getOffenderBookings(nomsId).stream()
                .map(booking ->
                        Booking.builder()
                                .offenderBookId(booking.getOffenderBookId())
                                .bookingNo(booking.getBookingNo())
                                .bookingActive("Y".equals(booking.getActiveFlag()))
                                .bookingBeginDate(booking.getBookingBeginDate())
                                .bookingEndDate(booking.getBookingEndDate())
                                .latestBooking("Y".equals(booking.getLatestBooking()))
                                .releaseDate(booking.getRelDate())
                                .legalCases(legalV1Repository.getBookingCases(booking.getOffenderBookId()).stream()
                                        .map(this::buildCase).collect(Collectors.toList()))
                                .build()
                )
                .collect(Collectors.toList());

        if (bookings.isEmpty()) {
            throw EntityNotFoundException.withId(nomsId);
        }

        return Bookings.builder().bookings(bookings).build();
    }

    private LegalCase buildCase(final LegalCaseSP lc) {
        return LegalCase.builder()
                .caseId(lc.getCaseId())
                .beginDate(lc.getBeginDate())
                .caseActive("A".equalsIgnoreCase(lc.getCaseStatus()))
                .caseInfoNumber(lc.getCaseInfoNumber())
                .court(CodeDescription.safeNullBuild(lc.getCourtCode(), lc.getCourtDesc()))
                .caseType(CodeDescription.safeNullBuild(lc.getCaseTypeCode(), lc.getCaseTypeDesc()))
                .charges(legalV1Repository.getCaseCharges(lc.getCaseId()).stream()
                        .map(this::buildCharge)
                        .collect(Collectors.toList()))
                .build();
    }

    private Charge buildCharge(final ChargeSP charge) {
        return Charge.builder()
                .offenderChargeId(charge.getOffenderChargeId())
                .statute(CodeDescription.safeNullBuild(charge.getStatuteCode(), charge.getStatuteDesc()))
                .offence(CodeDescription.safeNullBuild(charge.getOffenceCode(), charge.getOffenceDesc()))
                .band(CodeDescription.safeNullBuild(charge.getBandCode(), charge.getBandDesc()))
                .disposition(CodeDescription.safeNullBuild(charge.getDispositionCode(), charge.getDispositionDesc()))
                .imprisonmentStatus(CodeDescription.safeNullBuild(charge.getImprisonmentStatus(), charge.getImprisonmentStatusDesc()))
                .result(CodeDescription.safeNullBuild(charge.getResultCode(), charge.getResultDesc()))
                .noOfOffences(charge.getNoOfOffences())
                .chargeActive("A".equalsIgnoreCase(charge.getChargeStatus()))
                .mostSerious("Y".equals(charge.getMostSeriousFlag()))
                .convicted("Y".equalsIgnoreCase(charge.getConvictionFlag()))
                .severityRanking(charge.getSeverityRanking())
                .build();
    }

    public List<Alert> getAlerts(final String nomsId, final boolean includeInactive, final LocalDateTime modifiedSince) {
        final var alerts = alertV1Repository.getAlerts(nomsId, includeInactive, modifiedSince).stream()
                .filter(a -> a.getAlertSeq() != null)
                .map(a -> Alert.builder()
                        .type(CodeDescription.safeNullBuild(a.getAlertType(), a.getAlertTypeDesc()))
                        .subType(CodeDescription.safeNullBuild(a.getAlertCode(), a.getAlertCodeDesc()))
                        .date(a.getAlertDate())
                        .expiryDate(a.getExpiryDate())
                        .comment(a.getCommentText())
                        .status(a.getAlertStatus())
                        .build())
                .collect(Collectors.toList());

        if (alerts.isEmpty()) {
            throw EntityNotFoundException.withId(nomsId);
        }
        return alerts;
    }

    public Offender getOffender(final String nomsId) {
        return offenderV1Repository.getOffender(nomsId)
                .map(o -> Offender.builder()
                        .givenName(o.getFirstName())
                        .middleNames(o.getMiddleNames())
                        .surname(o.getLastName())
                        .birthDate(o.getBirthDate())
                        .title(o.getTitle())
                        .suffix(o.getSuffix())
                        .aliases(o.getOffenderAliases().stream().map(a -> OffenderAlias.builder()
                                .givenName(a.getFirstName())
                                .middleNames(a.getMiddleNames())
                                .surname(a.getLastName())
                                .birthDate(a.getBirthDate())
                                .build()).collect(Collectors.toList()))
                        .pncNumber(o.getPncNumber())
                        .croNumber(o.getCroNumber())
                        .nationalities(o.getNationalities())
                        .language(buildLanguage(o))
                        .convicted("Convicted".equalsIgnoreCase(o.getConvictedStatus()))
                        .ethnicity(CodeDescription.safeNullBuild(o.getEthnicityCode(), o.getEthnicityDesc()))
                        .gender(CodeDescription.safeNullBuild(o.getSexCode(), o.getSexDesc()))
                        .religion(CodeDescription.safeNullBuild(o.getReligionCode(), o.getReligionDesc()))
                        .csra(CodeDescription.safeNullBuild(o.getCsraCode(), o.getCsraDescription()))
                        .categorisationLevel(CodeDescription.safeNullBuild(o.getCatLevel(), o.getCatLevelDesc()))
                        .diet(CodeDescription.safeNullBuild(o.getDietCode(), o.getDietDesc()))
                        .iepLevel(CodeDescription.safeNullBuild(o.getIepLevel(), o.getIepLevelDesc()))
                        .imprisonmentStatus(CodeDescription.safeNullBuild(o.getImprisonmentStatus(), o.getImprisonmentStatusDesc()))
                        .diet(CodeDescription.safeNullBuild(o.getDietCode(), o.getDietDesc()))
                        .build())
                .orElseThrow(EntityNotFoundException.withId(nomsId));
    }

    private Language buildLanguage(final OffenderSP offender) {
        if (StringUtils.isNotBlank(offender.getSpokenLanguageCode())) {
            return Language.builder()
                    .interpreterRequired("Y".equals(offender.getInterpreterRequestedFlag()))
                    .spokenLanguage(CodeDescription.safeNullBuild(offender.getSpokenLanguageCode(), offender.getSpokenLanguageDesc()))
                    .build();
        }
        return null;
    }

    @Transactional
    public TransferSP transferTransaction(final String prisonId, final String nomsId, final String type, final String description, final BigDecimal amountInPounds, final LocalDate txDate, final String txId, final String uniqueClientId) {
        return financeV1Repository.postTransfer(prisonId, nomsId, type, description, amountInPounds, txDate, txId, uniqueClientId);
    }

    @Transactional
    public String createTransaction(final String prisonId, final String nomsId, final String type, final String description, final BigDecimal amountInPounds, final LocalDate txDate, final String txId, final String uniqueClientId) {
        return financeV1Repository.postTransaction(prisonId, nomsId, type, description, amountInPounds, txDate, txId, uniqueClientId);
    }

    public Image getOffenderImage(final String nomsId) {
        final var imageBytes = offenderV1Repository.getPhoto(nomsId).orElseThrow(EntityNotFoundException.withId(nomsId));

        return Image.builder().image(DatatypeConverter.printBase64Binary(imageBytes)).build();
    }

    /**
     * The repository creates a temporary CLOB in Oracle so direct this transaction to the writable DB.
     */
    @Transactional
    public OffenderPssDetailEvent getOffenderPssDetail(final String nomsId) {

        return offenderV1Repository.getOffenderPssDetail(nomsId)
                .map(o -> OffenderPssDetailEvent.builder()
                        .eventType(o.getEventType())
                        .nomsId(o.getNomsId())
                        .eventTimeStamp(LocalDateTime.ofInstant(o.getEventTimestamp().toInstant(), ZoneId.systemDefault()))
                        .id(o.getId())
                        .prisonId(o.getPrisonId())
                        .pssDetail(marshallDbJson(o.getEventData()))
                        .build())
                .orElseThrow(EntityNotFoundException.withId(nomsId));
    }

    public List<Hold> getHolds(final String prisonId, final String nomsId, final String uniqueClientId, final String clientName) {
        return financeV1Repository.getHolds(prisonId, nomsId, uniqueClientId)
                .stream()
                .map(h -> Hold.builder()
                        .clientUniqueRef(stripClientName(h.getClientUniqueRef(), clientName))
                        .holdNumber(h.getHoldNumber())
                        .holdUntilDate(h.getHoldUntilDate())
                        .amount(convertToPence(h.getTxnEntryAmount()))
                        .entryDate(h.getTxnEntryDate())
                        .description(h.getTxnEntryDesc())
                        .referenceNo(h.getTxnReferenceNumber())
                        .build())
                .collect(Collectors.toList());
    }

    private PssOffenderDetail marshallDbJson(final String dbJson) {
        PssOffenderDetail pssData = null;
        try {
            pssData = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(dbJson, PssOffenderDetail.class);
        }
        catch(Exception e) {
            log.error("Failed to parse/map JSON eventData {} data {}", e.getMessage(), dbJson);
        }
        return pssData;
    }

    private Long convertToPence(final BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValue();
    }

    private String stripClientName(final String clientUniqueRef, final String clientName) {
        if (StringUtils.isBlank(clientName)) {
            return clientUniqueRef;
        }
        if (StringUtils.startsWith(clientUniqueRef, clientName)) {
            return StringUtils.substringAfter(clientUniqueRef, clientName + "-");
        }
        return clientUniqueRef;
    }
}
