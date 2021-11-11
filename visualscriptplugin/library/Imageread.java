package com.crestech.opkey.plugin.visualscriptplugin.library;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.sikuli.script.FindFailed;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.contexts.Context;
import com.crestech.opkey.plugin.visualscriptplugin.Util;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Imageread implements KeywordLibrary {

	public FunctionResult Custom_MethodReadTextFromImage(int x, int y, int width, int height)
			throws FindFailed, IOException {
		BufferedImage bufferedImage = (Util.getScreen().capture(x, y, width, height).getImage());

		System.out.println("Path::" + Context.session().getDefaultPluginLocation());
		Tesseract instance = new Tesseract();
		instance.setDatapath(Context.session().getDefaultPluginLocation());
		instance.setLanguage("eng");
		instance.setOcrEngineMode(2);
		String ocrImagePath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString()
				+ ".png";
		ImageIO.write(bufferedImage, "png", new File(ocrImagePath));
		System.out.println("ocrImagePath  " + ocrImagePath);

		try {
			String imgText = instance.doOCR(new File(ocrImagePath));
//			return imgText;
			return Result.PASS().setOutput(imgText).make();
		} catch (TesseractException e) {
			e.getMessage();
//			return "Error while reading image";
			return Result.FAIL(ResultCodes.ERROR_TEXT_NOT_FOUND).setMessage("No text found in image.").make();
		}

	}

//	public String getImgText(String imageLocation) {
//		Tesseract instance = new Tesseract();
////		instance.setLanguage("eng");
//		instance.setOcrEngineMode(1);
//		try {
//			String imgText = instance.doOCR(new File(imageLocation));
//			return imgText;
//		} catch (TesseractException e) {
//			e.getMessage();
//			return "Error while reading image";
//		}
//	}
//
//	public static void main(String[] args) throws IOException {
//		Imageread app = new Imageread();
////		System.out.println(app.getImgText("D:\\img-2.png"));
//
//		BufferedImage imageToOcr = ImageIO.read(new File("D:\\img-2.png"));
//
//		TextRecognizer tr = TextRecognizer.start();
//		System.out.println(tr.doOCR(imageToOcr));
//
//	}

}
