package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@ApiModel(description = "Code Description")
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CodeDescription {

    @ApiModelProperty(value = "Code", position = 1)
    @JsonProperty("code")
    private String code;

    @ApiModelProperty(value = "Description", position = 2)
    @JsonProperty("desc")
    private String desc;

    public static CodeDescription safeNullBuild(final String code, final String desc) {
        if (StringUtils.isNotBlank(code)) {
            return new CodeDescription(code, desc);
        }
        return null;
    }

}
