package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Schema(description = "Live Roll")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LiveRoll {
    @Schema(description = "Noms Ids")
    private List<String> noms_ids;
}
