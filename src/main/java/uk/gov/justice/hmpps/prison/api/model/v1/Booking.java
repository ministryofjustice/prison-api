package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Offender Booking")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Booking {

    @JsonIgnore
    private Long offenderBookId;

    @Schema(name = "booking_no", description = "Bookings", requiredMode = REQUIRED, example = "A12313")
    @JsonProperty("booking_no")
    private String bookingNo;

    @Schema(name = "booking_started", description = "Start Date of Booking", requiredMode = REQUIRED, example = "2017-02-04")
    @JsonProperty("booking_started")
    private LocalDate bookingBeginDate;

    @Schema(name = "booking_ended", description = "End date of Booking", example = "2019-06-04")
    @JsonProperty("booking_ended")
    private LocalDate bookingEndDate;

    @Schema(name = "booking_active", description = "Booking Active?", example = "true", requiredMode = REQUIRED)
    @JsonProperty("booking_active")
    private boolean bookingActive;

    @Schema(name = "location", description = "Location of Offender")
    private Location location;

    @JsonIgnore
    private boolean latestBooking;

    @Schema(name = "release_date", description = "Release Date", example = "2019-02-04")
    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @Schema(name = "legal_cases", description = "Legal Cases")
    @JsonProperty("legal_cases")
    private List<LegalCase> legalCases;

}
