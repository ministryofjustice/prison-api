package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Schema(description = "Code Description")
@Getter
@ToString
@EqualsAndHashCode
public class CodeDescription {

    @Schema(description = "Code")
    private final String code;

    @Schema(description = "Description")
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
