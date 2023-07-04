package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Represents the data required receiving a prisoner under a new booking")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestForNewBooking {

    @Schema(description = "Received Prison ID", example = "MDI")
    @Length(max = 3, message = "Prison ID is 3 character code")
    @NotNull
    private String prisonId;

    @Schema(requiredMode = REQUIRED, description = "The time the booking in occurred, if not supplied it will be the current time", example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime bookingInTime;

    @Schema(description = "Where the prisoner has moved from (default OUT)", example = "SHEFCC")
    @Length(max = 6, message = "From location")
    private String fromLocationId;

    @Schema(description = "Reason for in movement (e.g. Unconvicted Remand)", example = "N")
    @NotNull
    private String movementReasonCode;

    @Schema(description = "Is this offender a youth", example = "false")
    private boolean youthOffender;

    @Schema(description = "Cell location where recalled prisoner should be housed, default will be reception", example = "MDI-RECP")
    @Length(max = 240, message = "Cell Location description cannot be more than 240 characters")
    private String cellLocation;

    @Schema(description = "Require imprisonment status (e.g Adult Imprisonment Without Option CJA03)", example = "SENT03")
    @Length(max = 12, message = "Imprisonment status cannot be more than 12 characters")
    private String imprisonmentStatus;


}
