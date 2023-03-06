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

@Schema(description = "Represents the data required for registering court return")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestForCourtTransferIn {

    @Schema(required = true, description = "Agency identifier", example = "MDI")
    @Length(max = 20, min = 2, message = "Agency identifier cannot be less then 2 and more than 20 characters")
    @NotNull
    private String agencyId;

    @Schema(description = "Movement Reason Code", example = "CA")
    @Length(max = 20, min = 1, message = "Movement reason code cannot be less then 2 and more than 20 characters")
    private String movementReasonCode;

    @Schema(description = "Additional comments", example = "Prisoner was transferred to a new prison")
    @Length(max = 240, message = "comment text size is a maximum of 240 characters")
    private String commentText;

    @Schema(required = true, description = "The date and time the movement occurred, if not supplied it will be the current time. Note: Time can be in the past but not before the last movement", example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateTime;

}
