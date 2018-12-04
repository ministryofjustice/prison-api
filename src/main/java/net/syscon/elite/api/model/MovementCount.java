package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Establishment roll count in and out numbers
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Establishment roll count in and out numbers")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovementCount {

    @ApiModelProperty(required = true, value = "List of offender numbers that are out")
    private List<String> offendersOut;

    @ApiModelProperty(required = true, value = "List of offender numbers that are in")
    private List<String> offendersIn;

    @NotNull
    @ApiModelProperty(required = true, value = "Number of prisoners arrived so far on given date")
    @JsonProperty("in")
    public Integer getIn() {
        return offendersIn != null ? offendersIn.size() : 0;
    }

    @NotNull
    @ApiModelProperty(required = true, value = "Number of prisoners that have left so far on given date")
    @JsonProperty("out")
    public Integer getOut() {
        return offendersOut != null ? offendersOut.size() : 0;
    }
}
