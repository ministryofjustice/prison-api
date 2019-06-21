package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "Offender Booking")
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

    @ApiModelProperty(name = "booking_no", value = "Bookings", position = 0, required = true)
    @JsonProperty("booking_no")
    private String bookingNo;

    @ApiModelProperty(name = "booking_started", value = "Start Date of Booking", position = 1, required = true)
    @JsonProperty("booking_started")
    private LocalDate bookingBeginDate;

    @ApiModelProperty(name = "booking_ended", value = "End date of Booking", position = 2)
    @JsonProperty("booking_ended")
    private LocalDate bookingEndDate;

    @ApiModelProperty(name = "booking_active", value = "Booking Active?", example = "true", position = 3, required = true)
    @JsonProperty("booking_active")
    private boolean bookingActive;

    @ApiModelProperty(name = "location", value = "Location of Offender", position = 4)
    private Location location;

    @JsonIgnore
    private boolean latestBooking;

    @ApiModelProperty(name = "release_date", value = "Release Date", position = 5)
    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @ApiModelProperty(name = "legal_cases", value = "Legal Cases", position = 6, allowEmptyValue = true)
    @JsonProperty("legal_cases")
    private List<LegalCase> legalCases;

}
