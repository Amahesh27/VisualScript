package com.crestech.opkey.plugin.visualscriptplugin.library;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.visualscriptplugin.Util;
import com.crestech.opkey.plugin.visualscriptplugin.VisualScriptObject;

public class AppearenceDisapprence implements KeywordLibrary {
	public FunctionResult Method_WaitforObjectAppearance(VisualScriptObject obj, int timeinSeconds, String matchScore)
			throws FindFailed, InterruptedException {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		(new VisualScriptFinder()).findObject(obj, valueOfMatchScore, timeinSeconds);
		Thread.sleep(1000L);
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_WaitforObjectDisAppearance(VisualScriptObject obj, int timeinSeconds,
			String matchScore) {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Double matchScorePercentage = Double.valueOf(ImageCompareUtils.getMatchScore(valueOfMatchScore,
				Double.valueOf(obj.getMatchPercentage().floatValue()).doubleValue()));
		String imagePath = obj.getImagePath();
		Pattern ptn = new Pattern(imagePath);
		if (Util.getScreen().waitVanish(ptn.similar(matchScorePercentage.doubleValue()), (timeinSeconds * 1000)))
			return Result.PASS().setOutput(true).make();
		return Result.FAIL(ResultCodes.ERROR_VERIFICATION_FAILLED).setOutput(false)
				.setMessage("Object did not Disappear. It is still present on the Screen").make();
	}
}
