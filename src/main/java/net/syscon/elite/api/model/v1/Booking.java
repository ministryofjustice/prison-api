package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Booking {

    @JsonIgnore
    private Long offenderBookId;
    @JsonProperty("booking_no")
    private String bookingNo;
    @JsonProperty("booking_started")
    private LocalDate bookingBeginDate;
    @JsonProperty("booking_ended")
    private LocalDate bookingEndDate;
    @JsonProperty("booking_active")
    private boolean bookingActive;
    private Location location;
    @JsonIgnore
    private boolean latestBooking;
    @JsonProperty("release_date")
    private LocalDate releaseDate;
    @JsonProperty("legal_cases")
    private List<LegalCase> legalCases;

}
