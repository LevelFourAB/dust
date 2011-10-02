package se.l4.dust.js.env;

import org.slf4j.Logger;

/**
 * Console implementation used within {@link JavascriptEnvironment}.
 * 
 * @author Andreas Holstenson
 *
 */
public class JavascriptConsole
{
	private final Logger logger;

	public JavascriptConsole(Logger logger)
	{
		this.logger = logger;
	}
	
	public void log(String value)
	{
		logger.info(value);
	}
	
	public void log(String value, Object... values)
	{
		logger.info(String.format(value, values));
	}
}
