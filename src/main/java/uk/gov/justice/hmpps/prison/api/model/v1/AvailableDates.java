package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Available Dates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AvailableDates {

    @Schema(description = "Available Dates")
    private List<LocalDate> dates;
}
