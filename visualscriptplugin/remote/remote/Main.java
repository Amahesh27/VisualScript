package com.crestech.opkey.plugin.visualscriptplugin.remote.remote;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import com.crestech.opkey.plugin.logging.LogConfiguration;
import com.crestech.opkey.plugin.visualscriptplugin.remote.remote.OsCheck.OSType;

public class Main {

	private static Logger logger = Logger.getLogger(Main.class.getName());

	static OutputStream currentLogSink = null;
	static OutputStream SystemOut = System.out;

	static int port = 5876;
	static String visualscriptJavaJarPath = null;

	public static void main(String[] args) throws Throwable {
		if (args.length < 2) {
			throw new IllegalArgumentException("required parameters: <port> <path to vsisualscript-java.jar>");
		}

		port = Integer.parseInt(args[0]);
		visualscriptJavaJarPath = args[1];
		File sikuliJavaJar = new File(visualscriptJavaJarPath);

		if (!sikuliJavaJar.exists()) {
			throw new FileNotFoundException(visualscriptJavaJarPath);
		}

		ServerSocket server = new ServerSocket();
		server.bind(new InetSocketAddress(port), 5);

		currentLogSink = SystemOut;

		OutputStream ops = new OutputStream() {
			public synchronized void write(int b) throws IOException {
				Main.currentLogSink.write(b);

				if (Main.SystemOut != Main.currentLogSink) {
					Main.SystemOut.write(b);
				}
			}
		};

		System.setOut(new PrintStream(ops));
		System.setErr(new PrintStream(ops));

		LogConfiguration.configure();

		while (true) {
			handleConnectionsSerially(server);
		}
	}

	private static void handleConnectionsSerially(ServerSocket server) throws Exception {
		logger.finest("waiting for OpKey. Listening on port " + port);
		Socket opkey = server.accept();

		logger.finest("waiting for OpKey log sink");
		Socket logSink = server.accept();
		currentLogSink = logSink.getOutputStream();

		logger.finest("Starting plugin process");
		int localPort = server.getLocalPort();
		Process p = startPluginProcess(localPort);

		InputStream procOut = p.getInputStream();
		StreamPump logPump = new StreamPump(procOut, System.out);
		logPump.start();

		logger.finest("waiting for plugin to connect");
		Socket plugin = server.accept();

		Closeable messagePump = startMessagePump(opkey, plugin);

		logger.finest("waiting for plugin to finish");
		int exitCode = p.waitFor();
		logger.fine("plugin process exited with exit code " + exitCode);
		logger.fine("Good bye..");

		logger.info("=====--786587623539573-session-end-5167451764253045--====");
		logPump.close();
		logSink.close();
		messagePump.close();

		currentLogSink = SystemOut;
		logger.info("Session finished\n\n\n");
	}

	private static Process startPluginProcess(int localPort) throws Exception {
		File tmp1 = File.createTempFile("opkey_", "_settings.xml");
		tmp1.delete();
		InputStream in = Main.class.getResourceAsStream("_settings.xml");
		Files.copy(in, tmp1.toPath());
		in.close();
		Path tmp1Path = tmp1.toPath();

		String settingsXml = new String(Files.readAllBytes(tmp1Path));
		settingsXml = settingsXml.replace("$port$", String.valueOf(localPort));
		Files.write(tmp1Path, settingsXml.getBytes());

		// TODO: detect vsisualscriptplugin-x.y.jar
		String classPath = "vsisualscriptplugin-2.0.jar";
		ProcessBuilder pb = new ProcessBuilder();

		if (OsCheck.getOperatingSystemType() == OSType.Windows) {
			String dblQot = "\"";
			classPath = dblQot + classPath + ";" + visualscriptJavaJarPath + dblQot;

		} else { // linux
			classPath += ":" + visualscriptJavaJarPath;
		}

		pb.command("java", "-cp", classPath, "VisualScriptMain", "client", tmp1.getPath());

		pb.redirectErrorStream(true);
		// pb.redirectOutput(Redirect.INHERIT);
		Process p = pb.start();
		logger.fine("Spawned (" + p.toString() + ")");

		return p;
	}

	private static Closeable startMessagePump(final Socket opkey, final Socket plugin) throws Exception {

		logger.info("Starting message pump");

		final OutputStream opkeyIn = opkey.getOutputStream();
		final InputStream opkeyOut = opkey.getInputStream();

		final InputStream pluginOut = plugin.getInputStream();
		final OutputStream pluginIn = plugin.getOutputStream();

		final StreamPump opkey2plugin = new StreamPump(opkeyOut, pluginIn);
		final StreamPump plugin2opkey = new StreamPump(pluginOut, opkeyIn);

		opkey2plugin.start();
		plugin2opkey.start();

		Closeable c = new Closeable() {
			@Override
			public void close() {
				try {
					logger.finest("closing message pump");

					plugin2opkey.close();
					opkey2plugin.close();

					opkeyIn.close();
					opkeyOut.close();

					pluginIn.close();
					pluginOut.close();

					opkey.close();
					plugin.close();

				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					String exceptionAsString = sw.toString();
					logger.severe(exceptionAsString);
				}
			}
		};

		return c;
	}
}

class StreamPump extends Thread {

	private OutputStream[] outlets;
	private InputStream in;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public StreamPump(InputStream in, OutputStream... out) {
		this.outlets = out;
		this.in = in;
		this.setDaemon(true);
	}

	public void close() throws InterruptedException {
		interrupt();
		join();

		for (OutputStream os : outlets) {
			try {
				os.flush();

			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String exceptionAsString = sw.toString();
				logger.severe(exceptionAsString);
			}
		}
	}

	@Override
	public void run() {
		byte[] buff = new byte[65565];
		int readBytes = 0;

		while (true) {
			try {
				readBytes = in.read(buff);

				if (readBytes < 0)
					break;

				if (readBytes == 0)
					continue;

				for (OutputStream os : outlets) {
					os.write(buff, 0, readBytes);
				}

			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}