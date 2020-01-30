package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCaseloadRoleIdentity implements Serializable {

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "username")
    private String username;

    @Column(name = "caseload_id")
    private String caseload;

}
