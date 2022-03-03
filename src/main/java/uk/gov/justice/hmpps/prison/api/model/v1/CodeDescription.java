package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@ApiModel(description = "Code Description")
@Getter
@ToString
@EqualsAndHashCode
public class CodeDescription {

    @ApiModelProperty(value = "Code", position = 1)
    private final String code;

    @ApiModelProperty(value = "Description", position = 2)
    private final String desc;

    private CodeDescription(final String code, final String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CodeDescription safeNullBuild(final String code, final String desc) {
        if (StringUtils.isNotBlank(code)) {
            return new CodeDescription(code, desc);
        }
        return null;
    }

}
