package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Schema(description = "Internal Location")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"description", "levels"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternalLocation {

    final static int MAX_LEVELS = 4;

    @Schema(description = "Description", example = "BMI-C-2-03")
    @JsonProperty("description")
    private String description;

    @Schema(description = "Levels")
    @JsonProperty("levels")
    private List<TypeValue> levels;

    public InternalLocation(final String description, final String levelStr) {
        this.description = description;
        if (levelStr != null) {
            this.levels = buildLevels(levelStr);
        }
    }

    private List<TypeValue> buildLevels(String levelStr) {
        return Arrays.stream(StringUtils.split(levelStr, "\\|", MAX_LEVELS))
                .map(level -> {
                    var tv = level.split(",");
                    if (tv.length == 2) {
                        return new TypeValue(tv[0], tv[1]);
                    } else {
                        throw new RuntimeException("Badly formed levelStr:" + levelStr);
                    }
                })
                .toList();
    }

}
