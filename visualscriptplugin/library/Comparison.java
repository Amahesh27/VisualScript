package com.crestech.opkey.plugin.visualscriptplugin.library;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.functiondispatch.Tuple;
import com.crestech.opkey.plugin.visualscriptplugin.VisualScriptObject;
import com.crestech.opkey.plugin.visualscriptplugin.imagecompare.ImageComparer;

public class Comparison implements KeywordLibrary {
	private Logger a = Logger.getLogger(getClass().getName());

	public FunctionResult Method_compareImageObjects(VisualScriptObject image1, VisualScriptObject image2,
			String matchScore) throws Exception {
		return Method_compareImages(image1.getImagePath(), image2.getImagePath(), matchScore);
	}

	public FunctionResult Method_compareImages(String image1Path, String image2Path, String matchScore)
			throws Exception {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Double matchScorePercentage = Double.valueOf(ImageCompareUtils.getMatchScore(valueOfMatchScore, 0.8D));
		File f1 = new File(image1Path);
		if (!f1.exists() || image1Path == null || image1Path.length() == 0)
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Invalid file path: " + image1Path)
					.make();
		File f2 = new File(image2Path);
		if (!f2.exists() || image2Path == null || image2Path.length() == 0)
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Invalid file path: " + image2Path)
					.make();
		float score = compareImagesBySikuliAndThenPixelByPixel(f1.getAbsolutePath(), f2.getAbsolutePath());
		if (score == 0.0F)
			return Result.PASS().setOutput(false).setMessage("Image not Matched. Match Score: " + score).make();
		if (score > matchScorePercentage.doubleValue())
			return Result.PASS().setOutput(true).setMessage("Image Matched with Match Score: " + score).make();
		return Result.PASS().setOutput(false)
				.setMessage("Images matched by sikuli but pixel-by-pixel matching score was: " + score).make();
	}

	public float compareImagesBySikuliAndThenPixelByPixel(String image1Path, String image2Path) throws Exception {
		return compareImagesBySikuliAndThenPixelByPixel(image1Path, image2Path, Double.valueOf(0.95D));
	}

	public float compareImagesBySikuliAndThenPixelByPixel(String image1Path, String image2Path, Double matchScore)
			throws Exception {
		Double matchScorePercentage = Double.valueOf(ImageCompareUtils.getMatchScore(matchScore.doubleValue(), 0.95D));
		String bigImagePath = image1Path;
		String smallImagePath = image2Path;
		Pattern p1 = new Pattern(image1Path);
		Pattern p2 = new Pattern(image2Path);
		int image1Area = p1.getBImage().getWidth() * p1.getBImage().getHeight();
		int image2Area = p2.getBImage().getWidth() * p2.getBImage().getHeight();
		if (image1Area < image2Area) {
			this.a.fine("Image 2 is larger than Image 1, so finding Image 1 inside Image 2");
			bigImagePath = image2Path;
			smallImagePath = image1Path;
		}
		Pattern smallImagePattern = new Pattern(smallImagePath);
		Finder f = new Finder(bigImagePath);
		f.find(smallImagePattern);
		if (f.hasNext()) {
			Match m = f.next();
			double originalScore = m.getScore();
			if (originalScore > matchScorePercentage.doubleValue()) {
				BufferedImage bigThumbnail = ImageIO.read(new File(bigImagePath)).getSubimage(m.getX(), m.getY(),
						m.getW(), m.getH());
				BufferedImage smallImage = ImageIO.read(new File(smallImagePath));
				float score = matchPixelByPixel(smallImage, bigThumbnail);
				System.out.println("## OriginalScore:" + originalScore + "   pixel-by-pixel score: " + score);
				return score;
			}
			return (float) originalScore;
		}
		return 0.0F;
	}

	private float matchPixelByPixel(BufferedImage smallImage, BufferedImage bigImage) throws Exception {
		int matchingPixelCount = 0;
		int smallImageWidth = smallImage.getWidth();
		int smallImageHeight = smallImage.getHeight();
		if (smallImageWidth > bigImage.getWidth() || smallImageHeight > bigImage.getHeight())
			throw new Exception("Image must not be smaller than thumbnail");
		for (int w = 0; w < smallImageWidth; w++) {
			for (int h = 0; h < smallImageHeight; h++) {
				int pixel1 = smallImage.getRGB(w, h);
				int pixel2 = bigImage.getRGB(w, h);
				if (Math.abs(pixel1 - pixel2) < 65000)
					matchingPixelCount++;
			}
		}
		float score = (matchingPixelCount / smallImageHeight * smallImageWidth);
		return score;
	}

	public FunctionResult method_OverlayImagesObjectBased(VisualScriptObject baselineImage,
			VisualScriptObject testImage, Boolean shouldOverlayImages, int numberOfRows, int numberOfColumns,
			String matchScore) throws Exception {
		return method_OverlayImages(baselineImage.getImagePath(), testImage.getImagePath(), shouldOverlayImages,
				numberOfRows, numberOfColumns, matchScore);
	}

	public FunctionResult method_OverlayImages(String baselineImagePath, String testImagePath,
			Boolean shouldOverlayImages, int numberOfRows, int numberOfColumns, String matchScore) throws Exception {
		if (matchScore == null || matchScore.isEmpty())
			matchScore = "0.72";
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		if (!(new File(baselineImagePath)).exists())
			return Result.FAIL(ResultCodes.ERROR_OBJECT_NOT_FOUND).setMessage("Baseline Image Path not found").make();
		if (!(new File(testImagePath)).exists())
			return Result.FAIL(ResultCodes.ERROR_OBJECT_NOT_FOUND).setMessage("Test Image Path not found").make();
		ImageComparer ic = new ImageComparer();
		File outputFolder = File.createTempFile("Image Overlay", "");
		outputFolder.mkdirs();
		Tuple<Integer, String> faultCountAndOverlayImagePath = ic.compareTwoImagesByHighlightingOrOverlaying(
				new File(baselineImagePath), new File(testImagePath), outputFolder.getAbsolutePath(), numberOfRows,
				numberOfColumns, shouldOverlayImages.booleanValue(), valueOfMatchScore);
		BufferedImage sideBySideImage = ImageComparer.joinBufferedImage(ImageIO.read(new File(baselineImagePath)),
				ImageIO.read(new File((String) faultCountAndOverlayImagePath.y)));
		File sideBySideFile = File.createTempFile("SideBySide Files", ".png");
		ImageIO.write(sideBySideImage, "PNG", sideBySideFile);
		return Result.PASS().setOutput(((Integer) faultCountAndOverlayImagePath.x).intValue())
				.setSnapshotPath(sideBySideFile.getAbsolutePath()).make();
	}
}
