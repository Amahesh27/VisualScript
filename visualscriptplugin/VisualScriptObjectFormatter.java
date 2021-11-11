package com.crestech.opkey.plugin.visualscriptplugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.crestech.opkey.plugin.communication.contracts.functioncall.FunctionCall.ObjectArguments.ObjectArgument;
import com.crestech.opkey.plugin.communication.contracts.functioncall.Object;
import com.crestech.opkey.plugin.communication.contracts.functioncall.Object.Properties;
import com.crestech.opkey.plugin.communication.contracts.functioncall.Object.Properties.Property;
import com.crestech.opkey.plugin.functiondispatch.ArgumentFormatter;
import com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers.ImageNotFoundException;
import com.crestech.opkey.plugin.visualscriptplugin.library.Scaling;

public class VisualScriptObjectFormatter extends ArgumentFormatter {

	private static Logger logger = Logger.getLogger(VisualScriptPlugin.class.getName());

	@Override
	protected java.lang.Object FormatObjectArgument(ObjectArgument oArg) throws Exception {
		Stack<Object> stk = new Stack<Object>();
		Object currentObj = oArg.getObject();
		stk.push(currentObj);

		// lets push the current object hierarchy into a stack, so that when
		// Retrieving, we can traverse from child to object. This is similar to
		// reversing the Object hierarchy

		while (currentObj.getChildObject() != null) {
			currentObj = currentObj.getChildObject().getObject();
			stk.push(currentObj);
		}

		// Now, the stack should be somewhat like this :
		// childObj -- parentObject -- grandParentObject -....- rootParentObject

		if (stk.size() == 0)
			throw new ImageNotFoundException();

		VisualScriptObject obj = new VisualScriptObject();
		Object leafObject = stk.pop();

		Properties objectproperties = leafObject.getProperties();
		for (Property prp : objectproperties.getProperty()) {
			System.out.println("prp.getName()  " + prp.getName());
		}

		for (Property prp : objectproperties.getProperty()) {
			if (!prp.isIsUsed() && !leafObject.isUseSmartIdentification())
				continue; // neither is-used, nor smart-identification

			if (prp.getDataType().equalsIgnoreCase("Image")) {
				String imageData = prp.getValue();

				if (imageData.startsWith("BASE64:")) {
					imageData = imageData.substring(7);
					byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(imageData);
					Path tempPath = Files.createTempFile("visualscript_", "_from-base64.png");
					Files.write(tempPath, imageBytes);
					imageData = tempPath.toString();
					logger.fine("Base64 image dumped at " + tempPath.toString());
				}

				BufferedImage originalImage = ImageIO.read(new File(imageData));
				File finalImage = File.createTempFile("FinalImage_", ".png");
				ImageIO.write(originalImage, "png", finalImage);

				String scaledPath = Scaling.getScaledImagePath(finalImage.getAbsolutePath());
				obj.setImagePath(scaledPath);

			} else if (prp.getName().equalsIgnoreCase("Tolerance")) {
				// tolerance is the complement of match percentage
				obj.setMatchPercentage(1.0f - Float.parseFloat(prp.getValue()));
			} else if (prp.getName().equalsIgnoreCase("RelativeLeft")) {
				obj.setRelativeLeft(Float.parseFloat(prp.getValue()));

			} else if (prp.getName().equalsIgnoreCase("RelativeTop")) {
				obj.setRelativeTop(Float.parseFloat(prp.getValue()));
			}

		}

		obj.validate();
		return obj;
	}
}
