package com.crestech.opkey.plugin.visualscriptplugin.library;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.sikuli.script.TextRecognizer;

public class OCRv2 {
	@SuppressWarnings("deprecation")
	public String performOCROnImage(BufferedImage bufferedImage) throws IOException {
		// TextRecognizer.start().setPSM(3);
		String ocrImagePath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString()
				+ ".png";
		ImageIO.write(bufferedImage, "png", new File(ocrImagePath));
		System.out.println("ocrImagePath  " + ocrImagePath);

		BufferedImage imageToOcr = ImageIO.read(new File(ocrImagePath));
		TextRecognizer tr = TextRecognizer.start().setPSM(3);
//		Tesseract instance = new Tesseract();
//		instance.setLanguage("eng");
//		instance.setOcrEngineMode(1);
//		try {
//			String imgText = instance.doOCR(new File(ocrImagePath));
//			return imgText;
//		} catch (TesseractException e) {
//			e.getMessage();
//			return "Error while reading image";
//		}
		return tr.doOCR(imageToOcr);
	}

}
