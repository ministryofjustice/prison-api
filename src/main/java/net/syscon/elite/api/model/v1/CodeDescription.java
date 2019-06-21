package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@ApiModel(description = "Code Description")
@Data
@ToString
@EqualsAndHashCode
@Builder
public class CodeDescription {

    @ApiModelProperty(value = "Code")
    private final String code;

    @ApiModelProperty(value = "Description", position = 1)
    private final String desc;

    public static CodeDescription safeNullBuild(final String code, final String desc) {
        if (StringUtils.isNotBlank(code)) {
            return CodeDescription.builder().code(code).desc(desc).build();
        }
        return null;
    }

    @JsonCreator
    public CodeDescription(@JsonProperty("code") String code, @JsonProperty("desc") String desc) {
        super();
        this.code = code;
        this.desc = desc;
    }
}
