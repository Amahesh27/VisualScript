package com.crestech.opkey.plugin.visualscriptplugin.remote.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.crestech.opkey.plugin.communication.contracts.functioncall.FunctionCall;
import com.crestech.opkey.plugin.communication.contracts.functioncall.FunctionCall.ObjectArguments.ObjectArgument;
import com.crestech.opkey.plugin.communication.contracts.functioncall.Object.Properties.Property;

public class FunctionCallInterceptor implements Observer {

	@Override
	public void update(Observable observable, Object obj) {
		ReversibleFunctionCallChannel fcc = (ReversibleFunctionCallChannel) observable;
		try {
			FunctionCall fc = fcc.NextCall();
			absorbImagesIn(fc);
			fcc.CallForward(fc);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-3456);
		}
	}

	public void absorbImagesIn(FunctionCall fc) throws IOException {
		List<ObjectArgument> objArgs = fc.getObjectArguments().getObjectArgument();

		for (ObjectArgument objArg : objArgs) {
			processObject(objArg.getObject());
		}
	}

	private void processObject(com.crestech.opkey.plugin.communication.contracts.functioncall.Object object)
			throws IOException {
		for (Property prop : object.getProperties().getProperty()) {
			if (prop.getDataType().equals("Image")) {
				String filePath = prop.getValue();
				String base64 = getFileBase64(filePath);
				prop.setValue(base64);
			}
		}

		if (object.getChildObject() != null)
			processObject(object.getChildObject().getObject());
	}

	private String getFileBase64(String filePath) throws IOException {
		byte[] fileBytes = Files.readAllBytes(new File(filePath).toPath());
		String base64 = javax.xml.bind.DatatypeConverter.printBase64Binary(fileBytes);
		return "BASE64:" + base64;
	}

}
