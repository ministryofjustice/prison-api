package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
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
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.InmateAlertRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAlertFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAlertRepository;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer.mapSortProperty;

@Service
@Transactional(readOnly = true)
@Slf4j
public class InmateAlertService {

    private final InmateAlertRepository inmateAlertRepository;
    private final OffenderAlertRepository offenderAlertRepository;
    private final int maxBatchSize;

    @Autowired
    public InmateAlertService(
            final InmateAlertRepository inmateAlertRepository,
            final OffenderAlertRepository offenderAlertRepository,
            @Value("${batch.max.size:1000}") final int maxBatchSize) {

        this.inmateAlertRepository = inmateAlertRepository;
        this.offenderAlertRepository = offenderAlertRepository;
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

    private boolean isExpiredAlert(final Alert alert) {
        var expiredAlert = false;

        if (alert.getDateExpires() != null) {
            expiredAlert = !alert.getDateExpires().isAfter(LocalDate.now());
        }

        return expiredAlert;
    }
}
