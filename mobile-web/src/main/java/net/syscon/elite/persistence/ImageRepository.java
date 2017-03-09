package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.ImageDetail;

public interface ImageRepository {

	ImageDetail findImageDetail(Long imageId);
	byte[] getImageContent(Long imageId);


}

