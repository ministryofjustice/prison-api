package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.api.resource.CellResource;
import uk.gov.justice.hmpps.prison.service.BedAssignmentHistoryService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class CellResourceImpl implements CellResource {

    private BedAssignmentHistoryService bedAssignmentHistoryService;

    public CellResourceImpl(final BedAssignmentHistoryService bedAssignmentHistoryService) {
       this.bedAssignmentHistoryService = bedAssignmentHistoryService;
    }

    @Override
    public List<BedAssignment> getBedAssignmentsHistory(final Long locationId, final LocalDate fromDate, final LocalDate toDate) {
        return bedAssignmentHistoryService.getBedAssignmentsHistory(locationId, fromDate, toDate);
    }
}
