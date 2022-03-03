package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @ApiModelProperty(name = "booking_no", value = "Bookings", position = 1, required = true, example = "A12313")
    @JsonProperty("booking_no")
    private String bookingNo;

    @ApiModelProperty(name = "booking_started", value = "Start Date of Booking", position = 2, required = true, example = "2017-02-04")
    @JsonProperty("booking_started")
    private LocalDate bookingBeginDate;

    @ApiModelProperty(name = "booking_ended", value = "End date of Booking", position = 3, example = "2019-06-04")
    @JsonProperty("booking_ended")
    private LocalDate bookingEndDate;

    @ApiModelProperty(name = "booking_active", value = "Booking Active?", example = "true", position = 4, required = true)
    @JsonProperty("booking_active")
    private boolean bookingActive;

    @ApiModelProperty(name = "location", value = "Location of Offender", position = 5)
    private Location location;

    @JsonIgnore
    private boolean latestBooking;

    @ApiModelProperty(name = "release_date", value = "Release Date", position = 6, example = "2019-02-04")
    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @ApiModelProperty(name = "legal_cases", value = "Legal Cases", position = 7, allowEmptyValue = true)
    @JsonProperty("legal_cases")
    private List<LegalCase> legalCases;

}
