package com.crestech.opkey.plugin.visualscriptplugin.library;

public class ImageCompareUtils {

	public static double getMatchScore(double matchScore, double defaultMatchScore) {
		double tempMatchScore;
		if(matchScore==0.0) {
			tempMatchScore = defaultMatchScore;
		} else {
			tempMatchScore = matchScore;
		}
		System.out.println("**********Value of matchswcore is :"+tempMatchScore);
		return tempMatchScore;
	}
	
	public static String isStringNullOrBlank(String match) {
		if(match==null || match.isEmpty()) {
			match="0.8";
		}
		return match;
	}

}
