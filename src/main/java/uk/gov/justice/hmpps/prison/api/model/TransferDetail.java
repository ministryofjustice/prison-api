package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "A movement that is a transfer")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class TransferDetail {
    @Schema(description = "Date prisoner left the original prison")
    private LocalDateTime dateOutOfPrison;
    @Schema(description = "Date prisoner entered the new prison. Can be absent if they have not arrived at the prison yet", requiredMode = NOT_REQUIRED)
    private LocalDateTime dateInToPrison;
    @Schema(description = "Reason for the transfer", example = "Compassionate Transfer", requiredMode = NOT_REQUIRED)
    private String transferReason;
    @Schema(description = "The prison they were transferred from", example = "WWI")
    private String fromPrisonId;
    @Schema(description = "The prison they were transferred to. Can be absent if they have not arrived at the prison yet", requiredMode = REQUIRED, example = "BXI")
    private String toPrisonId;
}
