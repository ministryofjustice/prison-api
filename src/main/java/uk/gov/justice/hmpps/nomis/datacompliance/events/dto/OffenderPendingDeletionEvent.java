package uk.gov.justice.hmpps.nomis.datacompliance.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This event signifies that an offender's
 * data is eligible for deletion, subject to
 * further checks by the Data Compliance Service.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderPendingDeletionEvent {
    private String offenderIdDisplay;
}

