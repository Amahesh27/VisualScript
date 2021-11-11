package com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers;

import java.io.FileNotFoundException;

import com.crestech.opkey.plugin.ExecutionStatus;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.functiondispatch.ExceptionHandler;

public class FileNotFoundExceptionHandler implements ExceptionHandler {

	@Override
	public Class<?> getExceptionType() {
		return FileNotFoundException.class;
	}

	@Override
	public FunctionResult handle(Throwable e) {
		FunctionResult res = new FunctionResult();
		res.setStatus(ExecutionStatus.Fail.toString());

		res.setMessage("File not found. " + e.getMessage());
		res.setOutput("");
		res.setResultCode(ResultCodes.ERROR_ARGUMENT_DATA_INVALID.Code());
		return res;
	}

}
