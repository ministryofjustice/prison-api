package net.syscon.elite.service.impl;

import lombok.AllArgsConstructor;
import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.repository.ImageRepository;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.repository.jpa.model.OffenderImage;
import net.syscon.elite.repository.jpa.repository.OffenderImageRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository repository;

    @Autowired
    private OffenderImageRepository offenderImageRepository;

    @Autowired
    private OffenderRepository offenderRepository;

    @Override
    public List<ImageDetail> findOffenderImagesFor(final String offenderNumber) {

        if (offenderRepository.getOffenderIdsFor(offenderNumber).isEmpty()) throw EntityNotFoundException.withId(offenderNumber);

        return offenderImageRepository.getImagesByOffenderNumber(offenderNumber).stream()
                .map(this::convertFrom)
                .collect(toList());
    }

    @Override
    public ImageDetail findImageDetail(final Long imageId) {
        return repository.findImageDetail(imageId).orElseThrow(EntityNotFoundException.withId(imageId));
    }

    @Override
    public Optional<byte[]> getImageContent(final Long imageId, final boolean fullSizeImage) {
        return Optional.ofNullable(repository.getImageContent(imageId, fullSizeImage));
    }

    @Override
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
