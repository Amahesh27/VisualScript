package com.crestech.opkey.plugin.visualscriptplugin.remote.local;

import java.io.File;
import java.net.ConnectException;
import java.util.Map;
import java.util.logging.Logger;

import com.crestech.opkey.plugin.communication.message.FunctionCallChannel;
import com.crestech.opkey.plugin.communication.transport.TCPv1Channel;
import com.crestech.opkey.plugin.communication.transport.TransportChannelFactory;
import com.crestech.opkey.plugin.communication.transport.TransportLayer;
import com.crestech.opkey.plugin.contexts.CommunicationProtocol;
import com.crestech.opkey.plugin.contexts.Context;
import com.crestech.opkey.plugin.contexts.SettingsLoader;
import com.crestech.opkey.plugin.logging.LogConfiguration;

public class Main {

	private static Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws Exception {
		int port = 5876;

		LogConfiguration.configure();
		logger.info("Starting visualscript-remote-client");

		File settingsXMLFile = new File(args[0]);
		SettingsLoader sl = new SettingsLoader();
		Map<String, String> settings = sl.load(settingsXMLFile);
		Context.session().setSettings(settings);

		String hostName = settings.get("Host");
		port = Integer.parseInt(settings.get("Port"));

		// TODO: gazab jugggaad for cloud
		if (port == 4444) {
			port = 4545;
			logger.fine("Assuming it to be cloud. Overriding remote port to 4545 from 4444");
		}

		CommunicationProtocol opkeyFacingProtocol = Context.session().getCommunicationProtocol();
		String opkeyFacingEndpoint = Context.session().getCommunicationEndpoint();
		TransportLayer opkeyFacing = TransportChannelFactory.getTransport(opkeyFacingProtocol, opkeyFacingEndpoint);

		TransportLayer remoteFacing = new TCPv1Channel(hostName + ":" + port);

		try {
			BridgingTransportLayer bridge = new BridgingTransportLayer(opkeyFacing, remoteFacing);
			FunctionCallChannel fcc = new ReversibleFunctionCallChannel(bridge);
			fcc.addObserver(new FunctionCallInterceptor());
			logger.finest("Opening the bridge");
			bridge.open();

			RemoteToLocalLogPump logPump = new RemoteToLocalLogPump(hostName, port);
			logger.finest("Starting remote-to-local log pump");
			logPump.start();

			Thread.currentThread().join();

		} catch (ConnectException ex) {
			if (ex.getMessage().startsWith("Connection refused")) {
				logger.severe("Please make sure you have started the visualscript-Remote plugin.");
				logger.severe("Also check that port " + port + " is not blocked at remote server.");
				logger.severe("It could be a firewall or an Antivirus.");
				System.exit(-4569);

			} else {
				throw ex;
			}
		}
	}
}
