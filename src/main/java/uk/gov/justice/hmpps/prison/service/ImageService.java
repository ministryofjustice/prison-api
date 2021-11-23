package uk.gov.justice.hmpps.prison.service;

import java.time.LocalDateTime;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
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
            .orElseThrow(EntityNotFoundException.withId(imageId));
    }

    public Optional<byte[]> getImageContent(final Long imageId, final boolean fullSizeImage) {
        return offenderImageRepository.findById(imageId)
        .map(i -> fullSizeImage ? i.getFullSizeImage() : i.getThumbnailImage());
    }

    public Optional<byte[]> getImageContent(final String offenderNo, final boolean fullSizeImage) {
        return offenderImageRepository.findLatestByOffenderNumber(offenderNo)
            .map(i -> fullSizeImage ? i.getFullSizeImage() : i.getThumbnailImage());
    }

    @PreAuthorize("hasRole('SYSTEM_USER')")
    @HasWriteScope
    @Transactional
    public ImageDetail putImageForOffender(final String offenderNumber, final boolean fullSizeImage,  final String imageData) {
        // Check that the offender number exists
        List<Offender> offenderList = offenderRepository.findByNomsId(offenderNumber);
        if (offenderList.isEmpty()) {
            throw EntityNotFoundException.withId(offenderNumber);
        }

        // Check that there is a booking present and disallow when not present. Must there be a booking?
        var latestBooking = offenderList.get(0).getLatestBooking();
        if (latestBooking.isEmpty()) {
            throw EntityNotFoundException.withMessage("There are no bookings for {}", offenderNumber);
        }

        // Uses the sequence OFFENDER_IMAGE_ID to get the next value for the ID
        var newImage = OffenderImage
            .builder()
            .captureDateTime(LocalDateTime.now())
            .orientationType("FRONT")
            .viewType("FACE")
            .imageType("OFF_BKG")
            .active(true)
            .sourceCode("GEN")
            .offenderBooking(latestBooking.get())
            .thumbnailImage(fullSizeImage ? null : Base64.getDecoder().decode(imageData))
            .fullSizeImage(fullSizeImage ? Base64.getDecoder().decode(imageData) : null)
            .build();

        final OffenderImage savedImage = offenderImageRepository.save(newImage);

        log.info("Saved image - Id {}, bookingId {}, offenderNo {}",
            savedImage.getId(), savedImage.getOffenderBooking().getBookingId(),offenderNumber);

        return savedImage.transform();
    }
}
