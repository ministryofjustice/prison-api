package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.resource.CellResource;
import uk.gov.justice.hmpps.prison.service.AgencyService;
import uk.gov.justice.hmpps.prison.service.BedAssignmentHistoryService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class CellResourceImpl implements CellResource {

    private BedAssignmentHistoryService bedAssignmentHistoryService;
    private AgencyService agencyService;

    public CellResourceImpl(final BedAssignmentHistoryService bedAssignmentHistoryService, final AgencyService agencyService) {
       this.bedAssignmentHistoryService = bedAssignmentHistoryService;
       this.agencyService = agencyService;
    }

    @Override
    public List<BedAssignment> getBedAssignmentsHistory(final Long locationId, final LocalDateTime fromDateTime, final LocalDateTime toDateTime) {
        return bedAssignmentHistoryService.getBedAssignmentsHistory(locationId, fromDateTime, toDateTime);
    }

    @Override
    public OffenderCell getCellAttributes(final Long locationId) {
        return agencyService.getCellAttributes(locationId);
    }
}
