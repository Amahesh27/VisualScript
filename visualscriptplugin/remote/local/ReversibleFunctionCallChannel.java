package com.crestech.opkey.plugin.visualscriptplugin.remote.local;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.crestech.opkey.plugin.communication.contracts.functioncall.FunctionCall;
import com.crestech.opkey.plugin.communication.message.FunctionCallChannel;
import com.crestech.opkey.plugin.communication.transport.TransportLayer;

public class ReversibleFunctionCallChannel extends FunctionCallChannel {

	private Marshaller fcMarshaller = null;

	public ReversibleFunctionCallChannel(TransportLayer transport) throws JAXBException {
		super(transport);

		JAXBContext fcContext = JAXBContext.newInstance(FunctionCall.class);
		fcMarshaller = fcContext.createMarshaller();

		fcMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		fcMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
	}

	public void CallForward(FunctionCall fc) throws Exception {
		transport.SendMessage(fc, fcMarshaller);
	}

}
