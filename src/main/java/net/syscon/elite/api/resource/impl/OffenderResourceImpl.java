package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.api.resource.BookingResource.GetAlertsByOffenderNosResponse;
import net.syscon.elite.api.resource.IncidentsResource.IncidentListResponse;
import net.syscon.elite.api.resource.OffenderResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateAlertService;
import net.syscon.elite.service.OffenderAddressService;
import net.syscon.elite.service.impl.IncidentService;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/offenders")
public class OffenderResourceImpl implements OffenderResource {

    private final IncidentService incidentService;
    private final InmateAlertService alertService;
    private final OffenderAddressService addressService;

    public OffenderResourceImpl(final IncidentService incidentService, final InmateAlertService alertService,
                                final OffenderAddressService addressService) {
        this.incidentService = incidentService;
        this.alertService = alertService;
        this.addressService = addressService;
    }

    @Override
    public IncidentListResponse getIncidentsByOffenderNo(@NotNull final String offenderNo, final List<String> incidentTypes, final List<String> participationRoles) {
        return new IncidentListResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(),
                incidentService.getIncidentCasesByOffenderNo(offenderNo, incidentTypes, participationRoles));
    }

    @Override
    public List<OffenderAddress> getAddressesByOffenderNo(@NotNull String offenderNo) {
        return addressService.getAddressesByOffenderNo(offenderNo);
    }

    @Override
    public GetAlertsByOffenderNosResponse getAlertsByOffenderNo(@NotNull final String offenderNo, final Boolean latestOnly, final String query, final String sortFields, final Order sortOrder) {
        final List<Alert> inmateAlertsByOffenderNos = alertService.getInmateAlertsByOffenderNos(
                List.of(offenderNo),
                nvl(latestOnly, true),
                query,
                StringUtils.defaultIfBlank(sortFields,"bookingId,alertId"),
                nvl(sortOrder, Order.ASC));
        return GetAlertsByOffenderNosResponse.respond200WithApplicationJson(inmateAlertsByOffenderNos);
    }
}
