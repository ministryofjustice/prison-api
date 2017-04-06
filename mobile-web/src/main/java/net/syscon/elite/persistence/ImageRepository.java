package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.ImageDetails;

public interface ImageRepository {

	ImageDetails findImageDetail(Long imageId);
	byte[] getImageContent(Long imageId);


}

