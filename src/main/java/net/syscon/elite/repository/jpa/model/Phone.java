package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PHONES")
public class Phone {
    @Id
    @Column(name = "PHONE_ID", nullable = false)
    private Long phoneId;

    @Column(name = "OWNER_ID", nullable = false)
    private Long ownerId;

    @Column(name = "OWNER_CLASS", nullable = false)
    private String ownerClass;

    @Column(name = "PHONE_TYPE")
    private String phoneType;

    @Column(name = "PHONE_NO")
    private String phoneNo;

    @Column(name = "EXT_NO")
    private String extNo;

}
