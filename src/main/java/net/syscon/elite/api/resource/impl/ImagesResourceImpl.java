package net.syscon.elite.api.resource.impl;


import lombok.AllArgsConstructor;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.api.resource.ImageResource;
import net.syscon.elite.service.ImageService;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/images")
@AllArgsConstructor
public class ImagesResourceImpl implements ImageResource {

    private final ImageService imageService;

    @Override
    public ResponseEntity<?> getImageData(final Long imageId, final boolean fullSizeImage) {
        final var data = imageService.getImageContent(imageId, fullSizeImage);
        if (data != null) {
            try {
                final var temp = File.createTempFile("userimage", ".tmp");
                FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data), temp);
                return ResponseEntity.ok()
                        .body(temp);
            } catch (final IOException e) {
                final var errorResponse = ErrorResponse.builder()
                        .errorCode(500)
                        .userMessage("An error occurred loading the image ID " + imageId)
                        .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorResponse);
            }
        } else {
            final var errorResponse = ErrorResponse.builder()
                    .errorCode(404)
                    .userMessage("No image was found with ID " + imageId)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse);
        }
    }

    @Override
    public ImageDetail getImage(final Long imageId) {
        return imageService.findImageDetail(imageId);
    }
}
