package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Immutable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Visitor {
    @Id
    private Long personId;
    private Long visitId;
    private String lastName;
    private String firstName;
    private LocalDate birthdate;
    private String leadVisitor;
    private String relationship;
}
