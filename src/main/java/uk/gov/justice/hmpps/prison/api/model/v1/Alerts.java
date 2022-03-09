package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Schema(description = "Alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Alerts {
    @Schema(description = "Alerts")
    private List<AlertV1> alerts;
}
