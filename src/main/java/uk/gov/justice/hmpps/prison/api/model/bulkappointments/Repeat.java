package uk.gov.justice.hmpps.prison.api.model.bulkappointments;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Repeat {
    @Schema(required = true, description = "The period at which the appointment should repeat.", example = "WEEKLY", allowableValues = "DAILY, WEEKDAYS, WEEKLY, FORTNIGHTLY, MONTHLY")
    @NotNull
    private RepeatPeriod repeatPeriod;

    @Schema(required = true, description = "The total number of appointments. Must be greater than 0")
    @Min(1)
    @NotNull
    private Integer count;

    /**
     * Given an initial LocalDateTime to start from build a Stream of LocalDateTime which corresponds to this Repeat.
     *
     * @param startDateTime The starting LocalDateTime
     * @return a Stream of (count) instances of LocalDateTime, starting with startDateTime according to the values in this Repeat instance.
     */
    public Stream<LocalDateTime> dateTimeStream(final LocalDateTime startDateTime) {
        return LongStream.range(0, count).mapToObj(l -> repeatPeriod.endDateTime(startDateTime, l));
    }
}
