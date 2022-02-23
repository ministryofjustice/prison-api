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

@Schema(description = "A movement IN and OUT range")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class MovementDate {
    private String reasonInToPrison;
    private LocalDateTime dateInToPrison;
    private String inwardType;
    private String reasonOutOfPrison;
    private LocalDateTime dateOutOfPrison;
    private String outwardType;

}
