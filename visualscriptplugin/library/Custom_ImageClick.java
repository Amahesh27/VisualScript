package com.crestech.opkey.plugin.visualscriptplugin.library;

import org.sikuli.script.Match;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.visualscriptplugin.Util;
import com.crestech.opkey.plugin.visualscriptplugin.VisualScriptObject;

public class Custom_ImageClick implements KeywordLibrary {
	public FunctionResult Custom_Method_ImageClick(VisualScriptObject obj, String matchScore) throws Exception {
		if (matchScore == null || matchScore.isEmpty())
			matchScore = "0.8";
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(matchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		System.out.println("***********Value convert in double is : " + valueOfMatchScore);
		Match find = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		Util.opkey_click(find);
		return Result.PASS().setOutput(true).make();
	}
}
