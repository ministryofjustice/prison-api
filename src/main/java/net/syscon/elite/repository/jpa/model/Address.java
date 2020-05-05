package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.time.LocalDate;

import static net.syscon.elite.repository.jpa.model.Country.COUNTRY;
import static net.syscon.elite.repository.jpa.model.County.COUNTY;
import static net.syscon.elite.repository.jpa.model.City.CITY;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ADDRESSES")
public class Address {
    @Id
    private Long addressId;

    @Column(name = "OWNER_ID")
    private Long ownerId;

    @Column(name = "OWNER_CLASS")
    private String ownerClass;

    private String flat;

    @Column(name = "ADDRESS_TYPE")
    private String addressType;

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

}
