package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel(description = "Update an existing alert")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UpdateAlert {
    @ApiModelProperty(value = "Date the alert became inactive", example = "2019-02-13", required = true)
    @NotNull
    private LocalDate expiryDate;

    @ApiModelProperty(value = "Alert status", allowableValues="INACTIVE,ACTIVE", example = "ACTIVE", required = true)
    @NotBlank
    private String alertStatus;
}
