package net.syscon.elite.web.api.resource.impl;


import net.syscon.elite.persistence.ImageRepository;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.ImageDetail;
import net.syscon.elite.web.api.resource.ImagesResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class ImagesResourceImpl implements ImagesResource {


	private ImageRepository imageRepository;

	@Inject
	public void setImageRepository(final ImageRepository imageRepository) { this.imageRepository = imageRepository; }

	@Override
	public GetImagesByImageIdResponse getImagesByImageId(String imageId) throws Exception {
		final ImageDetail imageDetail = imageRepository.findImageDetail(Long.valueOf(imageId));
		return GetImagesByImageIdResponse.withJsonOK(imageDetail);
	}

	@Override
	public GetImagesByImageIdDataResponse getImagesByImageIdData(String imageId) throws Exception {
		byte data[] = imageRepository.getImageContent(Long.valueOf(imageId));
		if (data != null) {
			StreamingOutput out = new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					output.write(data);
				}
			};
			return GetImagesByImageIdDataResponse.withJpegOK(out);
		} else {
			HttpStatus httpStatus = new HttpStatus("404", "404", "Image not found", "The image could not be found or is null", "");
			return GetImagesByImageIdDataResponse.withJsonNotFound(httpStatus);
		}
	}
}

