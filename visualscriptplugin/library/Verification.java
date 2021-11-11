package com.crestech.opkey.plugin.visualscriptplugin.library;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.visualscriptplugin.Util;
import com.crestech.opkey.plugin.visualscriptplugin.VisualScriptObject;

public class Verification implements KeywordLibrary {
	private Logger a = Logger.getLogger(getClass().getName());

	public FunctionResult Method_VerifyObjectExists(VisualScriptObject obj, String matchScore)
			throws FindFailed, IOException {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match match = (Match) (new VisualScriptFinder()).findObject_verification(obj, valueOfMatchScore);
		if (match != null)
			return Result.PASS().setOutput(true).setMessage("Object Exists").make();
		return Result.FAIL(ResultCodes.ERROR_VERIFICATION_FAILLED).setOutput(false).setMessage("Object does not exist")
				.make();
	}

	public FunctionResult Method_GetObjectExists(VisualScriptObject obj, String matchScore)
			throws FindFailed, IOException {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match match = (Match) (new VisualScriptFinder()).findObject_verification(obj, valueOfMatchScore);
		if (match != null)
			return Result.PASS().setOutput(true).make();
		return Result.PASS().setOutput(false).setMessage("Object Exists").make();
	}

	public FunctionResult Method_GetObjectExistsByPixel(VisualScriptObject obj, String matchScore)
			throws FindFailed, IOException {
		if (matchScore == null || matchScore.isEmpty())
			matchScore = "0.9";
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(matchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match match = (Match) (new VisualScriptFinder()).findObject_verification_smallObjects(obj, valueOfMatchScore);
		if (match != null) {
			if (match.getScore() > 0.98D)
				return Result.PASS().setOutput(true).make();
			if (match.getScore() >= 0.95D) {
				long pixelDifference = (new Util()).compareObjectAndMatchImage(obj, match);
				System.out.println(">>Pixel Difference " + pixelDifference);
				if (pixelDifference > 20L)
					return Result.PASS().setOutput(false).setMessage(">>Pixel Difference " + pixelDifference).make();
				return Result.PASS().setOutput(true).make();
			}
		}
		return Result.PASS().setOutput(false).setMessage("Object does not exist").make();
	}

	public FunctionResult Method_FindMatchingObject(VisualScriptObject obj, String matchScore) throws FindFailed {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match m = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_FindAllMatchingObjects(VisualScriptObject obj, String matchScore) {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Double matchScorePercentage = Double.valueOf(ImageCompareUtils.getMatchScore(valueOfMatchScore, 0.8D));
		String imagePath = obj.getImagePath();
		int numberofmatchingObjects = 0;
		Pattern ptn = new Pattern(imagePath);
		String message = "";
		Screen screen = Util.getScreen();
		try {
			Iterator<Match> itr = screen.findAll(ptn.similar(obj.getMatchPercentage().floatValue()));
			BufferedImage screenShot = screen.capture().getImage();
			while (itr.hasNext()) {
				Match match = itr.next();
				if (match.getScore() >= matchScorePercentage.doubleValue())
					numberofmatchingObjects++;
				this.a.fine("Score " + match.getScore() + " Found match " + match.toString());
				Util.highlight(screenShot, match.getRect());
			}
			message = numberofmatchingObjects + " matches found";
		} catch (FindFailed e) {
			message = e.getMessage();
		}
		return Result.PASS().setOutput(numberofmatchingObjects).setMessage(message).make();
	}
}
