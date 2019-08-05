package net.syscon.elite.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.AlertSubtype;
import net.syscon.elite.api.model.AlertType;
import net.syscon.elite.repository.AlertRepository;
import net.syscon.elite.service.AlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Alert API service implementation.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class AlertServiceImpl implements AlertService {
    private final AlertRepository alertRepository;

    public AlertServiceImpl(final AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public List<AlertType> getAlertTypes() {
        return alertRepository.getAlertTypes();
    }

    @Override
    public List<AlertSubtype> getAlertSubtypes(String parentCode) {
        return alertRepository.getAlertSubtypes(parentCode);
    }
}
