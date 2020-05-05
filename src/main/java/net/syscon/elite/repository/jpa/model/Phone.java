package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PHONES")
public class Phone {
    @Id
    @Column(name = "PHONE_ID")
    private Long phoneId;

    @Column(name = "OWNER_ID")
    private Long ownerId;

    @Column(name = "OWNER_CLASS")
    private String ownerClass;

    @Column(name = "PHONE_TYPE")
    private String phoneType;

    @Column(name = "PHONE_NO")
    private String phoneNo;

    @Column(name = "EXT_NO")
    private String extNo;

}
