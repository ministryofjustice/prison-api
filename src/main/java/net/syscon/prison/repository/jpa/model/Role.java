package net.syscon.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "OMS_ROLES")
@Data()
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {

    @Id()
    @Column(name = "ROLE_ID", nullable = false)
    private Long id;

    @Column(name = "ROLE_CODE", nullable = false, unique = true)
    private String code;

}
