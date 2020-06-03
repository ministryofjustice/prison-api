package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ADDRESS_USAGES")
@IdClass(AddressUsage.PK.class)
public class AddressUsage extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        @Column(name = "ADDRESS_ID")
        private Long id;
        private String addressUsage;
    }

    @Id
    @Column(name = "ADDRESS_ID")
    private Long id;

    @Id
    private String addressUsage;

    private String activeFlag;
}
