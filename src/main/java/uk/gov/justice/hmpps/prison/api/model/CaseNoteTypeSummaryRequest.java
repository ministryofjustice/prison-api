package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Case Note Type Usage Request
 **/
@SuppressWarnings("unused")
@Schema(description = "Case Note Type Usage Request by Date grouped by bookings")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteTypeSummaryRequest {

    @Builder.Default
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "a list of booking id to from date to search. Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered")
    private List<BookingFromDatePair> bookingFromDateSelection = new ArrayList<>();

    @Schema(description = "Case note types")
    private List<String> types;

    @Schema(description = "Booking Id to case note from date pair")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Data
    public static class BookingFromDatePair {

        @Schema(description = "Booking Id")
        private Long bookingId;

        @Schema(description = "Only case notes occurring on or after this date (in YYYY-MM-DDTHH:MM:SS format) will be considered.", example = "2018-11-01T12:00:00")
        private LocalDateTime fromDate;

    }
}
