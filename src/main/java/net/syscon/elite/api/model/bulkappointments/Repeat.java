package net.syscon.elite.api.model.bulkappointments;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Data
@Builder
public class Repeat {
    @ApiModelProperty(required = true, value = "The period at which the appointment should repeat.", example = "WEEKLY", allowableValues = "DAILY, WEEKDAYS, WEEKLY, FORTNIGHTLY, MONTHLY")
    @NotNull
    private RepeatPeriod repeatPeriod;

    @ApiModelProperty(required = true, value = "The total number of appointments. Must be greater than 0", position = 1)
    @Min(1)
    @NotNull
    private Integer count;

    /**
     * Given an initial LocalDateTime to start from build a Stream of LocalDateTime which corresponds to this Repeat.
     * @param startDateTime The starting LocalDateTime
     * @return a Stream of (count) instances of LocalDateTime, starting with startDateTime according to the values in this Repeat instance.
     */
    public Stream<LocalDateTime> dateTimeStream(LocalDateTime startDateTime) {
        return Stream.iterate(startDateTime, repeatPeriod::next).limit(count);
    }

    public Duration duration() {
        final var first = LocalDateTime.now();
        final var last =  dateTimeStream(first).skip(count-1).findFirst();
        return Duration.between(first, last.orElseThrow(RuntimeException::new)); // The exception will never be thrown.
    }
}
