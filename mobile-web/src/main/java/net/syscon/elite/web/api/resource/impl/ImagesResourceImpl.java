package net.syscon.elite.web.api.resource.impl;


import net.syscon.elite.service.ImageService;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.ImageDetails;
import net.syscon.elite.web.api.resource.ImagesResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

@Component
public class ImagesResourceImpl implements ImagesResource {

	private final ImageService imageService;

	@Inject
	public ImagesResourceImpl(ImageService imageService) {
		this.imageService = imageService;
	}

	@Override
	public GetImagesByImageIdResponse getImagesByImageId(final String imageId) throws Exception {
		final ImageDetails imageDetail = imageService.findImageDetail(Long.valueOf(imageId));
		return GetImagesByImageIdResponse.withJsonOK(imageDetail);
	}

	@Override
	@SuppressWarnings("squid:S1166")
	public GetImagesByImageIdDataResponse getImagesByImageIdData(final String imageId) throws Exception {
		final byte[] data = imageService.getImageContent(Long.valueOf(imageId));
		if (data != null) {
			final StreamingOutput out = output -> output.write(data);
			return GetImagesByImageIdDataResponse.withJpegOK(out);
		} else {
			final HttpStatus httpStatus = new HttpStatus("404", "404", "Image not found", "The image could not be found or is null", "");
			return GetImagesByImageIdDataResponse.withJsonNotFound(httpStatus);
		}
	}
}

