package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "STAFF_USER_ACCOUNTS")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@BatchSize(size = 25)
public class StaffUserAccount extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAFF_ID")
    @Exclude
    private Staff staff;

    @Column(name = "STAFF_USER_TYPE", nullable = false)
    private String type;

    @Column(name = "WORKING_CASELOAD_ID")
    private String activeCaseLoadId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERNAME")
    @Exclude
    private List<UserCaseloadRole> roles;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "USERNAME")
    @Exclude
    private List<UserCaseload> caseloads;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AccountDetail accountDetail;

    public List<UserCaseloadRole> getDpsRoles() {
        return getRoles().stream().filter(r -> "NWEB".equals(r.getId().getCaseload())).collect(Collectors.toList());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final StaffUserAccount that = (StaffUserAccount) o;

        return Objects.equals(getUsername(), that.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUsername());
    }
}
