package com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers;

import com.crestech.opkey.plugin.ExecutionStatus;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.functiondispatch.ExceptionHandler;

public class ImageNotFoundExceptionHandler implements ExceptionHandler {

	@Override
	public Class<?> getExceptionType() {
		return ImageNotFoundException.class;
	}

	@Override
	public FunctionResult handle(Throwable e) {
		FunctionResult res = new FunctionResult();
		res.setStatus(ExecutionStatus.Fail.toString());
		res.setMessage("Object does not contain 'Image' property.");
		res.setOutput("");
		res.setResultCode(ResultCodes.ERROR_ARGUMENT_DATA_MISSING.Code());
		return res;
	}

}
