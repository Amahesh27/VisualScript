package com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers;

import org.sikuli.script.FindFailed;

import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.communication.contracts.functionresult.Result;
import com.crestech.opkey.plugin.exceptionhandling.CanHandle;
import com.crestech.opkey.plugin.exceptionhandling.ExceptionHandler2;
import com.crestech.opkey.plugin.exceptionhandling.Handleability;

public class FindFailedExceptionHandler implements ExceptionHandler2 {

	@Override
	public FunctionResult handle(Throwable e) {
		return Result.FAIL(ResultCodes.ERROR_OBJECT_NOT_FOUND).setMessage(e.getMessage()).make();
	}

	@Override
	public Handleability canHandle(Throwable e) {
		return CanHandle.givenThat().throwable(e).isSubclassOf(FindFailed.class);
	}
}
