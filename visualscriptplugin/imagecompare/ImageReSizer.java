package com.crestech.opkey.plugin.visualscriptplugin.imagecompare;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageReSizer {

	private BufferedImage resizeImage(BufferedImage originalImage, int width, int height, int type) {
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		return resizedImage;
	}

	BufferedImage createResizedCopy(String imagePath1, String imagePath2) throws IOException {
		BufferedImage resizeImagePng;
		BufferedImage originalImage = ImageIO.read(new File(imagePath1));
		BufferedImage originalImage2 = ImageIO.read(new File(imagePath2));
		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
		int image1_width = originalImage.getWidth();
		int image1_height = originalImage.getHeight();
		int image2_width = originalImage2.getWidth();
		int image2_height = originalImage2.getHeight();
		if (image1_width >= image2_width || image1_height >= image2_height) {
			resizeImagePng = resizeImage(originalImage, image2_width, image2_height, type);
			ImageIO.write(resizeImagePng, "png", new File(imagePath1));
		} else {
			resizeImagePng = resizeImage(originalImage2, image1_width, image1_height, type);
			ImageIO.write(resizeImagePng, "png", new File(imagePath1));
		}
		return resizeImagePng;

	}
}