package net.syscon.elite.web.api.resource.impl;


import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

import org.springframework.stereotype.Component;

import net.syscon.elite.persistence.ImageRepository;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.ImageDetail;
import net.syscon.elite.web.api.resource.ImagesResource;

@Component
public class ImagesResourceImpl implements ImagesResource {


	private ImageRepository imageRepository;

	@Inject
	public void setImageRepository(final ImageRepository imageRepository) { this.imageRepository = imageRepository; }

	@Override
	public GetImagesByImageIdResponse getImagesByImageId(final String imageId) throws Exception {
		final ImageDetail imageDetail = imageRepository.findImageDetail(Long.valueOf(imageId));
		return GetImagesByImageIdResponse.withJsonOK(imageDetail);
	}

	@Override
	@SuppressWarnings("squid:S1166")
	public GetImagesByImageIdDataResponse getImagesByImageIdData(final String imageId) throws Exception {
		final byte data[] = imageRepository.getImageContent(Long.valueOf(imageId));
		if (data != null) {
			final StreamingOutput out = output -> output.write(data);
			return GetImagesByImageIdDataResponse.withJpegOK(out);
		} else {
			final HttpStatus httpStatus = new HttpStatus("404", "404", "Image not found", "The image could not be found or is null", "");
			return GetImagesByImageIdDataResponse.withJsonNotFound(httpStatus);
		}
	}
}

