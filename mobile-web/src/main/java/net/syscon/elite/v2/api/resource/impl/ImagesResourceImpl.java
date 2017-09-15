package net.syscon.elite.v2.api.resource.impl;


import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.ImageService;
import net.syscon.elite.v2.api.model.ErrorResponse;
import net.syscon.elite.v2.api.model.ImageDetail;
import net.syscon.elite.v2.api.resource.ImageResource;
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
	public GetImageDataResponse getImageData(final String imageId) {
		final byte[] data = imageService.getImageContent(Long.valueOf(imageId));
		if (data != null) {
			try {
                File temp = File.createTempFile("userimage", ".tmp");
                FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data), temp);
                return GetImageDataResponse.respond200WithApplicationJson(temp);
			} catch (IOException e) {
                final ErrorResponse errorResponse = ErrorResponse.builder()
                        .errorCode(500)
                        .userMessage("An error occurred loading the image ID "+ imageId)
                        .build();
                return GetImageDataResponse.respond500WithApplicationJson(errorResponse);
			}
		} else {
            final ErrorResponse errorResponse = ErrorResponse.builder()
                    .errorCode(404)
                    .userMessage("No image was found with ID "+ imageId)
                    .build();
            return GetImageDataResponse.respond404WithApplicationJson(errorResponse);
		}
	}

	@Override
	public GetImageResponse getImage(String imageId) {
		final ImageDetail imageDetail = imageService.findImageDetail(Long.valueOf(imageId));
		return GetImageResponse.respond200WithApplicationJson(imageDetail);

	}
}
