package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Schema(description = "Active Offender")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"found", "offender"})
public class ActiveOffender {

    @Schema(description = "found", name = "found", example = "true")
    private boolean found;

    @Schema(description = "offender", name = "offender")
    @JsonInclude(Include.NON_NULL)
    private OffenderId offender;

    public ActiveOffender(BigDecimal id) {
        this.found = id != null;
        this.offender = found ? new OffenderId(id.longValue()) : null;
    }
}
