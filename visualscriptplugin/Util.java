package com.crestech.opkey.plugin.visualscriptplugin;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import com.crestech.opkey.plugin.KeywordLibrary;
import com.crestech.opkey.plugin.contexts.Context;

public class Util implements KeywordLibrary {
	public static Screen getScreen() {
		return (Screen) Context.session().getTool();
	}

	public static void highlight(BufferedImage screenShot, Rectangle rect) {
		Graphics2D graphics = screenShot.createGraphics();
		graphics.setColor(Color.YELLOW);
		graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
		graphics.dispose();
	}

	public static String stringFromBase64(String base64) {
		StringBuilder binary = new StringBuilder();
		int countPadding = countPadding(base64);
		for (int i = 0; i < (base64.length() - countPadding); i++) {
			int base64Value = fromBase64(String.valueOf(base64.charAt(i)));
			String base64Binary = Integer.toBinaryString(base64Value);
			StringBuilder base64BinaryCopy = new StringBuilder();
			if (base64Binary.length() < 6) {
				for (int j = base64Binary.length(); j < 6; j++) {
					binary.append("0");
					base64BinaryCopy.append("0");
				}
				base64BinaryCopy.append(base64Binary);
			} else {
				base64BinaryCopy.append(base64Binary);
			}
			binary.append(base64Binary);
		}
		StringBuilder utf8String = new StringBuilder();
		for (int bytenum = 0; bytenum < (binary.length() / 8); bytenum++) {
			StringBuilder utf8Bit = new StringBuilder();
			for (int bitnum = 0; bitnum < 8; bitnum++) {
				utf8Bit.append(binary.charAt(bitnum + (bytenum * 8)));
			}
			char utf8Char = (char) Integer.parseInt(utf8Bit.toString(), 2);
			utf8String.append(String.valueOf(utf8Char));
		}
		return utf8String.toString();
	}

	private static int fromBase64(String x) {
		String charBase64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		return charBase64.indexOf(x);
	}

	private static int countPadding(String countPadding) {
		int index = countPadding.indexOf("=");
		int count = 0;
		while (index != -1) {
			count++;
			countPadding = countPadding.substring(index + 1);
			index = countPadding.indexOf("=");
		}
		return count;
	}

	public static boolean isBase64(String stringBase64) {
		String regex = "([A-Za-z0-9+/]{4})*" + "([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)";
		java.util.regex.Pattern patron = java.util.regex.Pattern.compile(regex);
		if (!patron.matcher(stringBase64).matches()) {
			return false;
		} else {
			return true;
		}
	}

