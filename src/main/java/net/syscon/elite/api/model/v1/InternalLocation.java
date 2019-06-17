package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Internal Location")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"description", "levels"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternalLocation {

    final static int MAX_LEVELS = 4;

    @ApiModelProperty(value = "Description", position = 0, example = "BMI-C-2-03")
    @JsonProperty("description")
    private String description;

    @ApiModelProperty(value = "Levels", position = 1)
    @JsonProperty("levels")
    private List<TypeValue> levels;

    public InternalLocation(String description, String levelStr) {
        this.description = description;
        if (levelStr != null) {
            this.levels = new ArrayList<>();
            for (String level : levelStr.split("\\|", MAX_LEVELS)) {
                String[] tv = level.split(",");
                if (tv.length == 2) {
                    this.levels.add(new TypeValue(tv[0], tv[1]));
                } else {
                    throw new RuntimeException("Badly formed levelStr:" + levelStr);
                }
            }
        }
    }


}
