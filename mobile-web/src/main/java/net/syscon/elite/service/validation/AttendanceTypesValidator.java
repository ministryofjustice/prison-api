package net.syscon.elite.service.validation;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.UpdateAttendance;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.support.ReferenceDomain;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@Component
public class AttendanceTypesValidator implements ConstraintValidator<AttendanceTypesValid, UpdateAttendance> {
    private final ReferenceDomainService referenceDomainService;

    public AttendanceTypesValidator(ReferenceDomainService referenceDomainService) {
        this.referenceDomainService = referenceDomainService;
    }

    @Override
    public void initialize(AttendanceTypesValid constraintAnnotation) {
        Assert.notNull(referenceDomainService, "Spring injection failed for referenceDomainService");
    }

    @Override
    public boolean isValid(UpdateAttendance value, ConstraintValidatorContext context) {
        boolean valid = true;
        // This should be ok as it is cached:
        final Page<ReferenceCode> outcomes = referenceDomainService.getReferenceCodesByDomain(ReferenceDomain.EVENT_OUTCOME.getDomain(), false, null, null, 0, 1000);
        final Optional<ReferenceCode> outcome = outcomes.getItems().stream().filter(x -> x.getCode().equals(value.getEventOutcome())).findFirst();
        if (!outcome.isPresent()) {
            valid = false;
            context.disableDefaultConstraintViolation();
            final String message = "Event outcome value " + value.getEventOutcome() + " does not exist";
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }

        final Page<ReferenceCode> performances = referenceDomainService.getReferenceCodesByDomain(ReferenceDomain.PERFORMANCE.getDomain(), false, null, null, 0, 1000);
        final Optional<ReferenceCode> performance = performances.getItems().stream().filter(x -> x.getCode().equals(value.getPerformance())).findFirst();
        if (!performance.isPresent()) {
            valid = false;
            context.disableDefaultConstraintViolation();
            final String message = "Performance value " + value.getPerformance() + " does not exist";
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
        return valid;
    }
}
