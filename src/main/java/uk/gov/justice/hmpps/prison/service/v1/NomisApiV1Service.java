package uk.gov.justice.hmpps.prison.service.v1;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountBalance;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.ActiveOffender;
import uk.gov.justice.hmpps.prison.api.model.v1.AlertV1;
import uk.gov.justice.hmpps.prison.api.model.v1.AvailableDates;
import uk.gov.justice.hmpps.prison.api.model.v1.Booking;
import uk.gov.justice.hmpps.prison.api.model.v1.Bookings;
import uk.gov.justice.hmpps.prison.api.model.v1.Charge;
import uk.gov.justice.hmpps.prison.api.model.v1.CodeDescription;
import uk.gov.justice.hmpps.prison.api.model.v1.ContactList;
import uk.gov.justice.hmpps.prison.api.model.v1.ContactPerson;
import uk.gov.justice.hmpps.prison.api.model.v1.Event;
import uk.gov.justice.hmpps.prison.api.model.v1.Hold;
import uk.gov.justice.hmpps.prison.api.model.v1.Image;
import uk.gov.justice.hmpps.prison.api.model.v1.InternalLocation;
import uk.gov.justice.hmpps.prison.api.model.v1.Language;
import uk.gov.justice.hmpps.prison.api.model.v1.LegalCase;
import uk.gov.justice.hmpps.prison.api.model.v1.Location;
import uk.gov.justice.hmpps.prison.api.model.v1.Offender;
import uk.gov.justice.hmpps.prison.api.model.v1.OffenderAlias;
import uk.gov.justice.hmpps.prison.api.model.v1.PaymentResponse;
import uk.gov.justice.hmpps.prison.api.model.v1.UnavailabilityReason;
import uk.gov.justice.hmpps.prison.api.model.v1.VisitRestriction;
import uk.gov.justice.hmpps.prison.api.model.v1.VisitSlotCapacity;
import uk.gov.justice.hmpps.prison.api.model.v1.VisitSlots;
import uk.gov.justice.hmpps.prison.api.resource.v1.impl.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.repository.v1.AlertV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.BookingV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.CoreV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.EventsV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.FinanceV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.LegalV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.OffenderV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.PrisonV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.VisitV1Repository;
import uk.gov.justice.hmpps.prison.repository.v1.model.AvailableDatesSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.BookingSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.ChargeSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.ContactPersonSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.EventSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.LegalCaseSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.LiveRollSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.OffenderSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.TransferSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.UnavailabilityReasonSP;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.justice.hmpps.prison.values.AccountCode.codeForNameOrEmpty;

