package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDate.EPOCH;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Getter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "PendingDeletionRequest")
public class PendingDeletionRequest {

    @Builder.Default
    @DateTimeFormat(iso = DATE)
    @ApiModelProperty(value = "The start of the deletion-due-date window (defaults to epoch)")
    @JsonProperty("dueForDeletionWindowStart")
    private LocalDateTime dueForDeletionWindowStart = EPOCH.atStartOfDay();

    @NotNull
    @DateTimeFormat(iso = DATE)
    @ApiModelProperty(value = "The end of the deletion-due-date window")
    @JsonProperty("dueForDeletionWindowEnd")
    private LocalDateTime dueForDeletionWindowEnd;
}
