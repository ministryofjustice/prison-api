package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.AlertSubtype;
import net.syscon.elite.api.model.AlertType;
import net.syscon.elite.api.resource.AlertResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AlertService;

import javax.ws.rs.Path;
import java.util.List;

/**
 * Implementation of /alerts endpoint.
 */
@RestResource
@Path("/alerts")
public class AlertResourceImpl implements AlertResource {
    private final AlertService alertService;

    public AlertResourceImpl(final AlertService alertService) { this.alertService = alertService; }

    @Override
    public List<AlertType> getAlertTypes() {
        return alertService.getAlertTypes();
    }

    @Override
    public List<AlertSubtype> getAlertSubtypes(String parentCode) {
        return alertService.getAlertSubtypes(parentCode);
    }
}
