package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Expire an alert")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ExpireAlert {
    @ApiModelProperty(value = "Date the alert became inactive", example = "2019-02-13", required = true)
    @NotNull
    private LocalDate expiryDate;

    @JsonIgnore
    public String getAlertStatus() {
        if (expiryDate == null)
            throw new IllegalArgumentException("Expiry date is null");

        return expiryDate.compareTo(LocalDate.now()) <= 0  ? "INACTIVE" : "ACTIVE";
    }
}
