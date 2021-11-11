package com.crestech.opkey.plugin.visualscriptplugin.imagecompare;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import javax.imageio.ImageIO;

import com.crestech.opkey.plugin.functiondispatch.Tuple;
import com.crestech.opkey.plugin.visualscriptplugin.library.Comparison;
import com.crestech.opkey.plugin.visualscriptplugin.library.ImageCompareUtils;

public final class ImageComparer {
	public Tuple<Integer, String> compareTwoImagesByHighlightingOrOverlaying(File baselineImage, File testImage,
			String outputFolderPath, int numberOrRows, int numberOfColumns, boolean shouldOverlayImages)
			throws Exception {
		return compareTwoImagesByHighlightingOrOverlaying(baselineImage, testImage, outputFolderPath, numberOrRows,
				numberOfColumns, shouldOverlayImages, 0.72D);
	}

	public Tuple<Integer, String> compareTwoImagesByHighlightingOrOverlaying(File baselineImage, File testImage,
			String outputFolderPath, int numberOrRows, int numberOfColumns, boolean shouldOverlayImages,
			double matchScore) throws Exception {
		long exeStrtTime = Calendar.getInstance().getTime().getTime();
		Double matchScorePercentage = Double.valueOf(ImageCompareUtils.getMatchScore(matchScore, 0.7200000286102295D));
		Method_resizeImage(baselineImage.getAbsolutePath(), testImage.getAbsolutePath());
		String _baselineImageFolderPath = baselineImage.getParentFile().getAbsolutePath() + "/BaselineImage";
		String _testImage2FolderPath = testImage.getParentFile().getAbsolutePath() + "/TestImage";
		System.out.println("Splitting Images");
		Method_SplitImage(baselineImage.getAbsolutePath(), _baselineImageFolderPath, numberOrRows, numberOfColumns);
		Method_SplitImage(testImage.getAbsolutePath(), _testImage2FolderPath, numberOrRows, numberOfColumns);
		System.out.println("Overlaying Images");
		int faultyBlocksCount = Method_CompareAndCreateHighLightImages(_baselineImageFolderPath, _testImage2FolderPath,
				outputFolderPath, matchScorePercentage, shouldOverlayImages);
		String outputFilePath = Method_concatImageFromFolder(outputFolderPath, numberOrRows, numberOfColumns);
		deleteDirectory(new File(_baselineImageFolderPath));
		deleteDirectory(new File(_testImage2FolderPath));
		System.out.println("Execution Time..... ");
		long exeEndTime = Calendar.getInstance().getTime().getTime();
		long totalExeTime = exeEndTime - exeStrtTime;
		System.out.println("Total Execution Time is " + (totalExeTime / 1000L) + " seconds");
		return new Tuple(Integer.valueOf(faultyBlocksCount), outputFilePath);
	}

