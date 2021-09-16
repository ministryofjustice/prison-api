package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STAFF_ID", nullable = false)
    @Exclude
    private Staff staff;

    @Column(name = "STAFF_USER_TYPE", nullable = false)
    private String type;

    @Column(name = "WORKING_CASELOAD_ID")
    private String activeCaseLoadId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "USERNAME")
    @Exclude
    @Default
    private List<UserCaseloadRole> roles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "USERNAME")
    @Exclude
    @Default
    private List<UserCaseload> caseloads = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AccountDetail accountDetail;

    public List<UserCaseloadRole> getDpsRoles() {
        return getRoles().stream().filter(r -> "NWEB".equals(r.getId().getCaseload())).collect(Collectors.toList());
    }

    public Optional<UserCaseloadRole> findByCaseloadAndRoleCode(final String caseload, final String roleCode) {
        return getRoles().stream()
            .filter(r -> r.getId().getCaseload().equals(caseload))
            .filter(r -> r.getRole().getCode().equals(roleCode))
            .findFirst();
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
