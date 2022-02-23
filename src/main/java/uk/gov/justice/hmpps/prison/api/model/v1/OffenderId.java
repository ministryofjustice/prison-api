package uk.gov.justice.hmpps.prison.api.model.v1;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "offender ID")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OffenderId {

    @Schema(description = "ID", name = "id", example = "1234567")
    private Long id;
}
