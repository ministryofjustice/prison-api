package uk.gov.justice.hmpps.prison.repository.jpa.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PROGRAM_SERVICES")
public class ProgramService {

    @Id
    @Column(name = "PROGRAM_ID")
    private Long programId;

    @Column(name = "DESCRIPTION")
    @ApiModelProperty(value = "Activity name")
    private String activity;

    @Column(name = "PROGRAM_CATEGORY")
    @ApiModelProperty(value = "Program category")
    private String programCategory;

    @Column(name = "PROGRAM_CODE")
    @ApiModelProperty(value = "Program code")
    private String programCode;

    @Column(name = "ACTIVE_FLAG")
    @ApiModelProperty(value = "Active flag")
    private String activeFlag = "Y";
}
