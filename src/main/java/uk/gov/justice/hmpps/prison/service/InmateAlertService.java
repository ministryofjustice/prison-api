package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.InmateAlertRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAlertFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAlertRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer.mapSortProperty;

@Service
@Transactional(readOnly = true)
@Slf4j
public class InmateAlertService {

    private final InmateAlertRepository inmateAlertRepository;
    private final OffenderAlertRepository offenderAlertRepository;
    private final AuthenticationFacade authenticationFacade;
    private final TelemetryClient telemetryClient;
    private final ReferenceDomainService referenceDomainService;
    private final int maxBatchSize;

    @Autowired
    public InmateAlertService(
            final InmateAlertRepository inmateAlertRepository,
            final OffenderAlertRepository offenderAlertRepository,
            final AuthenticationFacade authenticationFacade,
            final TelemetryClient telemetryClient,
            final ReferenceDomainService referenceDomainService,
            @Value("${batch.max.size:1000}") final int maxBatchSize) {

        this.inmateAlertRepository = inmateAlertRepository;
        this.offenderAlertRepository = offenderAlertRepository;
        this.authenticationFacade = authenticationFacade;
        this.telemetryClient = telemetryClient;
        this.referenceDomainService = referenceDomainService;
        this.maxBatchSize = maxBatchSize;
    }

    public Page<Alert> getInmateAlerts(final Long bookingId, final String orderBy, final Order order, final long offset, final long limit) {
        final var orderByBlank = StringUtils.isBlank(orderBy);

        final var alerts = inmateAlertRepository.getAlerts(//
                bookingId, //
                orderByBlank ? "dateExpires,dateCreated" : orderBy, //
                orderByBlank ? Order.DESC : order, //
                offset, limit);

        alerts.getItems().forEach(alert -> alert.setExpired(isExpiredAlert(alert)));

        log.info("Returning {} of {} matching Alerts starting at {} for bookingId {}", alerts.getItems().size(), alerts.getTotalRecords(), alerts.getPageOffset(), bookingId);
        return alerts;
    }

    public Alert getInmateAlert(final Long bookingId, final Long alertSeqId) {
        final var alert = inmateAlertRepository.getAlert(bookingId, alertSeqId)
                .orElseThrow(EntityNotFoundException.withId(alertSeqId));

        alert.setExpired(isExpiredAlert(alert));

        log.info("Returning Alert having alertSeqId {}, for bookingId {}", alertSeqId, bookingId);
        return alert;
    }

    public List<Alert> getInmateAlertsByOffenderNosAtAgency(final String agencyId, final List<String> offenderNos) {

        final var alerts = Lists.partition(offenderNos, maxBatchSize)
                .stream()
                .flatMap(offenderNosList -> inmateAlertRepository.getAlertsByOffenderNos(agencyId, offenderNosList, true, "bookingId,alertId", Order.ASC).stream())
                .toList();

        alerts.forEach(alert -> alert.setExpired(isExpiredAlert(alert)));

        log.info("Returning {} matching Alerts for Offender Numbers {} in Agency '{}'", alerts.size(), offenderNos, agencyId);
        return alerts;
    }

    public List<Alert> getInmateAlertsByOffenderNos(final List<String> offenderNos, final boolean latestOnly, final String orderByField, final Order order) {

        final var alerts = inmateAlertRepository.getAlertsByOffenderNos(null, offenderNos, latestOnly,  orderByField, order);
        alerts.forEach(alert -> alert.setExpired(isExpiredAlert(alert)));
        log.info("Returning {} matching Alerts for Offender Numbers {}", alerts.size(), offenderNos);
        return alerts;
    }

    public List<Alert> getAlertsForLatestBookingForOffender(final String offenderNo, final String alertCodes, final String sortProperties, final Direction direction) {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo(offenderNo)
            .latestBooking(true)
            .alertCodes(alertCodes)
            .build();

        return getAlertsForOffender(offenderNo, sortProperties, direction, filter);
    }

