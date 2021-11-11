package com.crestech.opkey.plugin.visualscriptplugin.remote.local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class RemoteToLocalLogPump extends Thread {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private String hostName = null;
	private int port = -1;

	public RemoteToLocalLogPump(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}

	@Override
	public void run() {
		Socket remote;
		try {
			remote = new Socket(hostName, port);

			InputStream in = remote.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			while (true) {
				String s = br.readLine();

				if (s == null)
					close();

				if (s.contains("=====--786587623539573-session-end-5167451764253045--====")) {
					close();
				}

				System.out.println(s);
			}

		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			logger.severe(exceptionAsString);
		}
	}

	private void close() {
		System.out.flush();
		logger.fine("Remote session closed.");
		logger.fine("Good Bye.");
		System.exit(8765);
	}

}
