package uk.gov.justice.hmpps.nomis.datacompliance.events.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * This event signifies that an offender
 * has been successfully deleted in NOMIS
 * and that downstream services should handle
 * appropriately.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OffenderDeletionCompleteEvent {

    @JsonProperty("offenderIdDisplay")
    private String offenderIdDisplay;

    @Singular
    @JsonProperty("offenders")
    private List<OffenderWithBookings> offenders;

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OffenderWithBookings {

        @JsonProperty("offenderId")
        private Long offenderId;

        @Singular
        @JsonProperty("bookings")
        private List<Booking> bookings;
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Booking {

        @JsonProperty("offenderBookId")
        private Long offenderBookId;
    }
}

