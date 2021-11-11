package com.crestech.opkey.plugin.visualscriptplugin.ExceptionHandlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import com.crestech.opkey.plugin.ExecutionStatus;
import com.crestech.opkey.plugin.ResultCodes;
import com.crestech.opkey.plugin.communication.contracts.functionresult.FunctionResult;
import com.crestech.opkey.plugin.functiondispatch.ExceptionHandler;

public class InvalidArgumentExceptionHandler implements ExceptionHandler {

	@Override
	public Class<?> getExceptionType() {
		return VisualScriptArgumentException.class;
	}

	@Override
	public FunctionResult handle(Throwable e) {
		FunctionResult res = new FunctionResult();
		res.setStatus(ExecutionStatus.Fail.toString());

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		String s = writer.toString();

		res.setMessage(s);
		res.setOutput("");
		res.setResultCode(ResultCodes.ERROR_ARGUMENT_DATA_INVALID.Code());
		return res;
	}

}
