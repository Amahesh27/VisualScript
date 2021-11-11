package com.crestech.opkey.plugin.visualscriptplugin;

import java.io.File;
import java.io.FileNotFoundException;

import com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers.ImageNotFoundException;
import com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers.VisualScriptArgumentException;

public class VisualScriptObject {

	private String imagePath = null;
	private Float matchPercentage = Util.getDefaultMatchPercentage(); // default value
	private float relativeTop = -1100001;
	private float relativeLeft = -1100001;
//	private Double matchScore ;

	public void setImagePath(String imgPath) {
		this.imagePath = imgPath;
	}

	public String getImagePath() {
		return imagePath;
	}

	public Float getMatchPercentage() {
		return matchPercentage;
	}

	public void setMatchPercentage(Float matchPercentage) {
		this.matchPercentage = matchPercentage;
	}

//	public Double getMatchScore() {
//		return matchScore;
//	}
//
//	public void setMatchScore(Double matchScore) {
//		this.matchScore = matchScore;
//	}

	public void setRelativeTop(float top) {
		this.relativeTop = top;
	}

	public float getRelativeTop() {
		return this.relativeTop;
	}

	public void setRelativeLeft(float left) {
		this.relativeLeft = left;
	}

	public float getRelativeLeft() {
		return this.relativeLeft;
	}

	public void validate() throws FileNotFoundException, ImageNotFoundException, VisualScriptArgumentException {
		System.out.println("Inside validate method()");
		if (this.getImagePath() != null && this.getImagePath().trim().length() > 0) {
			File f = new File(this.getImagePath());
			if (!f.exists() || f.isDirectory())
				// an image file is provided but is not actually present
				throw new FileNotFoundException(f.getAbsolutePath());

		} else { // no image file is provided
			throw new VisualScriptArgumentException("Image property not available in the selected Object.");
		}
//		System.out.println("Going for checking value is null or empty   :" + this.getMatchScore());
//
//		if (this.getMatchScore() == null) {
//			System.out.println("checking for null or is empty in validate()");
//			this.setMatchScore(0.8);
//
//		}
	}

}
