package net.syscon.elite.service.validation;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.UpdateAttendance;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.support.ReferenceDomain;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
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
        boolean valid = checkDomain(value.getEventOutcome(), context, ReferenceDomain.EVENT_OUTCOME, "Event outcome value %s does not exist");
        if (value.getPerformance() == null) {
            if (value.getEventOutcome().equals("ATT")) {
                valid = false;
                context.buildConstraintViolationWithTemplate("Performance value must be provided when event outcome is 'ATT'").addConstraintViolation();
            }
        } else {
            valid &= checkDomain(value.getPerformance(), context, ReferenceDomain.PERFORMANCE, "Performance value %s does not exist");
        }
        return valid;
    }

    private boolean checkDomain(String value, ConstraintValidatorContext context, ReferenceDomain domain, String messageTemplate) {
        // This should be ok as it is cached:
        final List<ReferenceCode> codes = referenceDomainService.getReferenceCodesByDomain(
                domain.getDomain(), false, null, null, 0, 1000).getItems();
        final Optional<ReferenceCode> code = codes.stream()
                .filter(x -> value.equals(x.getCode()))
                .filter(x -> "Y".equals(x.getActiveFlag()))
                .findFirst();
        if (code.isPresent()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        final String message = String.format(messageTemplate, value);
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