@Slf4j
@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('SYSTEM_USER','NOMIS_API_V1')")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NomisApiV1Service {

    private final BookingV1Repository bookingV1Repository;
    private final OffenderV1Repository offenderV1Repository;
    private final LegalV1Repository legalV1Repository;
    private final FinanceV1Repository financeV1Repository;
    private final AlertV1Repository alertV1Repository;
    private final EventsV1Repository eventsV1Repository;
    private final PrisonV1Repository prisonV1Repository;
    private final CoreV1Repository coreV1Repository;
    private final VisitV1Repository visitV1Repository;

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

    public List<AlertV1> getAlerts(final String nomsId, final boolean includeInactive, final LocalDateTime modifiedSince) {
        final var alerts = alertV1Repository.getAlerts(nomsId, includeInactive, modifiedSince).stream()
                .filter(a -> a.getAlertSeq() != null)
                .map(a -> AlertV1.builder()
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

    public List<Event> getEvents(final String prisonId, final OffenderIdentifier offenderIdentifier, final String eventType, final LocalDateTime fromDateTime, final Long limit) {
        return eventsV1Repository.getEvents(prisonId,
                offenderIdentifier.getNomsId(),
                offenderIdentifier.getRootOffenderId(),
                offenderIdentifier.getSingleOffenderId(),
                eventType, fromDateTime, limit)
                .stream()
                .map(e -> Event.builder()
                        .prisonId(e.getAgyLocId())
                        .nomsId(e.getNomsId())
                        .id(e.getApiEventId())
                        .timestamp(e.getEventTimestamp())
                        .type(e.getEventType())
                        .eventData(convertToJsonString(e))
                        .build())
                .collect(Collectors.toList());
    }

    private String convertToJsonString(final EventSP e) {
        final var json = StringUtils.trimToEmpty(e.getEventData_1()) + StringUtils.trimToEmpty(e.getEventData_2()) + StringUtils.trimToEmpty(e.getEventData_3());
        return StringUtils.defaultIfEmpty(json, "{}");
    }

    public Image getOffenderImage(final String nomsId) {
        final var imageBytes = offenderV1Repository.getPhoto(nomsId).orElseThrow(EntityNotFoundException.withId(nomsId));

        return Image.builder().image(DatatypeConverter.printBase64Binary(imageBytes)).build();
    }

    public Event getOffenderPssDetail(final String nomsId) {

        return offenderV1Repository.getOffenderPssDetail(nomsId)
                .map(e -> Event.builder()
                        .type(e.getEventType())
                        .nomsId(e.getNomsId())
                        .timestamp(e.getEventTimestamp())
                        .id(e.getApiEventId())
                        .prisonId(e.getAgyLocId())
                        .eventData(e.getEventData_1())
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

    public List<String> getLiveRoll(final String prisonId) {
        return prisonV1Repository.getLiveRoll(prisonId).stream().map(LiveRollSP::getOffenderIdDisplay).collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse storePayment(final String prisonId, final String nomsId, final String payType, final String payDesc, final BigDecimal payAmount, final LocalDate payDate, final String payClientRef) {
        // No return value from repository - a runtime exception will be thrown in the event of problems
        financeV1Repository.postStorePayment(prisonId, nomsId, payType, payDesc, payAmount, payDate, payClientRef);
        return PaymentResponse.builder().message("Payment accepted").build();
    }

    public AccountBalance getAccountBalances(final String prisonId, final String nomsId) {
        final var response = financeV1Repository.getAccountBalances(prisonId, nomsId);
        return AccountBalance.builder()
                .cash(convertToPence(response.get("cash")))
                .spends(convertToPence(response.get("spends")))
                .savings(convertToPence(response.get("savings")))
                .build();
    }

    public List<AccountTransaction> getAccountTransactions(final String prisonId, final String nomsId, final String accountCode, final LocalDate fromDate, final LocalDate toDate) {

        final var accountType = codeForNameOrEmpty(accountCode);
        if (StringUtils.isEmpty(accountType)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid account_code supplied. Should be one of cash, spends or savings");
        }

        return financeV1Repository.getAccountTransactions(prisonId, nomsId, accountType, fromDate, toDate)
                .stream()
                .map(t -> AccountTransaction.builder()
                        .id("" + t.getTxnId() + "-" + t.getTxnEntrySeq())
                        .type(CodeDescription.safeNullBuild(t.getTxnType(), t.getTxnTypeDesc()))
                        .description(t.getTxnEntryDesc())
                        .amount(convertToPence(t.getTxnEntryAmount()))
                        .date(t.getTxnEntryDate())
                        .build())
                .collect(Collectors.toList());
    }

    public AccountTransaction getTransactionByClientUniqueRef(final String prisonId, final String nomsId, final String uniqueClientId) {

        final var response = financeV1Repository.getTransactionByClientUniqueRef(prisonId, nomsId, uniqueClientId)
                .stream()
                .map(t -> AccountTransaction.builder()
                        .id("" + t.getTxnId() + "-" + t.getTxnEntrySeq())
                        .type(CodeDescription.safeNullBuild(t.getTxnType(), t.getTxnTypeDesc()))
                        .description(t.getTxnEntryDesc())
                        .amount(convertToPence(t.getTxnEntryAmount()))
                        .date(t.getTxnEntryDate())
                        .build())
                .collect(Collectors.toList());

        if (response.size() == 1) {
            return response.get(0);
        }
        if (response.size() == 0) {
            throw EntityNotFoundException.withId(uniqueClientId);
        }
        throw new RuntimeException("Found two transaction with same Client Unique Ref which shouldn't happen");
    }

    public ActiveOffender getActiveOffender(final String nomsId, final LocalDate birthDate) {
        final var id = coreV1Repository.getActiveOffender(nomsId, birthDate);
        return new ActiveOffender(id);
    }

    public AvailableDates getVisitAvailableDates(final Long offenderID, final LocalDate fromDate, final LocalDate toDate) {
        validateStartAndEndDateRange(fromDate, toDate);

        final var dates = visitV1Repository.getAvailableDates(offenderID, fromDate, toDate);

        return AvailableDates.builder().dates(dates.stream().map(AvailableDatesSP::getSlotDate).collect(Collectors.toList())).build();
    }

    public ContactList getVisitContactList(final Long offenderID) {

        final Map<ContactPerson, List<ContactPersonSP>> response = visitV1Repository.getContactList(offenderID).stream()
                .collect(Collectors.groupingBy(this::convertToPerson, LinkedHashMap::new, Collectors.toList()));

        final var contactPersonList = response.entrySet().stream().map(e -> {
            e.getKey().setRestrictions(addToPerson(e.getValue()));
            return e.getKey();
        }).collect(Collectors.toList());

        return new ContactList(contactPersonList);
    }

    public SortedMap<String, UnavailabilityReason> getVisitUnavailability(final Long offenderId, final String dates) {
        final var dateArray = validateDates(dates);

        return combineResultsIntoMap(dateArray, visitV1Repository.getUnavailability(offenderId, dates));
    }

    public VisitSlots getVisitSlotsWithCapacity(final String prisonId, final LocalDate fromDate, final LocalDate toDate) {
        validateStartAndEndDateRange(fromDate, toDate);

        final var response = visitV1Repository.getVisitSlotsWithCapacity(prisonId, fromDate, toDate).stream()
                .map(v -> VisitSlotCapacity.builder()
                        .time(v.getSlotStart().format(ofPattern("yyyy-MM-dd'T'HH:mm")) + "/" + v.getSlotEnd().format(ofPattern("HH:mm")))
                        .capacity(v.getCapacity())
                        .maxGroups(v.getMaxGroups())
                        .maxAdults(v.getMaxAdults())
                        .groupsBooked(v.getGroupsBooked())
                        .visitorsBooked(v.getVisitorsBooked())
                        .adultsBooked(v.getAdultsBooked())
                        .build())
                .collect(Collectors.toList());

        return new VisitSlots(response);
    }

    private void validateStartAndEndDateRange(final LocalDate fromDate, final LocalDate toDate) {
        final var cutOffDays = 60;
        final var now = LocalDate.now();

        if (fromDate.isBefore(now)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Start date cannot be in the past");

        if (toDate.isBefore(fromDate)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "End date cannot be before the start date");

        if (toDate.isAfter(now.plusDays(cutOffDays)))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "End date cannot be more than 60 days in the future");
    }

    private List<VisitRestriction> addToPerson(final List<ContactPersonSP> value) {

        return value.stream().filter(r -> StringUtils.isNotEmpty(r.getRestrictionTypeCode())).map(r ->
                VisitRestriction.builder()
                        .restrictionType(CodeDescription.safeNullBuild(r.getRestrictionTypeCode(), r.getRestrictionTypeDesc()))
                        .effectiveDate(r.getRestrictionEffectiveDate())
                        .expiryDate(r.getRestrictionExpiryDate())
                        .commentText(r.getCommentText()).build()).collect(Collectors.toList());
    }

    private ContactPerson convertToPerson(final ContactPersonSP c) {
        return ContactPerson.builder()
                ._id(c.getOffenderContactPersonId())
                .id(c.getPersonId())
                .firstName(c.getFirstName())
                .middleName(c.getMiddleName())
                .lastName(c.getLastName())
                .dateOfBirth(c.getBirthDate())
                .gender(CodeDescription.safeNullBuild(c.getSexCode(), c.getSexDesc()))
                .contactType(CodeDescription.safeNullBuild(c.getContactTypeCode(), c.getContactTypeDesc()))
                .approvedVisitor("Y".equals(c.getApprovedVisitorFlag()))
                .active("Y".equals(c.getActiveFlag()))
                .relationshipType(CodeDescription.safeNullBuild(c.getRelationshipTypeCode(), c.getRelationshipTypeDesc())).build();
    }

    private List<LocalDate> validateDates(final String dates) {
        if (dates == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing date selection");
        }
        return Arrays.stream(dates.split(",")).map(LocalDate::parse).peek(localDate -> {
            if (!localDate.isAfter(LocalDate.now())) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Dates requested must be in future");
        }).collect(Collectors.toList());
    }

    private SortedMap<String, UnavailabilityReason> combineResultsIntoMap(final List<LocalDate> dates, final List<UnavailabilityReasonSP> response) {
        final var dateMap = dates.stream().collect(Collectors.toMap(String::valueOf, date -> new UnavailabilityReason(), (a, b) -> b, TreeMap::new));
        response.forEach(r -> dateMap.computeIfPresent(r.getEventDateAsString(), (s, unavailableDate) -> unavailableDate.update(r)));

        return dateMap;
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
