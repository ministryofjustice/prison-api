package net.syscon.prison.service;

import lombok.AllArgsConstructor;
import net.syscon.prison.api.model.ImageDetail;
import net.syscon.prison.repository.ImageRepository;
import net.syscon.prison.repository.OffenderRepository;
import net.syscon.prison.repository.jpa.model.OffenderImage;
import net.syscon.prison.repository.jpa.repository.OffenderImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    @Autowired
    private ImageRepository repository;

    @Autowired
    private OffenderImageRepository offenderImageRepository;

    @Autowired
    private OffenderRepository offenderRepository;

    public List<ImageDetail> findOffenderImagesFor(final String offenderNumber) {

        if (offenderRepository.getOffenderIdsFor(offenderNumber).isEmpty()) throw EntityNotFoundException.withId(offenderNumber);

        return offenderImageRepository.getImagesByOffenderNumber(offenderNumber).stream()
                .map(this::convertFrom)
                .collect(toList());
    }

    public ImageDetail findImageDetail(final Long imageId) {
        return repository.findImageDetail(imageId).orElseThrow(EntityNotFoundException.withId(imageId));
    }

    public Optional<byte[]> getImageContent(final Long imageId, final boolean fullSizeImage) {
        return Optional.ofNullable(repository.getImageContent(imageId, fullSizeImage));
    }

    public Optional<byte[]> getImageContent(final String offenderNo, final boolean fullSizeImage) {
        return Optional.ofNullable(repository.getImageContent(offenderNo, fullSizeImage));
    }

    private ImageDetail convertFrom(final OffenderImage image) {
        return ImageDetail.builder()
                .imageId(image.getOffenderImageId())
                .captureDate(image.getCaptureDateTime().toLocalDate())
                .imageView(image.getImageViewType())
                .imageOrientation(image.getOrientationType())
                .imageType(image.getImageObjectType())
                .objectId(image.getImageObjectId())
                .build();
    }
}
