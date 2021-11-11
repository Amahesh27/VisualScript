package com.crestech.opkey.plugin.visualscriptplugin.remote.local;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.xml.bind.Marshaller;

import com.crestech.opkey.plugin.communication.contracts.functioncall.FunctionCall;
import com.crestech.opkey.plugin.communication.transport.ChannelClosedException;
import com.crestech.opkey.plugin.communication.transport.RawMessage;
import com.crestech.opkey.plugin.communication.transport.TransportLayer;

public class BridgingTransportLayer extends TransportLayer implements Observer {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private TransportLayer opkeyFacing;
	private TransportLayer remoteFacing;

	public BridgingTransportLayer(final TransportLayer opkeyFacing, TransportLayer remoteFacing) {
		this.opkeyFacing = opkeyFacing;
		this.opkeyFacing.addObserver(this);

		this.remoteFacing = remoteFacing;
		this.remoteFacing.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object arg) {
				RawMessage rawMsg = (RawMessage) arg;
				Object msg = rawMsg.getMessage();

				try {
					logger.finest("From remote to OpKey: " + rawMsg.getMessageHeader());
					opkeyFacing.SendMessage(msg, null);

				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					String exceptionAsString = sw.toString();
					logger.severe(exceptionAsString);
				}
			}
		});
	}

	@Override
	public void run() {
		// no need to do own pumping
	}

	@Override
	public void SendMessage(Object msg, Marshaller messageSerializer) throws Exception {
		RawMessage rm = new RawMessage(msg);
		logger.finest("From bridge to remote: " + rm.getMessageHeader());
		remoteFacing.SendMessage(msg, messageSerializer);
	}

	@Override
	protected void setup() throws Exception {
		remoteFacing.open(); // has more chance to fail
		opkeyFacing.open();
	}

	@Override
	protected void cleanup() {
		opkeyFacing.close();
	}

	@Override
	public void update(Observable observable, Object arg) {
		// from OpKey to remote

		RawMessage rawMsg = (RawMessage) arg;
		Object msg = rawMsg.getMessage();
		Class<?> msgClass = msg.getClass();

		if (msgClass == String.class) {
			if (rawMsg.getMessageHeader().equalsIgnoreCase("FunctionCall")) {
				passToUpperLayer(rawMsg);

			} else {
				auxMessageFromOpKeyToRemote(rawMsg);
			}

		} else if (FunctionCall.class.isAssignableFrom(msgClass)) {
			passToUpperLayer(rawMsg);

		} else if (ChannelClosedException.class.isAssignableFrom(msgClass)) {
			passToUpperLayer(rawMsg);

		} else {
			auxMessageFromOpKeyToRemote(rawMsg);
		}
	}

	private void auxMessageFromOpKeyToRemote(RawMessage rawMessage) {
		try {
			logger.finest("From opkey to remote: " + rawMessage.getMessageHeader());
			remoteFacing.SendMessage(rawMessage.getMessage(), null);

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			logger.severe(exceptionAsString);
		}
	}

	private void passToUpperLayer(RawMessage rawMsg) {
		logger.finest("From opkey to bridge: " + rawMsg.getMessageHeader());
		setChanged();
		notifyObservers(rawMsg);
	}
}