    public List<Alert> getAlertsForAllBookingsForOffender(final String offenderNo, final String alertCodes, final String sortProperties, final Direction direction) {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo(offenderNo)
            .alertCodes(alertCodes)
            .build();

        return getAlertsForOffender(offenderNo, sortProperties, direction, filter);
    }

    private List<Alert> getAlertsForOffender(String offenderNo, String sortProperties, Direction direction, OffenderAlertFilter filter) {
        final var alerts = offenderAlertRepository
            .findAll(filter, Sort.by(direction, OffenderAlertTransformer.mapSortProperties(sortProperties)))
            .stream()
            .map(OffenderAlertTransformer::transformForOffender)
            .toList();
        log.info("Returning {} matching Alerts for Offender Number {}", alerts.size(), offenderNo);
        return alerts;
    }

    public org.springframework.data.domain.Page<Alert> getAlertsForBooking(final Long bookingId, final LocalDate fromAlertDate, final LocalDate toAlertDate, final String alertType, final String alertStatus, final Pageable pageable) {
        final var filter = OffenderAlertFilter
            .builder()
            .bookingId(bookingId)
            .fromAlertDate(fromAlertDate)
            .toAlertDate(toAlertDate)
            .alertTypes(alertType)
            .status(alertStatus)
            .build();

        final var pageOfAlerts = offenderAlertRepository.findAll(
            filter,
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mapAlertSortOrderProperties(pageable.getSort())));

