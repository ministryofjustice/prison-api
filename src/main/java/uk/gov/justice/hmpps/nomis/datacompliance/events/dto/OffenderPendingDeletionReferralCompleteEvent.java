package uk.gov.justice.hmpps.nomis.datacompliance.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.syscon.elite.api.model.PendingDeletionRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.controller.DataComplianceController;

/**
 * This event signifies that the process of publishing
 * events for offenders pending deletion is complete.
 * The requestId matches the value provided in the POST request:
 * {@link DataComplianceController#requestOffenderPendingDeletions(PendingDeletionRequest)}
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderPendingDeletionReferralCompleteEvent {
    private Long batchId;
}

