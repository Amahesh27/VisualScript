package com.crestech.opkey.plugin.visualscriptplugin.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import com.crestech.opkey.plugin.visualscriptplugin.Util;
import com.crestech.opkey.plugin.visualscriptplugin.VisualScriptObject;

public class VisualScriptFinder {
	private Logger a = Logger.getLogger(getClass().getName());

	public Object findObject(VisualScriptObject object, double tolerance, int timeout) throws FindFailed {
		System.out.println("***********Value of tolerance is : " + tolerance);
		double matchScorePercentage = ImageCompareUtils.getMatchScore(tolerance, 0.8D);
		int i = 0;
		while (i < 10) {
			try {
				this.a.fine(">>Finding Object Attempt: " + i);
				String imagePath = object.getImagePath();
				Pattern ptn = new Pattern(imagePath);
				Screen screen = Util.getScreen();
				Iterator<Match> itr = screen.findAll(ptn.similar(object.getMatchPercentage().floatValue()));
				List<Match> allMatchedObject = new ArrayList<>();
				System.out.println("allMatchedObject:: " + allMatchedObject.size());
				while (itr.hasNext()) {
					Match match = itr.next();
					if (match.getScore() >= matchScorePercentage)
						allMatchedObject.add(match);
					this.a.fine("Score " + match.getScore() + " Found match " + match.toString());
				}
				this.a.fine(">>All Matched Object Found " + allMatchedObject.size());
				if (allMatchedObject.size() > 0) {
					if (object.getRelativeLeft() > -1100001.0F && object.getRelativeTop() > -1100001.0F) {
						((Match) allMatchedObject.get(0)).setTargetOffset((int) object.getRelativeLeft(),
								(int) object.getRelativeTop());
						return allMatchedObject.get(0);
					}
					return allMatchedObject.get(0);
				}
			} catch (FindFailed findFailed) {
			}
			i++;
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		throw new FindFailed("Object Not Found");
	}

	public Object findObject(VisualScriptObject object, int timeout) throws FindFailed {
		int i = 0;
		while (i < 10) {
			try {
				this.a.fine(">>Finding Object Attempt: " + i);
				String imagePath = object.getImagePath();
				Pattern ptn = new Pattern(imagePath);
				Screen screen = Util.getScreen();
				Iterator<Match> itr = screen.findAll(ptn.similar(object.getMatchPercentage().floatValue()));
				List<Match> allMatchedObject = new ArrayList<>();
				while (itr.hasNext()) {
					Match match = itr.next();
					if (match.getScore() >= 0.8D)
						allMatchedObject.add(match);
					this.a.fine("Score " + match.getScore() + " Found match " + match.toString());
				}
				this.a.fine(">>All Matched Object Found " + allMatchedObject.size());
				if (allMatchedObject.size() > 0) {
					if (object.getRelativeLeft() > -1100001.0F && object.getRelativeTop() > -1100001.0F) {
						((Match) allMatchedObject.get(0)).setTargetOffset((int) object.getRelativeLeft(),
								(int) object.getRelativeTop());
						return allMatchedObject.get(0);
					}
					return allMatchedObject.get(0);
				}
			} catch (FindFailed findFailed) {
			}
			i++;
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		throw new FindFailed("Object Not Found");
	}

	public Object findObject_verification(VisualScriptObject object, double matchScore) {
		double matchScorePercentage = ImageCompareUtils.getMatchScore(matchScore, 0.8D);
		try {
			return findObject(object, matchScorePercentage, 10);
		} catch (FindFailed e) {
			return null;
		}
	}

	public Object findObject_verification_smallObjects(VisualScriptObject object, double matchScore) {
		double matchScorePercentage = ImageCompareUtils.getMatchScore(matchScore, 0.9D);
		try {
			return findObject(object, matchScorePercentage, 10);
		} catch (FindFailed e) {
			return null;
		}
	}
}
