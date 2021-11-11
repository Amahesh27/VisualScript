package com.crestech.opkey.plugin.visualscriptplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import org.sikuli.script.Screen;

import com.crestech.opkey.plugin.communication.message.AsynchronousEventChannel;
import com.crestech.opkey.plugin.communication.message.DecryptionCallChannel;
import com.crestech.opkey.plugin.communication.message.FunctionCallChannel;
import com.crestech.opkey.plugin.communication.transport.TransportChannelFactory;
import com.crestech.opkey.plugin.communication.transport.TransportLayer;
import com.crestech.opkey.plugin.contexts.CommunicationProtocol;
import com.crestech.opkey.plugin.contexts.Context;
import com.crestech.opkey.plugin.contexts.SettingsLoader;
import com.crestech.opkey.plugin.eventhandling.CloseableThread;
import com.crestech.opkey.plugin.eventhandling.DefaultTerminationEventHandler;
import com.crestech.opkey.plugin.eventhandling.EventHandler;
import com.crestech.opkey.plugin.exceptionhandling.ExceptionHandler2;
import com.crestech.opkey.plugin.functiondispatch.ArgumentFormatter;
import com.crestech.opkey.plugin.functiondispatch.BaseDispatcher;
import com.crestech.opkey.plugin.functiondispatch.Dispatcher;
import com.crestech.opkey.plugin.functiondispatch.ExceptionHandler;
import com.crestech.opkey.plugin.functiondispatch.FunctionDispatchLoop;
import com.crestech.opkey.plugin.functiondispatch.LibraryLocator;
import com.crestech.opkey.plugin.logging.LogConfiguration;
import com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers.FileNotFoundExceptionHandler;
import com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers.FindFailedExceptionHandler;
import com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers.ImageNotFoundExceptionHandler;
import com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers.InvalidArgumentExceptionHandler;

public class VisualScriptPlugin {

	private static Logger logger = Logger.getLogger(VisualScriptPlugin.class.getName());

	public static void main(String[] args) throws Throwable {
		/*
		 * 
		 * 
		 * get all necessary command-line-parameters here. remember to declare the
		 * argument in plugin manifest. for most purposes the settings file is
		 * sufficient
		 */
		File settingsXMLFile = new File(args[0]);

		if (settingsXMLFile.exists() && !settingsXMLFile.isDirectory()) {
			SettingsLoader sl = new SettingsLoader();
			Map<String, String> settings = sl.load(settingsXMLFile);
			Context.session().setSettings(settings);
		}

		/*
		 * 
		 * 
		 * following are some support services. these help in developing a plugin and
		 * are mostly independent of plugin implementation. this means any plugin can
		 * use these service.
		 */

		LogConfiguration.configure();

		LibraryLocator locator = new LibraryLocator();

		ArgumentFormatter formatter = new VisualScriptObjectFormatter();

		/*
		 * 
		 * 
		 * Set up the communication channel. this could be one of many possible options.
		 * One may use shared-sqlite-db or tcp or some other exotic communication
		 * mechanism.
		 */
		CommunicationProtocol communicationProtocol = Context.session().getCommunicationProtocol();
		String communicationEndpoint = Context.session().getCommunicationEndpoint();

		TransportLayer transport = TransportChannelFactory.getTransport(communicationProtocol, communicationEndpoint);

		FunctionCallChannel fCallChannel = new FunctionCallChannel(transport);

		AsynchronousEventChannel eventChannel = new AsynchronousEventChannel(transport);

		/*
		 * Mandatory for setting a new channel for decryption purpose
		 */
		DecryptionCallChannel dCallChannel = new DecryptionCallChannel(transport);
		Context.session().setDecryptionCallChannel(dCallChannel);

		transport.open();

		/*
		 * 
		 * 
		 * setup the tool. this step involves preparing the target tool, instantiate its
		 * APIs etc.
		 */
		Screen visualScriptScreen = new Screen();
		Context.session().setTool(visualScriptScreen);

		/*
		 * 
		 * 
		 * these are exception handlers. add handlers specific to your application
		 */
		ArrayList<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>();

		exceptionHandlers.add(new ImageNotFoundExceptionHandler());
		exceptionHandlers.add(new FileNotFoundExceptionHandler());
		exceptionHandlers.add(new InvalidArgumentExceptionHandler());

		ExceptionHandler[] arrExHandlers = exceptionHandlers.toArray(new ExceptionHandler[exceptionHandlers.size()]);

		ArrayList<ExceptionHandler2> newExceptionHandlers = new ArrayList<ExceptionHandler2>();

		newExceptionHandlers.add(new FindFailedExceptionHandler());

		/*
		 * 
		 * 
		 * set up the method dispatcher. user can provide their own dispatcher if the
		 * methods need to be called a bit differently
		 */
		Dispatcher dispatcher = new BaseDispatcher(locator, formatter, arrExHandlers, newExceptionHandlers);

		/*
		 * 
		 * 
		 * function loop reads and dispatches method one after one. this loop can be run
		 * in a separate thread.
		 */
		CloseableThread dispatchLoop = new CloseableThread(new FunctionDispatchLoop(fCallChannel, dispatcher, null));
		dispatchLoop.start();

		/*
		 * 
		 * 
		 * Subscribe to desired events. SESSION_ENDING event is a popular choice
		 * 
		 * you may even publish your own events, but that is quite rare
		 */
		EventHandler onTerminate = new DefaultTerminationEventHandler(dispatchLoop, fCallChannel, eventChannel,
				transport);
		eventChannel.subscribe(onTerminate);

		/*
		 * 
		 * 
		 * wait until the main loop dies out
		 */
		onTerminate.waitForNextEvent();
		logger.info("Good Bye...");

		System.exit(0);
		// required because at times /SOME/ thread blocks
		// termination even after Main method has ended.
	}
}