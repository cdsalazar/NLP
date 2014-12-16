package edu.uchsc.ccp.opendmap;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

	@Override
	public String format(LogRecord arg0) {
		return formatMessage(arg0) + "\n";
	}

}
