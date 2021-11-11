package com.crestech.opkey.plugin.visualscriptplugin.imagecompare;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * ImageMerger acts as an ImageProducer to generate the merger of two images.
 * When created, it expects to Image objects. It extracts the pixels from these
 * objects, and merges them using a simple weighted addition in RGB space. The
 * width and height of the result image are each the maximum of the widths and
 * heights of the input images. Normally, you'll get best results when the
 * images are exactly the same size.
 */
public class ImageMerger {
	protected double img1weight, img2weight;
	protected Image baselineImage;
	protected Image testImage;
	protected ColorModel cm;

	int rwid, rhgt;
	protected int resultantImagePixels[];
	private Boolean highlight;

	/**
	 * Create in ImageMerger object to merge two images. This does not perform the
	 * merger, that should be done by calling generate(). This constructors sets the
	 * weights to 0.5 and 0.5.
	 */
	public ImageMerger(Image baselineImage, Image testImage, boolean highlight) {
		cm = null;
		this.baselineImage = baselineImage;
		this.testImage = testImage;
		img1weight = 0.5;
		img2weight = 0.5;
		rwid = 0;
		rhgt = 0;
		resultantImagePixels = null;
		this.highlight = highlight;
	}

	/**
	 * Set the relative weights of the two images. Usually, these should add up to
	 * 1.0, but they don't have to.
	 */
	public void setWeights(double img1weight, double img2weight) {
		this.img1weight = img1weight;
		this.img2weight = img2weight;
	}

	/**
	 * Generate the merged image and store it for later hand-off to an
	 * ImageConsumer. The caller must supply a Component c on which the image will
	 * eventually be drawn.
	 */
	private boolean generate_(Component comp) {
		MediaTracker mt;
		mt = new MediaTracker(comp);
		mt.addImage(baselineImage, 0);
		mt.addImage(testImage, 1);
		try {
			mt.waitForAll();
		} catch (Exception ie) {
		}

		int wid1, wid2;
		int hgt1, hgt2;

		wid1 = baselineImage.getWidth(comp);
		wid2 = testImage.getWidth(comp);
		hgt1 = baselineImage.getHeight(comp);
		hgt2 = testImage.getHeight(comp);

		rwid = Math.max(wid1, wid2);
		rhgt = Math.max(hgt1, hgt2);

		resultantImagePixels = new int[rwid * rhgt];

		int[] p1 = new int[rwid * rhgt];
		int[] p2 = new int[rwid * rhgt];

		PixelGrabber pg1 = new PixelGrabber(baselineImage, 0, 0, wid1, hgt1, p1, 0, rwid);
		try {
			pg1.grabPixels();
		} catch (Exception ie1) {
		}

		PixelGrabber pg2 = new PixelGrabber(testImage, 0, 0, wid2, hgt2, p2, 0, rwid);
		try {
			pg2.grabPixels();
		} catch (Exception ie2) {
		}

		cm = ColorModel.getRGBdefault();

		int y, x, rp, rpi;
		int red1, red2, redr;
		int green1, green2, greenr;
		int blue1, blue2, bluer;
		int alpha1, alpha2, alphar;
		double wgt1, wgt2;

		for (y = 0; y < rhgt; y++) {
			for (x = 0; x < rwid; x++) {
				rpi = y * rwid + x;
				rp = 0;

				blue1 = p1[rpi] & 0x00ff;
				blue2 = p2[rpi] & 0x00ff;

				green1 = (p1[rpi] >> 8) & 0x00ff;
				green2 = (p2[rpi] >> 8) & 0x00ff;

				red1 = (p1[rpi] >> 16) & 0x00ff;
				red2 = (p2[rpi] >> 16) & 0x00ff;

				alpha1 = (p1[rpi] >> 24) & 0x00ff;
				alpha2 = (p2[rpi] >> 24) & 0x00ff;

				// Com*****tions for combining the pixels, // perform this
				// any
				// ay you like! // Here we just use simple weighted
				// addition.
				wgt1 = img1weight * (alpha1 / 255.0);
				wgt2 = img2weight * (alpha2 / 255.0);

				redr = (int) (red1 * wgt1 + red2 * wgt2); //
				if (highlight) {
					redr = redr + 50;
				}
				redr = (redr < 0) ? (0) : ((redr > 255) ? (255) : (redr));

				greenr = (int) (green1 * wgt1 + green2 * wgt2); // if(highlight)
				{
					greenr = greenr + 50;
				}
				greenr = (greenr < 0) ? (0) : ((greenr > 255) ? (255) : (greenr));

				bluer = (int) (blue1 * wgt1 + blue2 * wgt2);
				bluer = (bluer < 0) ? (0) : ((bluer > 255) ? (255) : (bluer));

				alphar = 255;

				rp = (((((alphar << 8) + (redr & 0x0ff)) << 8) + (greenr & 0x0ff)) << 8) + (bluer & 0x0ff);

				// save the pixel
				resultantImagePixels[rpi] = rp;
			}
		}

		return true;

	}

	/**
	 * Simple approach to getting an image - just create one using
	 * MemoryImageSource.
	 * 
	 * @throws IOException
	 */
	public Image getGeneratedImage() throws IOException {
		Image ret;
		MemoryImageSource mis;
		if (resultantImagePixels == null) {
			Frame dummy = new Frame();

			generate_(dummy);
			dummy.dispose();
		}

		if (!highlight) {
			mis = new MemoryImageSource(rwid, rhgt, cm, resultantImagePixels, 0, rwid);
			ret = Toolkit.getDefaultToolkit().createImage(mis);
			return ret;

		} else {
			mis = new MemoryImageSource(rwid, rhgt, cm, resultantImagePixels, 0, rwid);
			ret = Toolkit.getDefaultToolkit().createImage(mis);
			// BufferedImage img = imageToBufferedImage(baselineImage);
			BufferedImage img = imageToBufferedImage(ret);
			Graphics2D g = img.createGraphics();
			Rectangle2D tr = new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight());
			Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
			g.setStroke(dashed);
			g.setPaint(Color.RED);
			g.draw(tr);
			File tempFile = File.createTempFile("overLayImage", ".png");
			ImageIO.write(img, "png", tempFile);

			g.dispose();
			return img;
		}

	}

	public BufferedImage getOverLayedImage() throws IOException {
		ImageMerger imerge = new ImageMerger(baselineImage, testImage, highlight);
		Image mergedImage = imerge.getGeneratedImage();
		imerge.dispose();

		BufferedImage bi = imageToBufferedImage(mergedImage);
		return bi;
	}

	public File getOverLayedImageFile() throws IOException {
		BufferedImage overlayedImage = this.getOverLayedImage();
		File outputfile = File.createTempFile("ImageOverlay", ".png");
		ImageIO.write(overlayedImage, "png", outputfile);
		return outputfile;
	}

	static BufferedImage imageToBufferedImage(Image im) {
		BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics bg = bi.getGraphics();
		bg.drawImage(im, 0, 0, null);
		bg.dispose();
		return bi;
	}

	/**
	 * Call this to free up pixel storage allocated by this ImageMerger object.
	 */
	public void dispose() {
		resultantImagePixels = null;
		return;
	}

}