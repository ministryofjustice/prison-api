package uk.gov.justice.hmpps.prison.service.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService;
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class AttendanceTypesValidator implements ConstraintValidator<AttendanceTypesValid, UpdateAttendance> {
    private final ReferenceDomainService referenceDomainService;

    public AttendanceTypesValidator(final ReferenceDomainService referenceDomainService) {
        this.referenceDomainService = referenceDomainService;
    }

    @Override
    public void initialize(final AttendanceTypesValid constraintAnnotation) {
        Assert.notNull(referenceDomainService, "Spring injection failed for referenceDomainService");
    }

    @Override
    public boolean isValid(final UpdateAttendance value, final ConstraintValidatorContext context) {
        var valid = checkDomain(value.getEventOutcome(), context, ReferenceDomain.EVENT_OUTCOME, "Event outcome value %s does not exist");
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

    private boolean checkDomain(final String value, final ConstraintValidatorContext context, final ReferenceDomain domain, final String messageTemplate) {
        // This should be ok as it is cached:
        final var codes = referenceDomainService.getReferenceCodesByDomain(
                domain.getDomain(), false, null, null, 0, 1000).getItems();
        final var code = codes.stream()
                .filter(x -> value.equals(x.getCode()))
                .filter(x -> "Y".equals(x.getActiveFlag()))
                .findFirst();
        if (code.isPresent()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        final var message = String.format(messageTemplate, value);
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
