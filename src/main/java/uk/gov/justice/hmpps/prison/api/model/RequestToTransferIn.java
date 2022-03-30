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

import java.time.LocalDateTime;

@Schema(description = "Represents the data required for receiving a prisoner transfer")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestToTransferIn {

    @Schema(required = true, description = "The time the movement occurred, if not supplied it will be the current time. Note: Time can be in the past but not before the last movement", example = "2020-03-24T12:13:40")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime receiveTime;

    @Schema(description = "Additional comments about the release", example = "Prisoner was transferred to a new prison")
    @Length(max = 240, message = "Comments size is a maximum of 240 characters")
    private String commentText;

    @Schema(description = "Cell location", example = "MDI-RECP")
    @Length(max = 240, message = "Cell Location description cannot be more than 240 characters")
    private String cellLocation;


}
