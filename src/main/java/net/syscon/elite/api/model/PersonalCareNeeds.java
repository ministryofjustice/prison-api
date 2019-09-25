package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@ApiModel(description = "Personal Care Needs")
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PersonalCareNeeds {

    @ApiModelProperty(value = "Personal Care Needs")
    List<PersonalCareNeed> personalCareNeeds;
}
