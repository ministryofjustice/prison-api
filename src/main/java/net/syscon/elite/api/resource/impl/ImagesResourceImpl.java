package net.syscon.elite.api.resource.impl;


import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.resource.ImageResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.ImageService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@RestResource
@Path("/images")
public class ImagesResourceImpl implements ImageResource {
    @Autowired
    private ImageService imageService;

    @Override
    public GetImageDataResponse getImageData(final Long imageId, final boolean fullSizeImage) {
        final var data = imageService.getImageContent(imageId, fullSizeImage);
        if (data != null) {
            try {
                final var temp = File.createTempFile("userimage", ".tmp");
                FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data), temp);
                return GetImageDataResponse.respond200WithApplicationJson(temp);
            } catch (final IOException e) {
                final var errorResponse = ErrorResponse.builder()
                        .errorCode(500)
                        .userMessage("An error occurred loading the image ID " + imageId)
                        .build();
                return GetImageDataResponse.respond500WithApplicationJson(errorResponse);
            }
        } else {
            final var errorResponse = ErrorResponse.builder()
                    .errorCode(404)
                    .userMessage("No image was found with ID " + imageId)
                    .build();
            return GetImageDataResponse.respond404WithApplicationJson(errorResponse);
        }
    }

    @Override
    public GetImageResponse getImage(final Long imageId) {
        final var imageDetail = imageService.findImageDetail(imageId);
        return GetImageResponse.respond200WithApplicationJson(imageDetail);
    }
}
