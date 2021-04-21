package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.AddressType.ADDR_TYPE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.City.CITY;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.County.COUNTY;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ADDRESSES")
@DiscriminatorColumn(name = "OWNER_CLASS")
@Inheritance
@EqualsAndHashCode(of = "addressId", callSuper = false)
@ToString(of = {"addressId", "addressType", "flat", "premise", "postalCode"})
public abstract class Address extends AuditableEntity {
    @Id
    @SequenceGenerator(name = "ADDRESS_ID", sequenceName = "ADDRESS_ID", allocationSize = 1)
    @GeneratedValue(generator = "ADDRESS_ID")
    @Column(name = "ADDRESS_ID", nullable = false)
    private Long addressId;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + ADDR_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ADDRESS_TYPE", referencedColumnName = "code"))
    })
    private AddressType addressType;

    private String flat;
    private String premise;
    private String street;
    private String locality;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "NO_FIXED_ADDRESS_FLAG")
    private String noFixedAddressFlag;

    @Column(name = "PRIMARY_FLAG", nullable = false)
    @Default
    private String primaryFlag = "N";

    @Column(name = "MAIL_FLAG", nullable = false)
    @Default
    private String mailFlag = "N";

    @Column(name = "COMMENT_TEXT")
    private String commentText;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + COUNTY + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "COUNTY_CODE", referencedColumnName = "code"))
    })
    private County county;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CITY + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "CITY_CODE", referencedColumnName = "code"))
    })
    private City city;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + COUNTRY + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "COUNTRY_CODE", referencedColumnName = "code"))
    })
    private Country country;

    @OneToMany
    @JoinColumn(name = "ADDRESS_ID")
    @Default
    private List<AddressUsage> addressUsages = new ArrayList<>();

    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Where(clause = "OWNER_CLASS = '"+AddressPhone.PHONE_TYPE+"'")
    @Default
    private List<AddressPhone> phones = new ArrayList<>();

    public void removePhone(final AddressPhone phone) {
        phones.remove(phone);
    }

    public AddressPhone addPhone(final AddressPhone phone) {
        phone.setAddress(this);
        phones.add(phone);
        return phone;
    }
}
