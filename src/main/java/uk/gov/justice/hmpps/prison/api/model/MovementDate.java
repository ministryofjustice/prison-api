package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel(description = "A movement IN and OUT range")
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