        log.info("Returning {} of {} matching Alerts starting at page {} for bookingId {}", pageOfAlerts.getNumberOfElements(), pageOfAlerts.getTotalElements(), pageOfAlerts.getNumber(), bookingId);
        return pageOfAlerts.map(OffenderAlertTransformer::transformForBooking);
    }

    private Sort mapAlertSortOrderProperties(Sort sort) {
        return Sort.by(sort
            .stream()
            .map(order -> Sort.Order
                .by(mapSortProperty(order.getProperty()))
                .with(order.getDirection()))
            .toList());
    }

    @Transactional
    public long createNewAlert(final long bookingId, final CreateAlert alert) {
        final var today = LocalDate.now();
        final var sevenDaysAgo = LocalDate.now().minusDays(7);

        final var existingAlertCode = referenceDomainService
                .getReferenceCodeByDomainAndCode(ReferenceDomain.ALERT.getDomain(), alert.getAlertType(), true)
                .orElseThrow(() -> new IllegalArgumentException("Alert type does not exist."));

        final var isValidAlertCode = existingAlertCode
                .getSubCodes()
                .stream()
                .anyMatch(subCode -> subCode.getCode().equals(alert.getAlertCode()));

        if (!isValidAlertCode)
            throw new IllegalArgumentException("Alert code does not exist.");

        if (alert.getAlertDate().isAfter(today))
            throw new IllegalArgumentException("Alert date cannot be in the future.");

        if (alert.getAlertDate().isBefore(sevenDaysAgo))
            throw new IllegalArgumentException("Alert date cannot go back more than seven days.");

        if (alert.getExpiryDate() != null && alert.getExpiryDate().isBefore(today))
            throw new IllegalArgumentException("Expiry date cannot be in the past.");

        final var existingActiveAlerts = inmateAlertRepository.getActiveAlerts(bookingId);
        final var matches = existingActiveAlerts
                .stream().anyMatch(al -> al.getAlertType().equals(alert.getAlertType()) &&
                        al.getAlertCode().equals(alert.getAlertCode()));

        if (matches) throw new IllegalArgumentException("Alert already exists for this offender.");

        final var username = authenticationFacade.getCurrentUsername();
        final var alertId = inmateAlertRepository.createNewAlert(bookingId, alert);

        log.info("Created new alert {}", alert);
        var createdAlert = new HashMap<>(Map.of(
                "bookingId", String.valueOf(bookingId),
                "alertSeq", String.valueOf(alertId),
                "alertDate", alert.getAlertDate().toString(),
                "alertCode", alert.getAlertCode(),
                "alertType", alert.getAlertType(),
                "created_by", username
        ));
        if (alert.getExpiryDate() != null) {
            createdAlert.put("expiryDate", alert.getExpiryDate().toString());
        }
        telemetryClient.trackEvent("Alert created", createdAlert, null);

        return alertId;
    }

    @Transactional
    public Alert updateAlert(final long bookingId, final long alertSeq, final AlertChanges alertChanges, final boolean lockTimeout) {
        final var existingAlert = inmateAlertRepository.getAlert(bookingId, alertSeq)
            .orElseThrow(EntityNotFoundException.withId(alertSeq));

        if (!alertChanges.isRemoveExpiryDate() && alertChanges.getExpiryDate() == null && StringUtils.isBlank(alertChanges.getComment())) {
            throw new IllegalArgumentException("Please provide an expiry date, or a comment");
        }
        if (lockTimeout) {
            inmateAlertRepository.lockAlert(bookingId, alertSeq);
        }
        if (alertChanges.isRemoveExpiryDate()) {
            return unexpireAlert(bookingId, alertSeq, alertChanges, existingAlert);
        }
        if (alertChanges.getExpiryDate() == null && StringUtils.isNotBlank(alertChanges.getComment())) {
            return updateAlertComment(bookingId, alertSeq, alertChanges);
        }
        return expireAlert(bookingId, alertSeq, alertChanges, existingAlert);
    }

    private Alert updateAlertComment(final long bookingId, final long alertSeq, final AlertChanges alertChanges) {
        final var username = authenticationFacade.getCurrentUsername();

        var alert = inmateAlertRepository.updateAlert(bookingId, alertSeq, AlertChanges.builder().comment(alertChanges.getComment()).build())
                .orElseThrow(EntityNotFoundException.withId(alertSeq));

        log.info("updateAlertComment() Alert updated {}", alert);
        telemetryClient.trackEvent("Alert updated", Map.of(
                "bookingId", String.valueOf(bookingId),
                "alertSeq", String.valueOf(alertSeq),
                "comment", "Comment text updated",
                "updated_by", username
        ), null);

        return alert;
    }

    private Alert expireAlert(final long bookingId, final long alertSeq, final AlertChanges alertChanges, final Alert existingAlert) {
        final var username = authenticationFacade.getCurrentUsername();

        if (!existingAlert.isActive())
            throw new IllegalArgumentException("Alert is already inactive.");

        final var alert = inmateAlertRepository.updateAlert(bookingId, alertSeq, alertChanges)
                .orElseThrow(EntityNotFoundException.withId(alertSeq));

        log.info("expireAlert() Alert updated {}", alert);
        telemetryClient.trackEvent("Alert updated", Map.of(
                "bookingId", String.valueOf(bookingId),
                "alertSeq", String.valueOf(alertSeq),
                "expiryDate", alertChanges.getExpiryDate().toString(),
                "updated_by", username
        ), null);
        return alert;
    }

    private Alert unexpireAlert(final long bookingId, final long alertSeq, final AlertChanges alertChanges, final Alert existingAlert) {
        final var username = authenticationFacade.getCurrentUsername();

        if (!existingAlert.isActive())
            throw new IllegalArgumentException("Alert is already inactive.");

        final var alert = inmateAlertRepository.updateAlert(bookingId, alertSeq, alertChanges)
                .orElseThrow(EntityNotFoundException.withId(alertSeq));

        log.info("unexpireAlert() Alert updated {}", alert);
        telemetryClient.trackEvent("Alert updated", Map.of(
                "bookingId", String.valueOf(bookingId),
                "alertSeq", String.valueOf(alertSeq),
                "expiryDate", "Expiry date removed",
                "updated_by", username
        ), null);
        return alert;
    }

    private boolean isExpiredAlert(final Alert alert) {
        var expiredAlert = false;

        if (alert.getDateExpires() != null) {
            expiredAlert = !alert.getDateExpires().isAfter(LocalDate.now());
        }

        return expiredAlert;
    }
}
