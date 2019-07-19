package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@ApiModel(description = "Reference Code Data")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
@JsonPropertyOrder({"description", "parentDomain", "parentCode", "activeFlag", "listSeq", "systemDataFlag", "expiredDate"})
@Data
@ToString
public class ReferenceCodeInfo {

    @ApiModelProperty(required = true, value = "Reference data item description.", position = 1, example = "Some description")
    @NotBlank
    private String description;

    @ApiModelProperty(value = "Parent reference data item domain.", position = 2, example = "TASK_TYPE")
    @Length(max = 1)
    private String parentDomain;

    @ApiModelProperty(value = "Parent reference data item code.", position = 3, example = "MIGRATION")
    @Length(max = 1)
    private String parentCode;

    @ApiModelProperty(required = true, value = "Reference data item active indicator flag.", example = "Y", allowableValues = "Y,N", position = 4)
    @NotBlank
    @Length(max = 1)
    @Pattern(regexp = "[N|Y]")
    private String activeFlag;

    @ApiModelProperty(value = "List Sequence", example = "1", position = 5)
    @NotBlank
    private Integer listSeq;

    @ApiModelProperty(value = "System Data Flag", position = 6, example = "N")
    @Length(max = 1)
    @Pattern(regexp = "[N|Y]")
    private String systemDataFlag;

    @ApiModelProperty(value = "Expired Date", position = 7, example = "2018-03-09")
    private LocalDate expiredDate;

}
