package com.crestech.opkey.plugin.visualscriptplugin.library;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.sikuli.script.Match;
import org.sikuli.script.Pattern;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.contexts.Context;
import com.crestech.opkey.plugin.visualscriptplugin.Util;
import com.crestech.opkey.plugin.visualscriptplugin.VisualScriptObject;

public class Scaling implements KeywordLibrary {
	private static Logger logger = Logger.getLogger(Scaling.class.getName());

	private static double imageScalingFactor = 1.0D;

	private FunctionResult Method_TrainImageScaleFactor(VisualScriptObject obj, Boolean ScaleUP, double matchScore)
			throws IOException, InterruptedException {
		double trialScalingFactor = imageScalingFactor;
		Double matchScorePercentage = Double.valueOf(ImageCompareUtils.getMatchScore(matchScore,
				Double.valueOf(obj.getMatchPercentage().floatValue()).doubleValue()));
		while (Context.current().getCallRemainingMillis() > 5000) {
			String scaledImagePath = innerScaleImage(obj.getImagePath(), trialScalingFactor);
			logger.fine("ScaledImagePath:" + scaledImagePath);
			Pattern ptn = new Pattern(scaledImagePath);
			Match m = Util.getScreen().exists(ptn.similar(matchScorePercentage.doubleValue()));
			if (m != null) {
				imageScalingFactor = trialScalingFactor;
				logger.info("Scaling factor found: " + trialScalingFactor);
				return Result.PASS().setOutput(trialScalingFactor).setMessage("Scaling factor found").make();
			}
			if (trialScalingFactor >= 2.0D)
				return Result.FAIL(ResultCodes.ERROR_OBJECT_NOT_FOUND)
						.setMessage("Scaling factor was not found upto double the size").make();
			if (trialScalingFactor <= 0.3D)
				return Result.FAIL(ResultCodes.ERROR_OBJECT_NOT_FOUND)
						.setMessage("Scaling factor was not found upto one-third the size").make();
			if (ScaleUP.booleanValue()) {
				trialScalingFactor += 0.025D;
			} else if (!ScaleUP.booleanValue()) {
				trialScalingFactor -= 0.025D;
			}
			Thread.sleep(10L);
		}
		return Result.FAIL(ResultCodes.ERROR_OBJECT_NOT_FOUND)
				.setMessage("Scaling factor was not found in the given time").make();
	}

	public FunctionResult Method_setImageUpScaleFactor(VisualScriptObject obj, String matchScore)
			throws IOException, InterruptedException {
		if (matchScore == null || matchScore.isEmpty())
			matchScore = "0.7";
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		System.out.println("**********stringMatchScore : " + stringMatchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		return Method_TrainImageScaleFactor(obj, Boolean.valueOf(true), valueOfMatchScore);
	}

	public FunctionResult Method_setImageDownScaleFactor(VisualScriptObject obj, String matchScore)
			throws IOException, InterruptedException {
		if (matchScore == null || matchScore.isEmpty())
			matchScore = "0.7";
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		System.out.println("**********stringMatchScore : " + stringMatchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		return Method_TrainImageScaleFactor(obj, Boolean.valueOf(false), valueOfMatchScore);
	}

	public FunctionResult Method_ResetImageScaleFactor() {
		logger.info("Scaling factor reset to 1.0");
		imageScalingFactor = 1.0D;
		return Result.PASS().setOutput(true).setMessage("Scaling factor reset to 1.0").make();
	}

	public static String getScaledImagePath(String OriginalImagePath) {
		try {
			String scaledPath = innerScaleImage(OriginalImagePath, imageScalingFactor);
			return scaledPath;
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			logger.severe(exceptionAsString);
			return OriginalImagePath;
		}
	}

	private static String innerScaleImage(String OriginalImagePath, double ScalingFactor) throws IOException {
		if (Double.compare(ScalingFactor, 1.0D) == 0)
			return OriginalImagePath;
		logger.fine("Performing Scaling");
		BufferedImage before = ImageIO.read(new File(OriginalImagePath));
		BufferedImage after = resizeImage(before, before.getType(), ScalingFactor);
		File ScaledImage = File.createTempFile("ScaledImage_" + ScalingFactor + "_", ".png");
		ImageIO.write(after, "png", ScaledImage);
		logger.finer("ScaledImagePath: " + ScaledImage.getAbsolutePath());
		return ScaledImage.getAbsolutePath();
	}

	private static BufferedImage resizeImage(BufferedImage originalImage, int type, double ScalingFactor) {
		int IMG_HEIGHT = (int) (originalImage.getHeight() * ScalingFactor);
		int IMG_WIDTH = (int) (originalImage.getWidth() * ScalingFactor);
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();
		return resizedImage;
	}
}
