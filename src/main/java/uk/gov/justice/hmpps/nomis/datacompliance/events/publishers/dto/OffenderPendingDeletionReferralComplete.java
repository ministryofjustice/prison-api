package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * This event signifies that the process of publishing
 * events for offenders pending deletion is complete.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class OffenderPendingDeletionReferralComplete {
    private Long batchId;
    private Long numberReferred;
    private Long totalInWindow;
}

