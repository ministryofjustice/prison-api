package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
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
@Builder(toBuilder = true)
public class ReferenceCodeInfo {

    @ApiModelProperty(required = true, value = "Reference data item description.", position = 1, example = "Some description")
    @NotBlank
    @Length(max = 40)
    private String description;

    @ApiModelProperty(value = "Parent reference data item domain.", position = 2, example = "TASK_TYPE")
    @Length(max = 12)
    private String parentDomain;

    @ApiModelProperty(value = "Parent reference data item code.", position = 3, example = "MIGRATION")
    @Length(max = 12)
    private String parentCode;

    @ApiModelProperty(required = true, value = "Reference data item active indicator flag.", example = "Y", allowableValues = "Y,N", position = 4)
    @Length(max = 1)
    @Pattern(regexp = "[N|Y]")
    @Builder.Default
    private String activeFlag = "Y";

    @ApiModelProperty(value = "List Sequence", example = "1", position = 5)
    @Max(value = 999999)
    private Integer listSeq;

    @ApiModelProperty(value = "System Data Flag", position = 6, example = "Y", allowableValues = "Y,N")
    @Length(max = 1)
    @Pattern(regexp = "[N|Y]")
    @Builder.Default
    private String systemDataFlag = "Y";

    @ApiModelProperty(value = "Expired Date", position = 7, example = "2018-03-09")

    private LocalDate expiredDate;

}
