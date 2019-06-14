package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel(description = "Code Description")
@Data
@ToString
@EqualsAndHashCode
@Builder
public class CodeDescription {

    @ApiModelProperty(value = "Code", position = 0)
    private final String code;

    @ApiModelProperty(value = "Description", position = 1)
    private String desc;

    @JsonCreator
    public CodeDescription(@JsonProperty("code") String code, @JsonProperty("desc") String desc) {
        super();
        this.code = code;
        this.desc = desc;
    }

    public CodeDescription(String code) {
        super();
        this.code = code;
    }

}
