package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(description = "Active Offender")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"found", "offender"})
public class ActiveOffender {

    @ApiModelProperty(value = "found", name = "found", example = "true")
    private boolean found;

    @ApiModelProperty(value = "offender", name = "offender")
    @JsonInclude(Include.NON_NULL)
    private OffenderId offender;

    public ActiveOffender(BigDecimal id) {
        this.found = id != null;
        this.offender = found ? new OffenderId(id.longValue()) : null;
    }
}
