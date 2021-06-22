package uk.gov.justice.hmpps.prison.service.transformers;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.Education;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EducationLevel;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StudyArea;
import uk.gov.justice.hmpps.prison.service.AddressTransformer;

import static uk.gov.justice.hmpps.prison.util.OptionalUtil.getOrNull;

@Component
public class OffenderEducationTransformer implements Converter<OffenderEducation, Education> {
    @Override
    public Education convert(@NotNull final OffenderEducation offenderEducation) {
        return Education
            .builder()
            .bookingId(offenderEducation.getId().getBookingId())
            .startDate(offenderEducation.getStartDate())
            .endDate(offenderEducation.getEndDate())
            .studyArea(getOrNull(offenderEducation.getStudyArea(), StudyArea::getDescription))
            .educationLevel(getOrNull(offenderEducation.getEducationLevel(), EducationLevel::getDescription))
            .numberOfYears(offenderEducation.getNumberOfYears())
            .graduationYear(offenderEducation.getGraduationYear())
            .comment(offenderEducation.getComment())
            .school(offenderEducation.getSchool())
            .isSpecialEducation(offenderEducation.getIsSpecialEducation())
            .addresses(AddressTransformer.translate(offenderEducation.getAddresses()))
            .build();
    }

}
