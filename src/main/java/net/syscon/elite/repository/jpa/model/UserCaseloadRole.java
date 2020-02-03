package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "USER_CASELOAD_ROLES")
@Data()
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "caseload_id='NWEB'")
public class UserCaseloadRole implements Serializable {

    @EmbeddedId
    private UserCaseloadRoleIdentity id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE_ID", updatable = false, insertable = false)
    private Role role;
}
