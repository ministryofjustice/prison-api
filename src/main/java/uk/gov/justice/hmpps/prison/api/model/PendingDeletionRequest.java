package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static java.time.LocalDate.EPOCH;

@Getter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "PendingDeletionRequest")
public class PendingDeletionRequest {

    @NotNull
    @Schema(description = "An ID uniquely identifying the deletion request batch", example = "123", required = true)
    @JsonProperty("batchId")
    private Long batchId;

    @Builder.Default
    @Schema(description = "The start of the deletion-due-date window in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS (defaults to epoch)", example = "2020-02-28T14:40:00")
    @JsonProperty("dueForDeletionWindowStart")
    private LocalDateTime dueForDeletionWindowStart = EPOCH.atStartOfDay();

    @NotNull
    @Schema(description = "The end of the deletion-due-date window in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS", example = "2020-02-28T14:40:00", required = true)
    @JsonProperty("dueForDeletionWindowEnd")
    private LocalDateTime dueForDeletionWindowEnd;

    @Schema(description = "The limit of the number of offenders retrieved and referred for deletion", example = "10")
    @JsonProperty("limit")
    private Integer limit;
}
