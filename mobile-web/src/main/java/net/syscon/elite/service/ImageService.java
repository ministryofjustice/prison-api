package net.syscon.elite.service;


import net.syscon.elite.web.api.model.ImageDetails;

public interface ImageService {

	ImageDetails findImageDetail(Long imageId);
	byte[] getImageContent(Long imageId);


}

