package com.crestech.opkey.plugin.visualscriptplugin.library;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.visualscriptplugin.Util;
import com.crestech.opkey.plugin.visualscriptplugin.VisualScriptObject;
import com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers.VisualScriptArgumentException;

public class OCR implements KeywordLibrary {
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public FunctionResult Method_readTextFromImage(VisualScriptObject obj) throws FindFailed, IOException {
		String textInImage = new OCRv2().performOCROnImage(new Pattern(obj.getImagePath()).getBImage());
		if (textInImage != null && textInImage.trim().length() > 0) {
			return Result.PASS().setOutput(textInImage).make();

		} else {
			return Result.FAIL(ResultCodes.ERROR_TEXT_NOT_FOUND).setMessage("No text found in image.").make();
		}

	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public FunctionResult Method_readTextFromImagePath(String imagePath) throws FindFailed, IOException {
		File f = new File(imagePath);
		if (!f.exists() || imagePath == null || imagePath.length() == 0) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Invalid file path: " + imagePath)
					.make();
		}
		String textInImage = new OCRv2().performOCROnImage(new Pattern(imagePath).getBImage());

		if (textInImage != null && textInImage.trim().length() > 0) {
			return Result.PASS().setOutput(textInImage).make();

		} else {
			return Result.FAIL(ResultCodes.ERROR_TEXT_NOT_FOUND).setMessage("No text found in image.").make();
		}
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public FunctionResult Method_readTextFromScreen() throws FindFailed, IOException {
		String textInImage = new OCRv2().performOCROnImage(Util.getScreen().capture().getImage());

		if (textInImage != null && textInImage.trim().length() > 0) {
			return Result.PASS().setOutput(textInImage).make();

		} else {
			return Result.FAIL(ResultCodes.ERROR_TEXT_NOT_FOUND).setMessage("No text found in image.").make();
		}
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public FunctionResult Method_readTextFromScreenCoordinates(int x, int y, int width, int height)
			throws FindFailed, IOException, VisualScriptArgumentException {
		if (width == 0 | height == 0)
			throw new VisualScriptArgumentException("Width/Height not specified properly. They may never be 0");
		String textInImage = new OCRv2().performOCROnImage(Util.getScreen().capture(x, y, width, height).getImage());

		if (textInImage != null && textInImage.trim().length() > 0) {
			return Result.PASS().setOutput(textInImage).make();

		} else {
			return Result.FAIL(ResultCodes.ERROR_TEXT_NOT_FOUND).setMessage("No text found in image.").make();
		}
	}

	private BufferedImage preprocessImageForOCR(BufferedImage br) throws IOException {
		return br;
	}

	private BufferedImage resize(BufferedImage inputImage, float factor) throws IOException {

		int scaledWidth = (int) (inputImage.getWidth() * factor);
		int scaledHeight = (int) (inputImage.getHeight() * factor);
		// creates output image
		BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
		g2d.dispose();

		return outputImage;
	}

}