	public void Method_SplitImage(String imagePath, String outputFolderPath, int rowPixel, int ColPixel)
			throws InterruptedException, IOException {
		System.out.println("Splitting Image '" + imagePath + "' into multiple smaller images.");
		File outputFolder = new File(outputFolderPath);
		deleteDirectory(outputFolder);
		outputFolder.mkdirs();
		File orgFile = new File(imagePath);
		FileInputStream fis = new FileInputStream(orgFile);
		BufferedImage image = ImageIO.read(fis);
		int rows = rowPixel;
		int cols = ColPixel;
		int chunks = rows * cols;
		int chunkWidth = image.getWidth() / cols;
		int chunkHeight = image.getHeight() / rows;
		int count = 0;
		BufferedImage[] imgs = new BufferedImage[chunks];
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < cols; y++) {
				imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());
				Graphics2D gr = imgs[count++].createGraphics();
				gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x,
						chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
				gr.dispose();
			}
		}
		for (int i = 0; i < imgs.length; i++) {
			String ii = String.format("%06d", new Object[] { Integer.valueOf(i) });
			ImageIO.write(imgs[i], "png", new File(outputFolderPath + "\\" + ii + ".png"));
		}
		System.out.println(imgs.length + " mini images created");
	}

	public int Method_CompareAndCreateHighLightImages(String firstImageFolderPath, String secondImageFolderPath,
			String outputImageFolderPath, Double acceptableImageMatchScore, boolean shouldOverlay) throws Exception {
		int imageBlocksDiffer = 0;
		File[] listOfPNGFile1 = getAllPNGFileFromDirectory(firstImageFolderPath);
		File[] listOfPNGFile2 = getAllPNGFileFromDirectory(secondImageFolderPath);
		File outFolder = new File(outputImageFolderPath);
		deleteDirectory(outFolder);
		outFolder.mkdirs();
		if (listOfPNGFile1.length == listOfPNGFile2.length) {
			for (int i = 0; i < listOfPNGFile1.length; i++) {
				File torenceImg, image1 = listOfPNGFile1[i];
				File image2 = listOfPNGFile2[i];
				Comparison c = new Comparison();
				float imageScore = c.compareImagesBySikuliAndThenPixelByPixel(image1.getAbsolutePath(),
						image2.getAbsolutePath(), acceptableImageMatchScore);
				Boolean imagesDiffer = Boolean.valueOf((imageScore < acceptableImageMatchScore.doubleValue()));
				if (imagesDiffer.booleanValue()) {
					System.out.println("ImageScore: " + imageScore + "\n" + image1.getAbsolutePath() + "\n"
							+ image2.getAbsolutePath());
					imageBlocksDiffer++;
					if (shouldOverlay) {
						torenceImg = overlayImage(image1.getAbsolutePath(), image2.getAbsolutePath(), false);
					} else {
						torenceImg = highlightImage(ImageIO.read(image2));
					}
				} else {
					torenceImg = image2;
				}
				String ii = String.format("%06d", new Object[] { Integer.valueOf(i) });
				String overlayImagePath = "" + outputImageFolderPath + "\\" + ii + ".png";
				try (FileInputStream fInStream = new FileInputStream(torenceImg);
						FileOutputStream fOutStream = new FileOutputStream(overlayImagePath);
						FileChannel inChannel = fInStream.getChannel();
						FileChannel outChannel = fOutStream.getChannel()) {
					inChannel.transferTo(0L, inChannel.size(), outChannel);
				}
			}
		} else {
			throw new Exception("Different number of Images exists in both the folders.");
		}
		return imageBlocksDiffer;
	}

	private File overlayImage(String baseImage, String testImage, boolean highlight)
			throws InterruptedException, IOException {
		Image i1 = Toolkit.getDefaultToolkit().createImage(baseImage);
		Image i2 = Toolkit.getDefaultToolkit().createImage(testImage);
		ImageMerger icom = new ImageMerger(i1, i2, highlight);
		File overlayedImage = icom.getOverLayedImageFile();
		return overlayedImage;
	}

	public File highlightImage(BufferedImage img) throws IOException {
		Graphics2D g = img.createGraphics();
		Rectangle2D tr = new Rectangle2D.Double(0.0D, 0.0D, img.getWidth(), img.getHeight());
		Stroke dashed = new BasicStroke(3.0F, 0, 2, 0.0F, new float[] { 9.0F }, 0.0F);
		g.setStroke(dashed);
		g.setPaint(Color.RED);
		g.draw(tr);
		g.dispose();
		File tmp = File.createTempFile("highlighted", ".png");
		ImageIO.write(img, "PNG", tmp);
		return tmp;
	}

	public void Method_resizeImage(String imagePath1, String imagePath2) throws IOException {
		ImageReSizer a = new ImageReSizer();
		a.createResizedCopy(imagePath1, imagePath2);
	}

	public String Method_concatImageFromFolder(String outputFolderPath, int rowPixel, int colPixel) throws IOException {
		int rows = rowPixel;
		int cols = colPixel;
		int chunks = rows * cols;
		File[] imgFiles = new File[chunks];
		outputFolderPath = outputFolderPath + "\\";
		for (int i = 0; i < chunks; i++) {
			String ii = String.format("%06d", new Object[] { Integer.valueOf(i) });
			imgFiles[i] = new File(outputFolderPath + ii + ".png");
		}
		BufferedImage[] buffImages = new BufferedImage[chunks];
		for (int j = 0; j < chunks; j++)
			buffImages[j] = ImageIO.read(imgFiles[j]);
		int type = buffImages[0].getType();
		int chunkWidth = buffImages[0].getWidth();
		int chunkHeight = buffImages[0].getHeight();
		BufferedImage finalImg = new BufferedImage(chunkWidth * cols, chunkHeight * rows, type);
		int num = 0;
		for (int k = 0; k < rows; k++) {
			for (int m = 0; m < cols; m++) {
				finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * m, chunkHeight * k,
						(ImageObserver) null);
				num++;
			}
		}
		System.out.println("Image concatenated.....");
		String path = outputFolderPath + "\\overLayImage.png";
		ImageIO.write(finalImg, "png", new File(path));
		return path;
	}

	public static BufferedImage imageToBufferedImage(Image im) {
		BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), 1);
		Graphics bg = bi.getGraphics();
		bg.drawImage(im, 0, 0, null);
		bg.dispose();
		return bi;
	}

	public static File[] getAllPNGFileFromDirectory(String imageFolder) {
		File dir = new File(imageFolder);
		FilenameFilter IMAGE_FILTER = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".png"))
					return true;
				return false;
			}
		};
		File[] listFile = dir.listFiles(IMAGE_FILTER);
		return listFile;
	}

	public static boolean deleteDirectory(File directory) {
		File[] files = directory.listFiles();
		if (files != null)
			for (int n = 0; n < files.length; n++) {
				if (files[n].isFile())
					files[n].delete();
			}
		return directory.delete();
	}

	public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {
		int offset = 8;
		int wid = img1.getWidth() + img2.getWidth() + offset;
		int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
		BufferedImage newImage = new BufferedImage(wid, height, 2);
		Graphics2D g2 = newImage.createGraphics();
		Color oldColor = g2.getColor();
		g2.setPaint(Color.GRAY);
		g2.fillRect(0, 0, wid, height);
		g2.setColor(oldColor);
		g2.drawImage(img1, (BufferedImageOp) null, 0, 0);
		g2.drawImage(img2, (BufferedImageOp) null, img1.getWidth() + offset, 0);
		g2.dispose();
		return newImage;
	}
}
