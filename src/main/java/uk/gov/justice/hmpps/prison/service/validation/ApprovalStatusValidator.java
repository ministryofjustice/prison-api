package uk.gov.justice.hmpps.prison.service.validation;

import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ApprovalStatusValidator implements ConstraintValidator<ValidApprovalStatus, ApprovalStatus> {
    @Override
    public void initialize(ValidApprovalStatus constraintAnnotation) {

    }

    @Override
    public boolean isValid(ApprovalStatus approvalStatus, ConstraintValidatorContext context) {
        return approvalStatus.isApproved() && !approvalStatus.hasRefusedReason() ||
                !approvalStatus.isApproved() && approvalStatus.hasRefusedReason();
    }
}
