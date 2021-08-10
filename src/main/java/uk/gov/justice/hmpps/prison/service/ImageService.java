package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final OffenderImageRepository offenderImageRepository;

    private final OffenderRepository offenderRepository;

    public List<ImageDetail> findOffenderImagesFor(final String offenderNumber) {

        if (offenderRepository.findByNomsId(offenderNumber).isEmpty()) throw EntityNotFoundException.withId(offenderNumber);

        return offenderImageRepository.getImagesByOffenderNumber(offenderNumber).stream()
                .map(OffenderImage::transform)
                .collect(toList());
    }

    public ImageDetail findImageDetail(final Long imageId) {
        return offenderImageRepository.findById(imageId)
            .map(OffenderImage::transform)
            .orElse(null);
    }

    public Optional<byte[]> getImageContent(final Long imageId, final boolean fullSizeImage) {
        return offenderImageRepository.findById(imageId)
        .map(i -> fullSizeImage ? i.getFullSizeImage() : i.getThumbnailImage());
    }

    public Optional<byte[]> getImageContent(final String offenderNo, final boolean fullSizeImage) {
        return offenderImageRepository.findLatestByOffenderNumber(offenderNo)
            .map(i -> fullSizeImage ? i.getFullSizeImage() : i.getThumbnailImage());
    }


}
