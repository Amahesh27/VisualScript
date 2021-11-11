package com.crestech.opkey.plugin.visualscriptplugin.library;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.visualscriptplugin.Util;
import com.crestech.opkey.plugin.visualscriptplugin.VisualScriptObject;

public class Generic implements KeywordLibrary {
	public FunctionResult Method_ObjectClick(VisualScriptObject obj, String matchScore) throws Exception {
		System.out.println("***********Inside Method_ObjectClick");
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		System.out.println("**********stringMatchScore : " + stringMatchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		System.out.println("**********valueOfMatchScore : " + valueOfMatchScore);
		Match find = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		Util.opkey_click(find);
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_DoubleClick(VisualScriptObject obj, String matchScore)
			throws FindFailed, InterruptedException {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match find = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		Util.opkey_dbclick(find);
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_DragDrop(VisualScriptObject imageToDrag, VisualScriptObject imageToDropIn,
			String matchScore) throws FindFailed {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Double matchPercentage = Double.valueOf(ImageCompareUtils.getMatchScore(valueOfMatchScore, 0.8D));
		String imgToDrag = imageToDrag.getImagePath();
		String imgDropIn = imageToDropIn.getImagePath();
		Screen screen = Util.getScreen();
		Pattern ptnImgToDrag = new Pattern(imgToDrag);
		Iterator<Match> itr = screen.findAll(ptnImgToDrag.similar(imageToDrag.getMatchPercentage().floatValue()));
		List<Match> allMatchedObject = new ArrayList<>();
		System.out.println("imgToDrag allMatched:: " + allMatchedObject.size());
		while (itr.hasNext()) {
			Match match = itr.next();
			if (match.getScore() >= matchPercentage.doubleValue())
				allMatchedObject.add(match);
			System.out.println("imgToDrag Score " + match.getScore() + " Found match " + match.toString());
		}
		Pattern ptnImgDropIn = new Pattern(imgDropIn);
		Iterator<Match> itr1 = screen.findAll(ptnImgDropIn.similar(imageToDropIn.getMatchPercentage().floatValue()));
		List<Match> allMatchedObject1 = new ArrayList<>();
		System.out.println("imgDropIn allMatched:: " + allMatchedObject1.size());
		while (itr1.hasNext()) {
			Match match = itr1.next();
			if (match.getScore() >= matchPercentage.doubleValue())
				allMatchedObject1.add(match);
			System.out.println("imgDropIn Score " + match.getScore() + " Found match " + match.toString());
		}
		Double matchPercentOfImageToDropIn = Double.valueOf(ImageCompareUtils.getMatchScore(valueOfMatchScore,
				Double.valueOf(imageToDropIn.getMatchPercentage().floatValue()).doubleValue()));
		Double matchPercentOfImageToDrag = Double.valueOf(ImageCompareUtils.getMatchScore(valueOfMatchScore,
				Double.valueOf(imageToDrag.getMatchPercentage().floatValue()).doubleValue()));
		Util.getScreen().dragDrop(ptnImgToDrag.similar(matchPercentOfImageToDropIn.doubleValue()),
				ptnImgDropIn.similar(matchPercentOfImageToDrag.doubleValue()));
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_typeSecureText(VisualScriptObject object, String value, String matchScore)
			throws Exception {
		if (Util.isBase64(value))
			return Method_TypeTextOnEditBox(object, Util.stringFromBase64(value), matchScore);
		return Method_TypeTextOnEditBox(object, value, matchScore);
	}

	public FunctionResult Method_TypeTextOnEditBox(VisualScriptObject obj, String text, String matchScore)
			throws FindFailed {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match find = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		Util.getScreen().type(find, text);
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_ClearTextOnEditBox(VisualScriptObject obj, String matchScore) throws FindFailed {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match find = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		Util.getScreen().type(find, "");
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_PasteTextOnEditBox(VisualScriptObject obj, String matchScore)
			throws FindFailed, HeadlessException, UnsupportedFlavorException, IOException {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match find = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		Util.getScreen().paste(find, data);
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_RightClick(VisualScriptObject obj, String matchScore) throws FindFailed {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match find = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		Util.getScreen().rightClick(find);
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_Hover(VisualScriptObject obj, String matchScore) throws FindFailed {
		String stringMatchScore = ImageCompareUtils.isStringNullOrBlank(matchScore);
		double valueOfMatchScore = 0.0D;
		try {
			valueOfMatchScore = Double.parseDouble(stringMatchScore);
		} catch (Exception e) {
			return Result.FAIL(ResultCodes.ERROR_ARGUMENT_DATA_INVALID).setMessage("Match Score value is invalid")
					.setOutput(false).make();
		}
		Match find = (Match) (new VisualScriptFinder()).findObject(obj, valueOfMatchScore, 10);
		Util.getScreen().hover(find);
		return Result.PASS().setOutput(true).make();
	}

	public FunctionResult Method_GetTextFromTextArea(int x, int y, int width, int height) throws IOException {
		Screen screen = Util.getScreen();
		ScreenImage screenImage = screen.capture(x, y, width, height);
		String result = (new OCRv2()).performOCROnImage(screenImage.getImage());
		return Result.PASS().setOutput(result).make();
	}
}
