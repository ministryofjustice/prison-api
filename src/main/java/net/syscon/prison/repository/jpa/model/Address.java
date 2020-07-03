package net.syscon.prison.repository.jpa.model;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static net.syscon.prison.repository.jpa.model.AddressType.ADDR_TYPE;
import static net.syscon.prison.repository.jpa.model.City.CITY;
import static net.syscon.prison.repository.jpa.model.Country.COUNTRY;
import static net.syscon.prison.repository.jpa.model.County.COUNTY;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ADDRESSES")
public class Address {
    @Id
    @Column(name = "ADDRESS_ID", nullable = false)
    private Long addressId;

    @Column(name = "OWNER_ID", nullable = false)
    private Long ownerId;

    @Column(name = "OWNER_CLASS", nullable = false)
    private String ownerClass;

    private String flat;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + ADDR_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ADDRESS_TYPE", referencedColumnName = "code"))
    })
    private AddressType addressType;

    private String premise;
    private String street;
    private String locality;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "NO_FIXED_ADDRESS_FLAG")
    private String noFixedAddressFlag;

    @Column(name = "PRIMARY_FLAG")
    private String primaryFlag;

    @Column(name = "MAIL_FLAG")
    private String mailFlag;

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
    @NotFound(action = IGNORE)
    @JoinColumn(name = "ADDRESS_ID")
    @Builder.Default
    private List<AddressUsage> addressUsages = new ArrayList<>();
}