	private static void opkey_mouseMove(Match _target) throws FindFailed, InterruptedException {
		System.out.println(">>Mouse Move");

		int x = _target.getX() + (_target.getW() / 2) + _target.getTargetOffset().getX();
		int y = _target.getY() + (_target.getH() / 2) + _target.getTargetOffset().getY();
		try {
			Robot robot = new Robot();
			robot.mouseMove(x, y);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread.sleep(500);
	}

	public static void opkey_click(Match _target) throws FindFailed, InterruptedException {
		opkey_mouseMove(_target);
		System.out.println(">>Mouse Down");
		try {
			Robot robot = new Robot();
			robot.mousePress(InputEvent.BUTTON1_MASK);
			Thread.sleep(300);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void opkey_dbclick(Match _target) throws FindFailed, InterruptedException {
		opkey_mouseMove(_target);
		Robot robot;
		try {
			robot = new Robot();
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			Thread.sleep(200);
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void opkey_mouseMove(Pattern _target) throws FindFailed, InterruptedException {
		System.out.println(">>Mouse Move");
		Util.getScreen().mouseMove(_target);
		Thread.sleep(300);
	}

	public static void opkey_click(Pattern _target) throws FindFailed, InterruptedException {
		opkey_mouseMove(_target);
		System.out.println(">>Mouse Down");
		Util.getScreen().mouseDown(org.sikuli.script.Button.LEFT);
		Thread.sleep(500);
		System.out.println(">>Mouse Up");
		Util.getScreen().mouseUp(org.sikuli.script.Button.LEFT);
	}

	public static void opkey_dbclick(Pattern _target) throws FindFailed, InterruptedException {
		opkey_mouseMove(_target);
		System.out.println(">>Mouse Down");
		Util.getScreen().mouseDown(org.sikuli.script.Button.LEFT);
		System.out.println(">>Mouse Up");
		Util.getScreen().mouseUp(org.sikuli.script.Button.LEFT);
		Thread.sleep(200);
		System.out.println(">>Mouse Down");
		Util.getScreen().mouseDown(org.sikuli.script.Button.LEFT);
		System.out.println(">>Mouse Up");
		Util.getScreen().mouseUp(org.sikuli.script.Button.LEFT);
	}

	public static Float getDefaultMatchPercentage() {
		// init from settings
		String toleranceValue = Context.session().getSetting("Tolerance", "0.7f");
		if (toleranceValue == null) {
			toleranceValue = "0.7f";
		}
		if (toleranceValue.trim().isEmpty()) {
			toleranceValue = "0.7f";
		}
		System.out.println(">>Tolerance Value is " + toleranceValue);
		return Float.parseFloat(toleranceValue);

	}

	public long compareObjectAndMatchImage(VisualScriptObject object, Match match) throws IOException {
		String tempDir = System.getProperty("java.io.tmpdir");
		String imagePath1 = object.getImagePath();
		System.out.println(">>Image Path 1 " + imagePath1);
		ScreenImage simage = Util.getScreen().capture(match.getRect().x, match.getRect().y, match.getRect().width,
				match.getRect().height);
		BufferedImage image = simage.getImage();

		String imagePath2 = tempDir + File.separator + UUID.randomUUID().toString() + ".png";
		System.out.println(">>Image Path 2 " + imagePath2);
		ImageIO.write(image, "png", new File(imagePath2));
		return new Util().compareImagesWithOpenCV(imagePath1, imagePath2);
	}

	private long compareImagesWithOpenCV(String path1, String path2) {
		Mat mat1 = Imgcodecs.imread(path1);
		Mat mat2 = Imgcodecs.imread(path2);
		long failed = compareMat(mat1, mat2);
		System.out.println("Failed pixels colurs   " + failed);
		return failed;
	}

	private long compareMat(Mat srcMat, Mat destMat) {
		Mat copyOfSrcMat = new Mat();
		Mat copyOfDestMat = new Mat();
		// srcMat.copyTo(copyOfSrcMat);
		// destMat.copyTo(copyOfDestMat);
		Imgproc.cvtColor(srcMat, copyOfSrcMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(destMat, copyOfDestMat, Imgproc.COLOR_BGR2GRAY);
		Imgproc.blur(copyOfSrcMat, copyOfSrcMat, new Size(3, 3));
		Imgproc.blur(copyOfDestMat, copyOfDestMat, new Size(3, 3));
		Imgproc.Canny(copyOfSrcMat, copyOfSrcMat, 60, 180);
		Imgproc.Canny(copyOfDestMat, copyOfDestMat, 60, 180);
		int numberOfPixelsOFSrc = copyOfSrcMat.channels() * copyOfSrcMat.rows() * copyOfSrcMat.cols();
		int numberOfPixelsOFDest = copyOfDestMat.channels() * copyOfDestMat.rows() * copyOfDestMat.cols();
		int rowCountOfSrcMat = copyOfSrcMat.rows();
		int colCountOfSrcMat = copyOfSrcMat.cols();

		int rowCountOfDestMat = copyOfDestMat.rows();
		int colCountOfDestMat = copyOfDestMat.cols();

		int srcMatColorChannelsCount = copyOfSrcMat.channels();
		int destMatColorChannelsCount = copyOfDestMat.channels();
		if (numberOfPixelsOFSrc != numberOfPixelsOFDest) {
		}

		if (rowCountOfSrcMat != rowCountOfDestMat) {
		}
		if (colCountOfSrcMat != colCountOfDestMat) {
		}

		if (srcMatColorChannelsCount != destMatColorChannelsCount) {
		}
		System.out.println(">>Source Mat Row Count " + rowCountOfSrcMat);
		System.out.println(">>Source Mat Col Count " + colCountOfSrcMat);
		System.out.println(">>Dest Mat Row Count " + rowCountOfDestMat);
		System.out.println(">>Dest Mat Col Count " + colCountOfDestMat);
		System.out.println(">>Source Mat Color Channel Count " + numberOfPixelsOFSrc);
		System.out.println(">>Dest Mat Color Channel Count " + numberOfPixelsOFDest);
		long failPercentage = 0;
		for (int row = 0; row < rowCountOfSrcMat; row++) {
			for (int col = 0; col < colCountOfSrcMat; col++) {
				double[] srcMatColorPixels = copyOfSrcMat.get(row, col);
				double[] destMatColorPixels = copyOfDestMat.get(row, col);
				boolean differncesFound = false;
				for (int colorPixel = 0; colorPixel < srcMatColorChannelsCount; colorPixel++) {
					double srcMatColorPixel = srcMatColorPixels[colorPixel];
					double destMatColorPixel = destMatColorPixels[colorPixel];
					if (srcMatColorPixel != destMatColorPixel) {
						differncesFound = true;
						break;
					}
				}
				if (differncesFound) {
					failPercentage++;
				}
			}
		}
		return failPercentage;
	}
}
