package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Schema(description = "Bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Bookings {
    @Schema(description = "Bookings")
    private List<Booking> bookings;
}
