package net.syscon.elite.service.v1;

import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.repository.v1.*;
import net.syscon.elite.repository.v1.model.BookingSP;
import net.syscon.elite.repository.v1.model.ChargeSP;
import net.syscon.elite.repository.v1.model.LegalCaseSP;
import net.syscon.elite.service.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('SYSTEM_USER','NOMIS_API_V1')")
public class NomisApiV1Service {

    private final BookingV1Repository bookingV1Repository;
    private final OffenderV1Repository offenderV1Repository;
    private final LegalV1Repository legalV1Repository;
    private final FinanceV1Repository financeV1Repository;
    private final AlertV1Repository alertV1Repository;

    public NomisApiV1Service(BookingV1Repository bookingV1Repository,
                             OffenderV1Repository offenderV1Repository,
                             LegalV1Repository legalV1Repository,
                             FinanceV1Repository financeV1Repository,
                             AlertV1Repository alertV1Repository) {
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

    private Location buildLocation(BookingSP l) {
        return Location.builder()
                .establishment(new CodeDescription(l.getAgyLocId(), l.getAgyLocDesc()))
                .housingLocation(StringUtils.isNotBlank(l.getHousingLocation()) ? new InternalLocation(l.getHousingLocation(), l.getHousingLevels()) : null)
                .build();
    }

    public Bookings getBookings(final String nomsId) {
        var bookings = bookingV1Repository.getOffenderBookings(nomsId).stream()
                .map(booking ->
                    Booking.builder()
                            .offenderBookId(booking.getOffenderBookId())
                            .bookingNo(booking.getBookingNo())
                            .bookingActive("Y".equals(booking.getActiveFlag()))
                            .bookingBeginDate(booking.getBookingBeginDate())
                            .bookingEndDate(booking.getBookingEndDate())
                            .location(buildLocation(booking))
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
                .court(CodeDescription.builder().code(lc.getCourtCode()).desc(lc.getCourtDesc()).build())
                .caseType(CodeDescription.builder().code(lc.getCaseTypeCode()).desc(lc.getCaseTypeDesc()).build())
                .charges(legalV1Repository.getCaseCharges(lc.getCaseId()).stream()
                            .map(this::buildCharge)
                            .collect(Collectors.toList()))
                .build();
    }

    private Charge buildCharge(final ChargeSP charge) {
        return Charge.builder()
                .offenderChargeId(charge.getOffenderChargeId())
                .statute(CodeDescription.builder().code(charge.getStatuteCode()).desc(charge.getStatuteDesc()).build())
                .offence(CodeDescription.builder().code(charge.getOffenceCode()).desc(charge.getOffenceDesc()).build())
                .band(CodeDescription.builder().code(charge.getBandCode()).desc(charge.getBandDesc()).build())
                .disposition(CodeDescription.builder().code(charge.getDispositionCode()).desc(charge.getDispositionDesc()).build())
                .imprisonmentStatus(CodeDescription.builder().code(charge.getImprisonmentStatus()).desc(charge.getImprisonmentStatusDesc()).build())
                .result(CodeDescription.builder().code(charge.getResultCode()).desc(charge.getResultDesc()).build())
                .noOfOffences(charge.getNoOfOffences())
                .chargeActive("Y".equalsIgnoreCase(charge.getChargeStatus()))
                .mostSerious("Y".equals(charge.getMostSeriousFlag()))
                .convicted("Y".equalsIgnoreCase(charge.getConvictionFlag()))
                .severityRanking(charge.getSeverityRanking())
                .build();
    }

    public List<Alert> getAlerts(final String nomsId, final boolean includeInactive, final LocalDateTime modifiedSince) {
        var alerts = alertV1Repository.getAlerts(nomsId, includeInactive, modifiedSince).stream()
                .filter(a -> a.getAlertSeq() != null)
                .map(a -> Alert.builder()
                        .type(CodeDescription.builder().code(a.getAlertType()).desc(a.getAlertTypeDesc()).build())
                        .subType(CodeDescription.builder().code(a.getAlertCode()).desc(a.getAlertCodeDesc()).build())
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
                        .aliases(o.getOffenderAliases().stream().map(a -> OffenderAlias.builder()
                                .givenName(a.getFirstName())
                                .middleNames(a.getMiddleNames())
                                .surname(a.getLastName())
                                .birthDate(a.getBirthDate())
                                .build()).collect(Collectors.toList()))
                        .pncNumber(o.getPncNumber())
                        .croNumber(o.getCroNumber())
                        .nationalities(o.getNationalities())
                        .language(Language.builder().interpreterRequired("Y".equals(o.getInterpreterRequestedFlag())).spokenLanguage(CodeDescription.builder().code(o.getSpokenLanguageCode()).desc(o.getSpokenLanguageDesc()).build()).build())
                        .convicted("Convicted".equalsIgnoreCase(o.getConvictedStatus()))
                        .ethnicity(CodeDescription.builder().code(o.getEthnicityCode()).desc(o.getEthnicityDesc()).build())
                        .gender(CodeDescription.builder().code(o.getSexCode()).desc(o.getSexDesc()).build())
                        .religion(CodeDescription.builder().code(o.getReligionCode()).desc(o.getReligionDesc()).build())
                        .csra(CodeDescription.builder().code(o.getCsraCode()).desc(o.getCsraDescription()).build())
                        .categorisationLevel(CodeDescription.builder().code(o.getCatLevel()).desc(o.getCatLevelDesc()).build())
                        .diet(CodeDescription.builder().code(o.getDietCode()).desc(o.getDietDesc()).build())
                        .iepLevel(CodeDescription.builder().code(o.getIepLevel()).desc(o.getIepLevelDesc()).build())
                        .imprisonmentStatus(CodeDescription.builder().code(o.getImprisonmentStatus()).desc(o.getImprisonmentStatusDesc()).build())
                        .diet(CodeDescription.builder().code(o.getDietCode()).desc(o.getDietDesc()).build())
                        .build())
                .orElseThrow(EntityNotFoundException.withId(nomsId));
    }

    @Transactional
    public String createTransaction(String prisonId, String nomsId, String type, String description, BigDecimal amountInPounds, LocalDate txDate, String txId, String uniqueClientId) {

        return financeV1Repository.postTransaction(prisonId, nomsId, type, description, amountInPounds, txDate, txId, uniqueClientId);
    }

    public Image getOffenderImage(final String nomsId) {
        return offenderV1Repository.getPhoto(nomsId).orElseThrow(EntityNotFoundException.withId(nomsId));
    }
}
