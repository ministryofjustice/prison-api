package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ImageService {

    private final OffenderImageRepository offenderImageRepository;

    private final OffenderRepository offenderRepository;

    public List<ImageDetail> findOffenderImagesFor(final String offenderNumber) {
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

    @Transactional
    public ImageDetail putImageForOffender(final String offenderNumber, final InputStream receivedImage) {
        // Uses a 4:3 aspect ratio - will distort square photos! Compact cameras and phones use 4:3 for portrait.
        final int fullWidth = 427, fullHeight = 570;
        final int thumbWidth = 150, thumbHeight = 200;

        final var rootOffender = offenderRepository
            .findRootOffenderByNomsId(offenderNumber)
            .orElseThrow(EntityNotFoundException.withMessage(format("No prisoner found for prisoner number %s", offenderNumber)));

        final var booking = rootOffender
            .getLatestBooking()
            .orElseThrow(EntityNotFoundException.withMessage(format("There are no bookings for %s", offenderNumber)));

        // Set the previously active facial image for this bookingId to inactive
        final var previousImage = offenderImageRepository.findLatestByBookingId(booking.getBookingId());
        if (previousImage.isPresent()) {
            final var prev = previousImage.get();
            log.info("Setting previous facial image to active=false - Id {}, bookingId {}, bookingSeq {}, offenderNo {}",
                prev.getId(), booking.getBookingId(), booking.getBookingSequence(), offenderNumber);
            prev.setActive(false);
            offenderImageRepository.save(prev);
        }

        try {
            final var imageToScale = receivedImage.readAllBytes();
            final var fullImage = scaleImage(fullWidth, fullHeight, imageToScale);
            final var thumbImage = scaleImage(thumbWidth, thumbHeight, imageToScale);

            final var newImage = OffenderImage
                .builder()
                .captureDateTime(LocalDateTime.now())
                .orientationType("FRONT")
                .viewType("FACE")
                .imageType("OFF_BKG")
                .active(true)
                .sourceCode("GEN")
                .offenderBooking(booking)
                .thumbnailImage(thumbImage)
                .fullSizeImage(fullImage)
            .build();

            // Suggested method - does add the image but does not flush or return the imageId for the response
            // var savedImage = booking.addImage(newImage);

            final var savedImage = offenderImageRepository.save(newImage);

            log.info("Saved image - Id {}, bookingId {}, bookingSeq {}, offenderNo {}",
                savedImage.getId(), booking.getBookingId(), booking.getBookingSequence(), offenderNumber);

            return savedImage.transform();
        } catch (Exception e) {
            throw BadRequestException.withMessage("Error scaling the image. Must be in JPEG format.");
        }
    }

    private byte[] scaleImage(int width, int height, byte[] source) throws IOException, IllegalArgumentException, InterruptedException {
        final var is = new ByteArrayInputStream(source);
        final var original = ImageIO.read(is);
        final var scaled = original.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        final var outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final var ready = outputImage.getGraphics().drawImage(scaled, 0, 0, new ImageWait());
        if (!ready) {
            // Large images may take slightly longer to scale - not seen any (so far) though
            log.info("Initial image response not ready - waiting 500 ms");
            Thread.sleep(500);
        }
        final var baos = new ByteArrayOutputStream();
        ImageIO.write(outputImage, "jpg", baos);
        return baos.toByteArray();
    }

    private static class ImageWait implements ImageObserver {
        @Override
        public boolean imageUpdate(Image img, int infoFlags, int x, int y, int width, int height) {
            log.info("Image update received a response for image {} x {}", width, height);
            return true;
        }
    }
}
