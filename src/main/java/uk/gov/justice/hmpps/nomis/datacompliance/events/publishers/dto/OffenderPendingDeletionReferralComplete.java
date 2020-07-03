package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.nomis.datacompliance.controller.DataComplianceController;
import uk.gov.justice.hmpps.prison.api.model.PendingDeletionRequest;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * This event signifies that the process of publishing
 * events for offenders pending deletion is complete.
 * The requestId matches the value provided in the POST request:
 * {@link DataComplianceController#requestOffendersPendingDeletion(PendingDeletionRequest)}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class OffenderPendingDeletionReferralComplete {
    private Long batchId;
    private Long numberReferred;
    private Long totalInWindow;
}

