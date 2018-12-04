package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Establishment roll count in and out numbers
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Establishment roll count in and out numbers")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class MovementCount {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    @ApiModelProperty(required = true, value = "Number of prisoners arrived so far on given date")
    @JsonProperty("in")
    private Integer in;

    @NotNull
    @ApiModelProperty(required = true, value = "Number of prisoners that have left so far on given date")
    @JsonProperty("out")
    private Integer out;

    @ApiModelProperty(required = true, value = "List of offender numbers that are out")
    @JsonProperty("offendersOut")
    private List<String> offendersOut;

    @ApiModelProperty(required = true, value = "List of offender numbers that are in")
    @JsonProperty("offendersIn")
    private List<String> offendersIn;

}
