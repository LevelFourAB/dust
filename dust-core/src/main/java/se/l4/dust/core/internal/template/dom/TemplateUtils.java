package se.l4.dust.core.internal.template.dom;

import se.l4.dust.api.TemplateException;

public class TemplateUtils
{
	private TemplateUtils()
	{
	}
	
	public static void throwException(Object e, String message)
	{
		int line = -1;
		
		if(e instanceof LocationAware)
		{
			LocationAware l = (LocationAware) e;
			line = l.getLine();
		}
		
		if(line != -1)
		{
			throw new TemplateException(
				String.format("Error around line %s: %s", line, message)
			);
		}
		else
		{
			throw new TemplateException(message);
		}
	}
}
